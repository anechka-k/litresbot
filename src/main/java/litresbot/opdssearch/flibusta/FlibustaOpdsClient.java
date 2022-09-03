package litresbot.opdssearch.flibusta;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import litresbot.AppProperties;
import litresbot.books.BookFileLink;
import litresbot.books.BookInfo;
import litresbot.opdssearch.opds.Entry;
import litresbot.opdssearch.opds.FileNameParser;
import litresbot.opdssearch.opds.Link;
import litresbot.opdssearch.opds.Page;

public class FlibustaOpdsClient
{
  final static Logger logger = Logger.getLogger(FlibustaOpdsClient.class);
  
  private static final String authorSearch = "/search?searchType=authors&searchTerm=%s";
  private static final String bookSearch = "/search?searchType=books&searchTerm=%s";
  public static String flibustaHost = "";
  static {
    flibustaHost = AppProperties.getStringProperty("flibustaHost");
    if (flibustaHost == null || flibustaHost.length() == 0) {
      flibustaHost = "http:\\flibusta.is";
    }
  }
  
  public static OpdsSearchResult searchBooks(String searchQuery)
  {
    OpdsSearchResult result = new OpdsSearchResult();
    result.books = new ArrayList<BookInfo>();

    List<Entry> bookEntries = new ArrayList<Entry>();    
    try
    {
      bookEntries = downloadBooks(flibustaHost, searchQuery);
    }
    catch (IOException e)
    {
      logger.warn("OPDS search results an error", e);
      return result;
    }
    
    for(Entry e : bookEntries)
    {
      BookInfo bookInfo = new BookInfo();
      bookInfo.links = new ArrayList<BookFileLink>();
      bookInfo.title = e.title;
      bookInfo.author = e.author;
      bookInfo.id = e.id;
      bookInfo.site = flibustaHost;
      bookInfo.annotation = e.annotation;

      for(Link l : e.links)
      {
        if(!l.rel.toLowerCase().contains("open-access")) continue;
        
        String[] fileparts = FileNameParser.parse(l.href, l.type);
        
        BookFileLink bookFileLink = new BookFileLink();
        bookFileLink.href = l.href;
        bookFileLink.format = fileparts[1];

        bookInfo.links.add(bookFileLink);
      }

      result.books.add(bookInfo);
    }
    
    return result;
  }

  private static List<Entry> downloadBooks(String host, String searchQuery) throws IOException {
    List<Entry> bookEntries = new ArrayList<Entry>();

    FlibustaOpdsCache.CacheRecord cached = FlibustaOpdsCache.cache.getIfPresent(searchQuery);
    if (cached != null) return cached.entries;

    String encodedSearch = URLEncoder.encode(searchQuery, "UTF-8");

    List<Page> bookPages = new ArrayList<Page>();
    List<Page> authorPages = new ArrayList<Page>();
    List<Page> allPages = new ArrayList<Page>();
        
    bookPages = FlibustaCrawler.downloadBooksSearch(host, String.format(bookSearch, encodedSearch));
    authorPages = FlibustaCrawler.downloadAuthorsSearch(host, String.format(authorSearch, encodedSearch));
        
    allPages = new ArrayList<Page>();
    allPages.addAll(bookPages);
    allPages.addAll(authorPages);
        
    bookEntries = FlibustaCrawler.processBooks(allPages);

    FlibustaOpdsCache.CacheRecord newCached = new FlibustaOpdsCache.CacheRecord();
    newCached.entries = bookEntries;
    newCached.url = searchQuery;
    FlibustaOpdsCache.cache.put(searchQuery, newCached);
    return bookEntries;
  }
}
