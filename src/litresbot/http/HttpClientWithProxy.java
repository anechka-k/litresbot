package litresbot.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpClientWithProxy
{  
  public static String sendGetRequest(String url, HttpSourceType source) throws IOException
  {
    HttpURLConnection httpUrlConnection = openConnection(url, source);
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
  
  public static String sendPostRequest(String url, String postData, HttpSourceType source) throws IOException
  {
    byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
    
    HttpURLConnection httpUrlConnection = openConnection(url, source);
    httpUrlConnection.setDoOutput(true);
    httpUrlConnection.setInstanceFollowRedirects(false);
    httpUrlConnection.setRequestMethod("POST");
    httpUrlConnection.setRequestProperty("Content-Type", "application/json"); 
    httpUrlConnection.setRequestProperty("charset", "utf-8");
    httpUrlConnection.setRequestProperty("Content-Length", Integer.toString(postDataBytes.length));
    httpUrlConnection.setUseCaches(false);
    
    try( DataOutputStream wr = new DataOutputStream( httpUrlConnection.getOutputStream()))
    {
      wr.write(postDataBytes);
    }
    
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
  
  public static InputStream download(String url, String fileName, HttpSourceType source) throws IOException
  {
    HttpURLConnection httpUrlConnection = openConnection(url, source);
    return httpUrlConnection.getInputStream();
  }
  
  private static HttpURLConnection openConnection(String url, HttpSourceType source) throws IOException
  {
    URL fullUrl = new URL(url);
    Proxy proxy = ProxySelector.select(source);    
    HttpURLConnection httpUrlConnection = (HttpURLConnection) fullUrl.openConnection(proxy);
    return httpUrlConnection;
  }
}
