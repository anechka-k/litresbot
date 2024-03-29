package litresbot.opdssearch.flibusta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import litresbot.opdssearch.opds.Crawler;
import litresbot.opdssearch.opds.Entry;
import litresbot.opdssearch.opds.Link;
import litresbot.opdssearch.opds.Page;

public class FlibustaCrawler
{
  static Crawler crawler = new Crawler();

  public static List<Entry> processBooks(List<Page> pages) throws IOException
  {
    List<Entry> entries = new ArrayList<Entry>();
    Set<String> processedEntries = new HashSet<String>();
    
    for(Page page : pages)
    {
      // collect book links
      for(Entry entry : page.entries)
      {
        if(processedEntries.contains(entry.id)) continue;
        
        List<Link> currentLinks = new ArrayList<Link>();
          
        for(Link link : entry.links)
        {
          if(link.type.toLowerCase().contains("opds-catalog")) continue;
          if(!link.rel.toLowerCase().contains("open-access")) continue;            
          currentLinks.add(link);
        }
          
        if(currentLinks.isEmpty()) continue;
        
        processedEntries.add(entry.id);
          
        String entryId = entry.id;
        entryId = entryId.replace("tag:book:", "");

        // replace id with short URL id
        for (Link l : entry.links) {
          if (!l.href.startsWith("/b/")) continue;
          String[] linkParts = l.href.substring(1).split("/");
          if (linkParts.length < 2) continue;
          if (linkParts[1].length() == 0) continue;
          entryId = linkParts[1];
          break;
        }
          
        Entry newEntry = new Entry(entry.updated, entryId, entry.title, entry.author, entry.annotation);
        newEntry.links = currentLinks;
        entries.add(newEntry);
      }
    }
    
    return entries;
  }
  
  public static List<Page> downloadAuthorsSearch(String root, String search) throws IOException
  {
    List<Page> pages = crawler.downloadCatalog(root, "/opds" + search);
    List<Page> fullPages = new ArrayList<Page>();
    Set<String> processedEntries = new HashSet<String>();
      
    for(Page page : pages)
    {
      for(Entry entry : page.entries)
      {
        if(processedEntries.contains(entry.id)) continue;
        if(!entry.id.startsWith("tag:author:")) continue;
          
        String authorId = entry.id.replace("tag:author:", "");
          
        String authorBooksUrl = "/opds/author/" + authorId + "/alphabet";
          
        List<Page> catalogPages = crawler.downloadCatalog(root, authorBooksUrl);
        fullPages.addAll(catalogPages);
        processedEntries.add(entry.id);
      }
    }
    
    return fullPages;
  }
  
  public static List<Page> downloadBooksSearch(String root, String search) throws IOException
  {
    List<Page> pages = crawler.downloadCatalog(root, "/opds" + search);
    return pages;
  }
}
