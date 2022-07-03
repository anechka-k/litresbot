package litresbot.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import litresbot.AppProperties;

public class HttpClientWithProxy
{
  static class SocksFakeDnsResolver implements DnsResolver {
    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        // Return some fake DNS record for every request, we won't be using it
        return new InetAddress[] { InetAddress.getByAddress(new byte[] { 1, 1, 1, 1 }) };
    }
  }

  static class SocksConnectionSocketFactory extends PlainConnectionSocketFactory {
    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
        InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
        return new Socket(proxy);
    }

    @Override
    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
            InetSocketAddress localAddress, HttpContext context) throws IOException {
        // Convert address to unresolved
        InetSocketAddress unresolvedRemote = InetSocketAddress
                .createUnresolved(host.getHostName(), remoteAddress.getPort());
        return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
    }
  }

  static class SocksSSLConnectionSocketFactory extends SSLConnectionSocketFactory {

    public SocksSSLConnectionSocketFactory(final SSLContext sslContext) {
        // You may need this verifier if target site's certificate is not secure
        super(sslContext, NoopHostnameVerifier.INSTANCE);
    }

    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
        InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
        return new Socket(proxy);
    }

    @Override
    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
            InetSocketAddress localAddress, HttpContext context) throws IOException {
        // Convert address to unresolved
        InetSocketAddress unresolvedRemote = InetSocketAddress
                .createUnresolved(host.getHostName(), remoteAddress.getPort());
        return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
    }
  }

  public static String sendGetRequest(String url) throws IOException
  {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpClientContext context = HttpClientContext.create();
    HttpGet request = new HttpGet(url);
    request.setHeader("User-Agent", "PostmanRuntime/7.29.0");

    boolean useProxy = AppProperties.getBooleanProperty("useProxy");
    String proxyTypeString = AppProperties.getStringProperty("proxyType");
    String proxyHost = AppProperties.getStringProperty("proxyHost");
    int proxyPort = AppProperties.getIntProperty("proxyPort");

    if (useProxy) {      
      if (proxyTypeString.compareToIgnoreCase("http") == 0) {
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        RequestConfig config = RequestConfig.custom()
          .setProxy(proxy)
          .build();
        request.setConfig(config);
      } else if (proxyTypeString.compareToIgnoreCase("socks4") == 0 || proxyTypeString.compareToIgnoreCase("socks5") == 0){
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory> create()
            .register("http", new SocksConnectionSocketFactory())
            .register("https", new SocksSSLConnectionSocketFactory(SSLContexts.createSystemDefault())).build();
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg, new SocksFakeDnsResolver());

        InetSocketAddress socksaddr = new InetSocketAddress(proxyHost, proxyPort);
        context.setAttribute("socks.address", socksaddr);

        httpclient = HttpClients.custom()
          .setConnectionManager(cm)
          .build();
      } else {
        throw new IOException("Unsupported proxy type: " + proxyTypeString);
      }
    }
      
    try {
        CloseableHttpResponse response = httpclient.execute(request, context);
        int status = response.getStatusLine().getStatusCode();
        if(status != 200) {
          throw new IOException("Got status: " + status);
        }

        try {
            return EntityUtils.toString(response.getEntity());
        } finally {
            response.close();
        }
    } finally {
        httpclient.close();
    }
  }
  
  public static InputStream getDownloadStream(String url) throws IOException
  {
    HttpURLConnection httpUrlConnection = openRawConnection(url);
    return httpUrlConnection.getInputStream();
  }
  
  private static HttpURLConnection openRawConnection(String url) throws IOException
  {
    URL fullUrl = new URL(url);
    Proxy proxy = Proxy.NO_PROXY;
    
    boolean useProxy = AppProperties.getBooleanProperty("useProxy");
    String proxyTypeString = AppProperties.getStringProperty("proxyType");
    String proxyHost = AppProperties.getStringProperty("proxyHost");
    int proxyPort = AppProperties.getIntProperty("proxyPort");

    if(useProxy)
    {
      Proxy.Type proxyType = Proxy.Type.SOCKS;
      
      if(proxyTypeString.compareToIgnoreCase("http") == 0)
      {
        proxyType = Proxy.Type.HTTP;
      }
      
      proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
    }
     
    HttpURLConnection httpUrlConnection = (HttpURLConnection) fullUrl.openConnection(proxy);
    httpUrlConnection.setRequestProperty("content-type", "application/json;  charset=utf-8");
    return httpUrlConnection;
  }
}
