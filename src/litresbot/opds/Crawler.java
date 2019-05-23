package litresbot.opds;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import litresbot.HttpClientWithProxy;

public class Crawler
{
  public static List<Entry> downloadBooks(String root, String search, List<Entry> downloaded) throws IOException
  {
    List<Page> pages = downloadPages(root, search);
    List<Entry> entries = new ArrayList<Entry>();
    
    // prepare a check list to avoid duplicates
    Set<String> processedLinks = new HashSet<String>();
    for(Entry entry : downloaded)
    {
      for(Link link : entry.links)
      {
        if(link.type.toLowerCase().contains("opds-catalog")) continue;
        if(!link.rel.toLowerCase().contains("open-access")) continue;
        processedLinks.add(link.href);
      }
    }
    
    for(Page page : pages)
    {
      for(Entry entry : page.entries)
      {
        List<Link> currentLinks = new ArrayList<Link>();
        
        for(Link link : entry.links)
        {
          if(link.type.toLowerCase().contains("opds-catalog")) continue;
          if(!link.rel.toLowerCase().contains("open-access")) continue;
          if(processedLinks.contains(link.href)) continue;
          currentLinks.add(link);
          processedLinks.add(link.href);
        }
        
        if(currentLinks.isEmpty()) continue;
        
        Entry newEntry = new Entry(entry.updated, entry.id, entry.title, entry.author);
        newEntry.links = currentLinks;
        entries.add(newEntry);
      }
    }
    
    return entries;
  }
  
  public static Page downloadPage(String url) throws IOException
  {
    String reply = HttpClientWithProxy.doRequest(url);
      
    InputStream inputStream = new ByteArrayInputStream(reply.getBytes(Charset.forName("UTF-8")));
      
    Page page = PageParser.parse(inputStream);
    return page;
  }
  
  public static List<Page> downloadPages(String root, String search) throws IOException
  {
    List<Page> pages = new ArrayList<Page>();
    Set<String> currentUrls = new HashSet<String>();
    Set<String> processedUrls = new HashSet<String>();
    
    Page page = downloadPage(root + "/opds" + search);
    pages.add(page);
    
    // store the link to the next page in currentUrls
    page.links.stream()
      .filter((l) -> l.rel.equals("next"))
      .forEach(lnk ->
      {
        currentUrls.add(lnk.href);
      }
    );
    
    // store the catalog links in currentUrls
    for(Entry entry : page.entries)
    {
      entry.links.stream()
        .filter((l) -> l.type != null && l.type.toLowerCase().contains("opds-catalog"))
        .forEach(lnk ->
        {
          currentUrls.add(lnk.href);
        }
      );
    }
    
    while(true)
    {
      boolean found = false;
      Set<String> nextUrls = new HashSet<String>();
      
      for(String url : currentUrls)
      {
        if(processedUrls.contains(url)) continue;

        found = true;
        
        Page nextPage = downloadPage(root + url);
        pages.add(nextPage);
        
        processedUrls.add(url);
          
        nextPage.links.stream()
          .filter((l) -> l.rel.equals("next"))
          .forEach(lnk ->
          {
            nextUrls.add(lnk.href);
          }
        );
        
        for(Entry entry : nextPage.entries)
        {
          entry.links.stream()
            .filter((l) -> l.type != null && l.type.toLowerCase().contains("opds-catalog"))
            .forEach(lnk ->
            {
              nextUrls.add(lnk.href);
            }
          );
        }
      }
    
      currentUrls.clear();
      currentUrls.addAll(nextUrls);
      
      if(!found) break;
    }
    
    return pages;
  }
}
