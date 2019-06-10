package litresbot.flibusta;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import litresbot.AppProperties;
import litresbot.books.BookDownloader;
import litresbot.books.BookFileId;
import litresbot.books.BookFileLink;
import litresbot.books.BookInfo;
import litresbot.books.BookInfoId;
import litresbot.books.PluralsText;
import litresbot.books.PluralsTextEn;
import litresbot.http.HttpClientWithProxy;
import litresbot.http.HttpSourceType;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.SendMessageList;
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
      
      String bookText = litresbot.Application.userMessages.get(UserMessagesEn.bookText);
      String booksText = PluralsTextEn.convert(bookText, (long) booksCount);
      
      if(litresbot.Application.userMessages.language().contentEquals("ru"))
      {
        booksText = PluralsText.convert(bookText, (long) booksCount);
      }
      
      result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.searchFoundTotal) +
          booksCount + " " + booksText + "\n\n");
      result.endTextPage();
      
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
        
        booksCache.put(book.id, book);
      }
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
