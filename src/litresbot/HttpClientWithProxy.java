package litresbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class HttpClientWithProxy
{  
  public static String doRequest(String url) throws IOException
  {
    HttpURLConnection httpUrlConnection = openConnection(url);
    int status = httpUrlConnection.getResponseCode();
    
    if(status != 200)
    {
      throw new IOException("Got status: " + status);
    }
    
    InputStream response = httpUrlConnection.getInputStream();
      
    BufferedReader in = new BufferedReader(new InputStreamReader(response, "UTF-8"));
      
    String line = null;
    String reply = "";
    while((line = in.readLine()) != null)
    {
      reply += line;
    }

    return reply;
  }
  
  public static InputStream download(String url, String fileName) throws IOException
  {
    HttpURLConnection httpUrlConnection = openConnection(url);
    return httpUrlConnection.getInputStream();
  }
  
  private static HttpURLConnection openConnection(String url) throws IOException
  {
    URL fullUrl = new URL(url);        
    Proxy proxy = Proxy.NO_PROXY;
    
    boolean useProxy = AppProperties.getBooleanProperty("useProxy");
    
    if(useProxy)
    {
      String proxyTypeString = AppProperties.getStringProperty("proxyType");
      Proxy.Type proxyType = Proxy.Type.SOCKS;
      
      if(proxyTypeString.compareToIgnoreCase("http") == 0)
      {
        proxyType = Proxy.Type.HTTP;
      }
      
      proxy = new Proxy(proxyType, new InetSocketAddress(AppProperties.getStringProperty("proxyHost"), AppProperties.getIntProperty("proxyPort")));
    }
    
    HttpURLConnection httpUrlConnection = (HttpURLConnection) fullUrl.openConnection(proxy);
    return httpUrlConnection;
  }
}
