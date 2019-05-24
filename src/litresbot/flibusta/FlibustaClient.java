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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import litresbot.HttpClientWithProxy;
import litresbot.SendMessageList;
import litresbot.books.BookDownloader;
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
    BookDownloader.folder = "./books";
  }
  
  public static SendMessageList getBooks(String searchQuery)
  {
    SendMessageList result = new SendMessageList(4096);
    
    try
    {
      String encodedSearch = URLEncoder.encode(searchQuery, "UTF-8");
      List<Entry> entries = new ArrayList<Entry>();
      
      List<Entry> bookEntries = Crawler.downloadBooks(rootOPDSDefault, String.format(bookSearch, encodedSearch), entries);
      List<Entry> authorEntries = Crawler.downloadBooks(rootOPDSDefault, String.format(authorSearch, encodedSearch), bookEntries);
      bookEntries.addAll(authorEntries);
      
      Long booksCount = (long) bookEntries.size();
      
      if(booksCount == 0)
      {
        result.appendPage("К сожалению ничего не найдено");
        result.endPage();
        return result;
      }
      
      String booksText = PluralsText.convert("книга", booksCount);
      result.appendPage("Найдено: " + bookEntries.size() + " " + booksText + "\n\n");
      result.endPage();
      
      for(Entry entry : bookEntries)
      {
        result.appendPage("<b>");
        result.appendPage(entry.title);
        result.appendPage("</b>\n");
            
        if (entry.author != null)
        {
          result.appendPage(" (");
          result.appendPage(entry.author);
          result.appendPage(")");
        }
            
        result.appendPage("\n");
        
        for(Link link : entry.links)
        {
          String type = link.type.replace("application/", "");
          long hashCode = Integer.toUnsignedLong(link.href.hashCode());
          String id = Long.toHexString(hashCode);
                
          // create a copy of the link with trimmed type
          Link newLink = new Link(link.href, type, link.title, link.rel);
          urlCache.put(id, newLink);
                
          result.appendPage(type);
          result.appendPage(" : /download");
          result.appendPage("" + hashCode);
          result.appendPage("\n");
        }
              
        result.appendPage("\n");
        result.endPage();
      }
    }
    catch (IOException e)
    {
      Logger.logMessage("Http request failed: ", e);
    }
    
    return result;
  }
  
  public static String getUrlFromId(String id)
  {
    long hashCode = Convert.parseLong(id);
    String hashCodeHex = Long.toHexString(hashCode);
    
    Link link = urlCache.getIfPresent(hashCodeHex);  
    if(link == null) return null;
    
    return link.href;
  }
   
  public static String getFilenameFromId(String id)
  {
    long hashCode = Convert.parseLong(id);
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

  public static byte[] downloadWithCache(String bookId) throws IOException
  {
    String bookUrlShort = getUrlFromId(bookId);
    
    if(bookUrlShort == null)
    {
      return null;
    }
    
    String fileName = getFilenameFromId(bookId);
    
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
