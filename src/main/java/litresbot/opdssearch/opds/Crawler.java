package litresbot.opdssearch.opds;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import litresbot.http.HttpClientWithProxy;

public class Crawler
{
  public HttpClientWithProxy http;

  public Crawler() {
    http = new HttpClientWithProxy();
  }

  public Crawler(HttpClientWithProxy http_) {
    http = http_;
  }

  public Page downloadPage(String url) throws IOException
  {
    String reply = http.sendGetRequest(url);
      
    InputStream inputStream = new ByteArrayInputStream(reply.getBytes(Charset.forName("UTF-8")));
      
    Page page = PageParser.parse(inputStream);
    return page;
  }
  
  public List<Page> downloadCatalog(String root, String url) throws IOException
  {
    List<Page> pages = new ArrayList<Page>();
    Set<String> currentUrls = new HashSet<String>();
    Set<String> processedUrls = new HashSet<String>();
    
    currentUrls.add(url);
    
    while(true)
    {
      boolean found = false;
      Set<String> nextUrls = new HashSet<String>();
      
      for(String currentUrl : currentUrls)
      {
        if(processedUrls.contains(currentUrl)) continue;

        found = true;
        
        Page nextPage = downloadPage(root + currentUrl);
        pages.add(nextPage);
        
        processedUrls.add(currentUrl);
          
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
