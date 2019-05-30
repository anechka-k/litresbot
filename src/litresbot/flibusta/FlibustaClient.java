package litresbot.flibusta;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import litresbot.HttpClientWithProxy;
import litresbot.SendMessageList;
import litresbot.books.BookDownloader;
import litresbot.books.BookFileId;
import litresbot.books.BookInfoId;
import litresbot.books.PluralsText;
import litresbot.opds.Crawler;
import litresbot.opds.Entry;
import litresbot.opds.FileNameParser;
import litresbot.opds.Link;
import litresbot.opds.Page;
import litresbot.opds.PageParser;
import litresbot.util.Convert;
import litresbot.util.Logger;
import litresbot.util.Plurals;
import litresbot.util.Plurals.PluralForm;

@SuppressWarnings("unused")
public class FlibustaClient
{
  public static final int URL_CACHE_SIZE = 1000;
  
  private static final String rootOPDStor = "http://flibustahezeous3.onion";
  private static final String rootOPDShttp = "http://flibusta.is";
  private static final String rootOPDSDefault = rootOPDStor;
  private static final String authorSearch = "/search?searchType=authors&searchTerm=%s";
  private static final String bookSearch = "/search?searchType=books&searchTerm=%s";
  private static Cache<String, Link> urlCache;
  static
  {
    urlCache = CacheBuilder.newBuilder().maximumSize(URL_CACHE_SIZE).build();
  }
  
  static
  {
    BookDownloader.folder = "./flibooks";
  }
  
  private static HashMap<String, Entry> booksCache;
  static
  {
    booksCache = new HashMap<String, Entry>();
  }
  
  public static SendMessageList getBooks(String searchQuery)
  {
    SendMessageList result = new SendMessageList(4096);
    
    try
    {
      String encodedSearch = URLEncoder.encode(searchQuery, "UTF-8");
      List<Entry> entries = new ArrayList<Entry>();
      
      List<Page> bookPages = FlibustaCrawler.downloadBooksSearch(rootOPDSDefault, String.format(bookSearch, encodedSearch));
      List<Page> authorPages = FlibustaCrawler.downloadAuthorsSearch(rootOPDSDefault, String.format(authorSearch, encodedSearch));
      
      List<Page> allPages = new ArrayList<Page>();
      allPages.addAll(bookPages);
      allPages.addAll(authorPages);
      
      List<Entry> bookEntries = FlibustaCrawler.processBooks(allPages);
      
      Long booksCount = (long) bookEntries.size();
      
      if(booksCount == 0)
      {
        result.appendTextPage("К сожалению ничего не найдено");
        result.endTextPage();
        return result;
      }
      
      String booksText = PluralsText.convert("книга", booksCount);
      result.appendTextPage("Найдено: " + bookEntries.size() + " " + booksText + "\n\n");
      result.endTextPage();
      
      for(Entry entry : bookEntries)
      {
        result.appendTextPage("<b>");
        result.appendTextPage(entry.title);
        result.appendTextPage("</b>\n");
            
        if (entry.author != null)
        {
          result.appendTextPage(" (");
          result.appendTextPage(entry.author);
          result.appendTextPage(")\n");
        }
            
        result.appendTextPage("Скачать: ");
        result.appendTextPage("/bookinfo");
        result.appendTextPage(entry.id);
              
        result.appendTextPage("\n\n");
        result.endTextPage();
        
        booksCache.put(entry.id, entry);
      }
    }
    catch (IOException e)
    {
      Logger.logMessage("Http request failed: ", e);
    }
    
    return result;
  }
  
  public static SendMessageList getBookInfo(BookInfoId bookId)
  {
    SendMessageList result = new SendMessageList(4096);
    
    Entry bookInfo = booksCache.get(bookId.id);
    if(bookInfo == null)
    {
      result.appendTextPage("К сожалению ничего не найдено");
      result.endTextPage();
      return result;
    }
    
    result.appendTextPage("<b>");
    result.appendTextPage(bookInfo.title);
    result.appendTextPage("</b>\n");
        
    if (bookInfo.author != null)
    {
      result.appendTextPage(" (");
      result.appendTextPage(bookInfo.author);
      result.appendTextPage(")\n");
    }
    
    result.endTextPage();
    
    List<InlineKeyboardButton> buttonsRow = new ArrayList<InlineKeyboardButton>();
    InlineKeyboardButton btn1 = new InlineKeyboardButton();
    InlineKeyboardButton btn2 = new InlineKeyboardButton();
    btn1.setText("Скачать");
    btn1.setCallbackData("/format" + bookInfo.id);
    btn2.setText("Читать");
    btn2.setCallbackData("/read" + bookInfo.id);
    
    buttonsRow.add(btn1);
    buttonsRow.add(btn2);
    
    List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
    buttons.add(buttonsRow);
    result.appendButtons(buttons);
    
    return result;
  }
  
  public static SendMessageList chooseBookFormat(BookInfoId bookId)
  {
    SendMessageList result = new SendMessageList(4096);
    
    Entry bookInfo = booksCache.get(bookId.id);
    if(bookInfo == null)
    {
      result.appendTextPage("К сожалению ничего не найдено");
      result.endTextPage();
      return result;
    }
    
    result.appendTextPage("<b>");
    result.appendTextPage(bookInfo.title);
    result.appendTextPage("</b>\n");
        
    if (bookInfo.author != null)
    {
      result.appendTextPage(" (");
      result.appendTextPage(bookInfo.author);
      result.appendTextPage(")\n");
    }
    
    result.endTextPage();
    
    // generate keyboard with download formats

    List<InlineKeyboardButton> buttonsRow = new ArrayList<InlineKeyboardButton>();
    
    for(Link link : bookInfo.links)
    {
      String type = link.type.replace("application/", "");
      long hashCode = Integer.toUnsignedLong(link.href.hashCode());
      String id = Long.toHexString(hashCode);
            
      // create a copy of the link with trimmed type
      Link newLink = new Link(link.href, type, link.title, link.rel);
      urlCache.put(id, newLink);
      
      String shortBookType = type.replace("+zip", "");
      InlineKeyboardButton btn1 = new InlineKeyboardButton();
      btn1.setText(shortBookType.toUpperCase());
      btn1.setCallbackData("/download" + hashCode);
      buttonsRow.add(btn1);
    }
    
    List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
    buttons.add(buttonsRow);
    result.appendButtons(buttons);
    
    return result;
  }

  public static SendMessageList readBook(String argument)
  {
    SendMessageList result = new SendMessageList(4096);
    result.appendTextPage("Not implemented\n");
    result.endTextPage();
    return result;
  }
  
  public static String getUrlFromBookFileId(BookFileId id)
  {
    long hashCode = Convert.parseLong(id.id);
    String hashCodeHex = Long.toHexString(hashCode);
    
    Link link = urlCache.getIfPresent(hashCodeHex);  
    if(link == null) return null;
    
    return link.href;
  }
   
  public static String getFilenameFromBookFileId(BookFileId id)
  {
    long hashCode = Convert.parseLong(id.id);
    String hashCodeHex = Long.toHexString(hashCode);
    
    Link link = urlCache.getIfPresent(hashCodeHex);
    if(link == null) return null;
    
    String url = link.href;
    String type= link.type;
    
    String filename = FileNameParser.parse(url);
    
    if(type.endsWith("+zip") && (!filename.endsWith(".zip")))
    {
      filename += ".zip";
    }
    
    return filename;
  }

  public static byte[] downloadWithCache(BookFileId bookFileId) throws IOException
  {
    String bookUrlShort = getUrlFromBookFileId(bookFileId);
    
    if(bookUrlShort == null)
    {
      return null;
    }
    
    String fileName = getFilenameFromBookFileId(bookFileId);
    
    if(fileName == null)
    {
      return null;
    }
    
    Logger.logInfoMessage("Downloading book: " + bookUrlShort);
    byte[] book = BookDownloader.downloadWithCache(rootOPDSDefault, bookUrlShort, fileName);
    Logger.logInfoMessage("Downloading book done: " + bookUrlShort);
    
    return book;
  }
}
