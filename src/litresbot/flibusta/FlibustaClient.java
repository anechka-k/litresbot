package litresbot.flibusta;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import litresbot.AppProperties;
import litresbot.HttpClientPostLocal;
import litresbot.SendMessageList;
import litresbot.books.BookDownloader;
import litresbot.books.BookFileId;
import litresbot.books.BookFileLink;
import litresbot.books.BookInfo;
import litresbot.books.BookInfoId;
import litresbot.books.PluralsText;
import litresbot.books.PluralsTextEn;
import litresbot.localisation.UserMessagesEn;
import litresbot.util.Logger;

public class FlibustaClient
{
  public static final int URL_CACHE_SIZE = 1000;
  
  private static Cache<String, BookFileLink> urlCache;
  static
  {
    urlCache = CacheBuilder.newBuilder().maximumSize(URL_CACHE_SIZE).build();
  }
  
  static
  {
    BookDownloader.folder = "./flibooks";
  }
  
  private static HashMap<String, BookInfo> booksCache;
  static
  {
    booksCache = new HashMap<String, BookInfo>();
  }
  
  public static SendMessageList getBooks(String searchQuery)
  {
    SendMessageList result = new SendMessageList(4096);
    
    try
    {
      //List<BookInfo> bookEntries = new ArrayList<BookInfo>();
      
      String encodedSearch = URLEncoder.encode(searchQuery, "UTF-8");
      String opdsFlibustaUrl = AppProperties.getStringProperty("opdsFlibustaUrl");
      String requestUrl = opdsFlibustaUrl + "?request=searchBook&query=" + encodedSearch;
      
      JSONObject requestResult = HttpClientPostLocal.request(requestUrl);
      
      String resultStatus = requestResult.getString("result");
      
      if(!resultStatus.contentEquals("ok"))
      {
        Logger.logInfoMessage("OPDS search results an error: " + requestResult.getString("error"));
        
        result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorSearchNotFound));
        result.endTextPage();
        return result;
      }
      
      JSONArray books = (JSONArray) requestResult.get("books");
      
      if((books == null) || (books.length() == 0))
      {
        result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorSearchNotFound));
        result.endTextPage();
        return result;
      }
      
      int booksCount = books.length();
      
      String bookText = litresbot.Application.userMessages.get(UserMessagesEn.bookText);
      String booksText = PluralsTextEn.convert(bookText, (long) booksCount);
      
      if(litresbot.Application.userMessages.language().contentEquals("ru"))
      {
        booksText = PluralsText.convert(bookText, (long) booksCount);
      }
      
      result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.searchFoundTotal) +
          booksCount + " " + booksText + "\n\n");
      result.endTextPage();
      
      /*for(BookInfo entry : bookEntries)
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
            
        result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.searchGoto));
        result.appendTextPage("/bookinfo");
        result.appendTextPage(entry.id);
              
        result.appendTextPage("\n\n");
        result.endTextPage();
        
        booksCache.put(entry.id, entry);
      }*/
    }
    catch (Exception e)
    {
      Logger.logMessage("Http request failed: ", e);
    }
    
    return result;
  }
  
  public static SendMessageList getBookInfo(BookInfoId bookId)
  {
    SendMessageList result = new SendMessageList(4096);
    
    BookInfo bookInfo = booksCache.get(bookId.id);
    if(bookInfo == null)
    {
      result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorSearchNotFound));
      result.endTextPage();
      return result;
    }
    
    /*result.appendTextPage("<b>");
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
    btn1.setText(litresbot.Application.userMessages.get(UserMessagesEn.searchDownload));
    btn1.setCallbackData("/format" + bookInfo.id);
    btn2.setText(litresbot.Application.userMessages.get(UserMessagesEn.searchRead));
    btn2.setCallbackData("/read" + bookInfo.id);
    
    buttonsRow.add(btn1);
    buttonsRow.add(btn2);
    
    List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
    buttons.add(buttonsRow);
    result.appendButtons(buttons);*/
    
    return result;
  }
  
  public static SendMessageList chooseBookFormat(BookInfoId bookId)
  {
    SendMessageList result = new SendMessageList(4096);
    
    BookInfo bookInfo = booksCache.get(bookId.id);
    if(bookInfo == null)
    {
      result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorSearchNotFound));
      result.endTextPage();
      return result;
    }
    
    /*result.appendTextPage("<b>");
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
      String[] fileparts = FileNameParser.parse(link.href, link.type);
      String type = fileparts[1];
      
      long hashCode = Integer.toUnsignedLong(link.href.hashCode());
      String id = Long.toHexString(hashCode);

      urlCache.put(id, link);

      InlineKeyboardButton btn1 = new InlineKeyboardButton();
      btn1.setText(type.toUpperCase());
      btn1.setCallbackData("/download" + hashCode);
      buttonsRow.add(btn1);
    }
    
    List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
    buttons.add(buttonsRow);
    result.appendButtons(buttons);*/
    
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
    BookFileLink link = urlCache.getIfPresent(id.id);  
    if(link == null) return null;
    
    return link.href;
  }
   
  public static String getFilenameFromBookFileId(BookFileId id)
  {    
    BookFileLink link = urlCache.getIfPresent(id.id);
    if(link == null) return null;
    
    return id.id;
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
    byte[] book = /*BookDownloader.downloadWithCache(rootOPDSDefault, bookUrlShort, fileName)*/null;
    Logger.logInfoMessage("Downloading book done: " + bookUrlShort);
    
    return book;
  }
}
