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
    
    page.links.stream()
      .filter((l) -> l.rel.equals("next"))
      .forEach(lnk ->
      {
        currentUrls.add(lnk.href);
      }
    );
    
    while(true)
    {
      boolean found = false;
      Set<String> nextUrls = new HashSet<String>();
      
      for(String entry : currentUrls)
      {
        if(processedUrls.contains(entry)) continue;

        found = true;
        
        Page nextPage = downloadPage(root + entry);
        pages.add(nextPage);
        processedUrls.add(entry);
          
        nextPage.links.stream()
          .filter((l) -> l.rel.equals("next"))
          .forEach(lnk ->
          {
            nextUrls.add(lnk.href);
          }
        );
      }
    
      currentUrls.clear();
      currentUrls.addAll(nextUrls);
      
      if(!found) break;
    }
    
    return pages;
  }
}
