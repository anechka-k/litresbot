package litresbot.flibusta;

import java.io.ByteArrayInputStream;
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
import litresbot.opds.Crawler;
import litresbot.opds.FileNameParser;
import litresbot.opds.Link;
import litresbot.opds.Page;
import litresbot.opds.PageParser;
import litresbot.util.Convert;
import litresbot.util.Logger;

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
  
  public static SendMessageList getBooks(String searchQuery)
  {
    SendMessageList result = new SendMessageList(4096);
    boolean found = false;
    
    try
    {
      String encodedSearch = URLEncoder.encode(searchQuery, "UTF-8");      
      List<Page> pages = Crawler.downloadPages(rootOPDSDefault, String.format(bookSearch, encodedSearch));
      
      for(Page page : pages)
      {
        if (page.entries != null && page.entries.size() > 0)
        {
          found = true;
          
          if (page.title != null)
          {
            result.appendPage("<b>");
            result.appendPage(page.title);
            result.appendPage("</b>\n");
          }
          page.entries.stream().forEach(entry ->
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
              
            entry.links.stream()
              .filter(l -> l.rel != null && l.rel.contains("open-access"))
              .forEach(link ->
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
            );
              
            result.appendPage("\n");
            result.endPage();
          });
        }
      }
    }
    catch (IOException e)
    {
      Logger.logMessage("Http request failed: ", e);
    }
    
    if(!found)
    {
      result.appendPage("Nothing found");
      result.endPage();
      return result;
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
  
  public static byte[] download(String url, String fileName) throws IOException
  {
    byte[] book = BooksCache.getBookFromId(url);
    if(book != null)
    {
      Logger.logInfoMessage("book " + url + " found in cache");
      return book;
    }
    
    String bookUrl = rootOPDSDefault + url;
    InputStream bookData = HttpClientWithProxy.download(bookUrl, fileName);
    book = IOUtils.toByteArray(bookData);
    BooksCache.addBook(url, book);
    
    return book;
  }
}
