package litresbot.opdssearch.flibusta;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import litresbot.AppProperties;
import litresbot.opdssearch.opds.Entry;
import litresbot.opdssearch.opds.FileNameParser;
import litresbot.opdssearch.opds.Link;
import litresbot.opdssearch.opds.Page;

public class FlibustaClient
{
  private static final String authorSearch = "/search?searchType=authors&searchTerm=%s";
  private static final String bookSearch = "/search?searchType=books&searchTerm=%s";
  
  public static JSONObject searchBooks(String searchQuery) throws JSONException
  {
    JSONObject result = new JSONObject();
    
    String encodedSearch;
    try
    {
      encodedSearch = URLEncoder.encode(searchQuery, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      result = new JSONObject();
      result.put("result", "error");
      result.put("error", e.getMessage());
      return result;
    }
    
    String flibustaHost = AppProperties.getStringProperty("flibustaHost");
    if (flibustaHost == null || flibustaHost.length() == 0) {
      flibustaHost = "http:\\flibusta.is";
    }
    result.put("site", flibustaHost);
    result.put("library", "flibustaOPDS");
    
    List<Entry> bookEntries = new ArrayList<Entry>();
    
    try
    {
      List<Page> bookPages = new ArrayList<Page>();
      List<Page> authorPages = new ArrayList<Page>();
      List<Page> allPages = new ArrayList<Page>();
      
      bookPages = FlibustaCrawler.downloadBooksSearch(flibustaHost, String.format(bookSearch, encodedSearch));
      authorPages = FlibustaCrawler.downloadAuthorsSearch(flibustaHost, String.format(authorSearch, encodedSearch));
      
      allPages = new ArrayList<Page>();
      allPages.addAll(bookPages);
      allPages.addAll(authorPages);
      
      bookEntries = FlibustaCrawler.processBooks(allPages);
    }
    catch (IOException e)
    {
      result = new JSONObject();
      result.put("result", "error");
      result.put("error", e.getMessage());
      return result;
    }
    
    result.put("result", "ok");
    JSONArray books = new JSONArray();
    
    for(Entry e : bookEntries)
    {
      JSONObject book = new JSONObject();
      book.put("title", e.title);
      book.put("author", e.author);
      book.put("id", e.id);
      
      JSONArray links = new JSONArray();
      for(Link l : e.links)
      {
        if(!l.rel.toLowerCase().contains("open-access")) continue;
        
        String[] fileparts = FileNameParser.parse(l.href, l.type);
        
        JSONObject link = new JSONObject();
        link.put("href", l.href);
        link.put("format", fileparts[1]);

        links.put(link);
      }
      
      book.put("links", links);
      books.put(book);
    }
    
    result.put("books", books);
    
    return result;
  }
}
