package litresbot.flibusta;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.xml.sax.SAXException;

import com.kursx.parser.fb2.Body;
import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.P;
import com.kursx.parser.fb2.Section;
import com.kursx.parser.fb2.Title;

import litresbot.AppProperties;
import litresbot.books.BookDownloader;
import litresbot.books.BookFileLink;
import litresbot.books.BookInfo;
import litresbot.books.FileExtensions;
import litresbot.books.plurals.PluralsText;
import litresbot.books.plurals.PluralsTextEn;
import litresbot.http.HttpClientWithProxy;
import litresbot.http.HttpSourceType;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.SendMessageList;
import litresbot.util.Logger;
import litresbot.util.TelegramEscape;

public class FlibustaClient
{  
  static
  {
    BookDownloader.folder = "./flibooks";
  }
  
  private static HashMap<String, BookInfo> booksCache;
  static
  {
    booksCache = new HashMap<String, BookInfo>();
  }
  
  public static List<BookInfo> getOpdsBooks(String searchQuery) throws JSONException, IOException
  {
    List<BookInfo> bookEntries = new ArrayList<BookInfo>();
    
    try
    {
      String encodedSearch = URLEncoder.encode(searchQuery, "UTF-8");
      String opdsFlibustaUrl = AppProperties.getStringProperty("opdsFlibustaUrl");
      String requestUrl = opdsFlibustaUrl + "?request=searchBook&query=" + encodedSearch;
      
      String postAnswer = HttpClientWithProxy.sendPostRequest(requestUrl, "", HttpSourceType.OPDS_SERVICE);
      JSONObject requestResult = new JSONObject(postAnswer);
      
      String resultStatus = requestResult.getString("result");
      
      if(!resultStatus.contentEquals("ok"))
      {
        Logger.logInfoMessage("OPDS search results an error: " + requestResult.getString("error"));
        return bookEntries;
      }
      
      JSONArray books = requestResult.getJSONArray("books");
      String searchSite = requestResult.getString("site");
      
      for(int i = 0; i < books.length(); i++)
      {
        JSONObject book = books.getJSONObject(i);
        
        BookInfo bookInfo = new BookInfo();
        bookInfo.author = book.getString("author");
        bookInfo.title = book.getString("title");
        bookInfo.id = book.getString("id");
        bookInfo.site = searchSite;
        
        bookInfo.links = new ArrayList<BookFileLink>();
        
        JSONArray links = book.getJSONArray("links");
        for(int j = 0; j < links.length(); j++)
        {
          JSONObject link = links.getJSONObject(j);
          
          BookFileLink bookFileLink = new BookFileLink();
          bookFileLink.href = link.getString("href");
          bookFileLink.format = link.getString("format");
          
          bookInfo.links.add(bookFileLink);
        }
        
        bookEntries.add(bookInfo);
      }
    }
    catch(Exception e)
    {
      Logger.logMessage("Error while searching with OPDS service", e);
      bookEntries = new ArrayList<BookInfo>();
      return bookEntries;
    }
    
    return bookEntries;
  }
  
  public static SendMessageList getBooks(String searchQuery)
  {
    SendMessageList result = new SendMessageList(4096);
    
    try
    {
      List<BookInfo> bookEntries = getOpdsBooks(searchQuery);
      
      int booksCount = bookEntries.size();
      
      if(booksCount == 0)
      {
        result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorSearchNotFound));
        result.endTextPage();
        return result;
      }
      
      // create cache data to allow instant access by book id
      for(BookInfo book : bookEntries)
      {
        booksCache.put(book.id, book);
      }
      
      // generate the search result header - how much books found
      
      String bookText = litresbot.Application.userMessages.get(UserMessagesEn.bookText);
      String booksText = PluralsTextEn.convert(bookText, (long) booksCount);
      
      if(litresbot.Application.userMessages.language().contentEquals("ru"))
      {
        booksText = PluralsText.convert(bookText, (long) booksCount);
      }
      
      result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.searchFoundTotal) +
          booksCount + " " + booksText + "\n\n");
      result.endTextPage();
      
      // generate the search result body
      
      for(BookInfo book : bookEntries)
      {
        result.appendTextPage("<b>");
        result.appendTextPage(book.title);
        result.appendTextPage("</b>\n");
            
        if (book.author != null)
        {
          result.appendTextPage(" (");
          result.appendTextPage(book.author);
          result.appendTextPage(")\n");
        }
            
        result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.searchGoto));
        result.appendTextPage("/bookinfo");
        result.appendTextPage(book.id);
              
        result.appendTextPage("\n\n");
        result.endTextPage();
      }
    }
    catch (Exception e)
    {
      Logger.logMessage("Http request failed", e);
    }
    
    return result;
  }
  
  public static SendMessageList getBookInfo(String bookId)
  {
    SendMessageList result = new SendMessageList(4096);
    
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null)
    {
      result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorSearchNotFound));
      result.endTextPage();
      return result;
    }
    
    // generate the book info header
    
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
    
    // generate the book info download and read buttons
    
    List<InlineKeyboardButton> buttonsRow = new ArrayList<InlineKeyboardButton>();
    InlineKeyboardButton btn1 = new InlineKeyboardButton();
    InlineKeyboardButton btn2 = new InlineKeyboardButton();
    btn1.setText(litresbot.Application.userMessages.get(UserMessagesEn.searchDownload));
    btn1.setCallbackData("/format" + bookInfo.id);
    btn2.setText(litresbot.Application.userMessages.get(UserMessagesEn.searchRead));
    btn2.setCallbackData("/read" + bookInfo.id + "p" + "0");
    
    buttonsRow.add(btn1);
    buttonsRow.add(btn2);
    
    List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
    buttons.add(buttonsRow);
    result.appendButtons(buttons);
    
    return result;
  }
  
  public static SendMessageList chooseBookFormat(String bookId)
  {
    SendMessageList result = new SendMessageList(4096);
    
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null)
    {
      result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorSearchNotFound));
      result.endTextPage();
      return result;
    }
    
    // generate the book info header
    
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
    
    for(BookFileLink link : bookInfo.links)
    {      
      String formatType = FileExtensions.detectFormat(link.format);
      
      if(formatType == null) continue;

      InlineKeyboardButton btn1 = new InlineKeyboardButton();
      btn1.setText(formatType.toUpperCase());
      btn1.setCallbackData("/download" + formatType.toLowerCase() + bookId);
      buttonsRow.add(btn1);
    }
    
    List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
    buttons.add(buttonsRow);
    result.appendButtons(buttons);
    
    return result;
  }

  public static SendMessageList readBook(String bookId, Long position)
  {
    String defaultFormat = "fb2";
    SendMessageList result = new SendMessageList(4096);
    
    String root = getUrlRootFromBook(bookId);
    if(root == null) return null;
    
    String url = getUrlShortFromBook(bookId, defaultFormat);
    if(url == null) return null;
    
    String fileName = getFilenameFromBook(bookId, defaultFormat);
    if(fileName == null) return null;
    
    byte[] book = null;
        
    try
    {
      Logger.logInfoMessage("Downloading book: " + url);
      book = BookDownloader.downloadUnzipBook(root, url, fileName);
      Logger.logInfoMessage("Downloading book done: " + url);
    }
    catch(IOException e)
    {
      Logger.logMessage("Book unzip failed", e);
    }
    
    if(book == null)
    {
      result = new SendMessageList(4096);
      result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorCouldNotDownloadFile));
      result.endTextPage();
      return result;
    }
    
    // now we have a book content in byte array
    // let's read it
    
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(book);
    
    try
    {      
      FictionBook fb2 = new FictionBook(byteArrayInputStream);
      
      Body fb2Body = fb2.getBody();
      
      if(fb2Body == null)
      {
        result = new SendMessageList(4096);
        result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorCouldNotDownloadFile));
        result.endTextPage();
        return result;
      }
      
      List<Section> sections = fb2Body.getSections();
      
      int i = 0;
      
      for(Section section : sections)
      {
        Title sectionTitle = section.getTitle();
        
        // show title if available
        if(sectionTitle != null)
        {
          List<P> titleParagraphs = sectionTitle.getParagraphs();
          
          // show title paragraphs if available
          if(titleParagraphs != null)
          {
            for(P paragraph : titleParagraphs)
            {
              String line = paragraph.getText();
              String escapedLine = TelegramEscape.escapeText(line);
              
              result.appendTextPage("<b>");
              result.appendTextPage("\n" + escapedLine + "\n\n");
              result.appendTextPage("</b>");
              result.endTextPage();
            }
          }
        }
        
        List<P> paragraphs = section.getParagraphs();
        
        // show text if available
        if(paragraphs != null)
        {
          for(P paragraph : paragraphs)
          {
            String line = paragraph.getText();
            String escapedLine = TelegramEscape.escapeText(line);
            
            result.appendTextPage(escapedLine + "\n\n");
            result.endTextPage();
          }
        }
        
        i++;
        
        if(i > 3) break;
      }
    }
    catch(IOException e)
    {
      Logger.logMessage("Read book failed", e);
    }
    catch (ParserConfigurationException e)
    {
      Logger.logMessage("Parse book failed", e);
    }
    catch (SAXException e)
    {
      Logger.logMessage("Parse book failed", e);
    }
    
    return result;
  }
  
  public static String getUrlFromBook(String bookId, String format)
  {
    String root = getUrlRootFromBook(bookId);
    if(root == null) return null;
    
    String url = getUrlShortFromBook(bookId, format);
    if(url == null) return null;
    
    return root + url;
  }
   
  public static String getFilenameFromBook(String bookId, String format)
  {
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null)
    {
      return bookId;
    }
    
    for(BookFileLink link : bookInfo.links)
    {
      String linkFormat = FileExtensions.detectFormat(link.format);
      if(linkFormat == null) continue;
      if(!linkFormat.contentEquals(format)) continue;
      
      return bookId + "." + link.format;
    }
    
    return bookId;
  }

  public static byte[] downloadWithCache(String bookId, String format) throws IOException
  {
    String root = getUrlRootFromBook(bookId);
    if(root == null) return null;
    
    String url = getUrlShortFromBook(bookId, format);
    if(url == null) return null;
    
    String fileName = getFilenameFromBook(bookId, format);
    if(fileName == null) return null;
    
    Logger.logInfoMessage("Downloading book: " + url);
    byte[] book = BookDownloader.downloadWithCache(root, url, fileName);
    Logger.logInfoMessage("Downloading book done: " + url);
    
    return book;
  }
  
  private static String getUrlRootFromBook(String bookId)
  {    
    BookInfo book = booksCache.get(bookId);  
    if(book == null) return null;    
    return book.site;
  }
  
  private static String getUrlShortFromBook(String bookId, String format)
  {    
    BookInfo book = booksCache.get(bookId);  
    if(book == null) return null;
    
    format = format.toLowerCase();
    
    for(BookFileLink link : book.links)
    {
      String linkFormat = FileExtensions.detectFormat(link.format);
      if(linkFormat == null) continue;
      if(!linkFormat.contentEquals(format)) continue;
      
      return link.href;
    }
    
    return null;
  }
}
