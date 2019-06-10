package litresbot.http;

import java.net.InetSocketAddress;
import java.net.Proxy;

import litresbot.AppProperties;

public class ProxySelector
{  
  public static Proxy select(HttpSourceType source)
  {
    Proxy proxy = Proxy.NO_PROXY;
    
    boolean useProxy = AppProperties.getBooleanProperty("telegramUseProxy");
    String proxyTypeString = AppProperties.getStringProperty("telegramProxyType");
    String proxyHost = AppProperties.getStringProperty("telegramProxyHost");
    int proxyPort = AppProperties.getIntProperty("telegramProxyPort");
    
    switch(source)
    {
      case TELEGRAM:
        break;
        
      case BOOKS:
        useProxy = AppProperties.getBooleanProperty("booksUseProxy");
        proxyTypeString = AppProperties.getStringProperty("booksProxyType");
        proxyHost = AppProperties.getStringProperty("booksProxyHost");
        proxyPort = AppProperties.getIntProperty("booksProxyPort");
        break;
        
      case OPDS_SERVICE:
        useProxy = AppProperties.getBooleanProperty("opdsSearchUseProxy");
        proxyTypeString = AppProperties.getStringProperty("opdsSearchProxyType");
        proxyHost = AppProperties.getStringProperty("opdsSearchProxyHost");
        proxyPort = AppProperties.getIntProperty("opdsSearchProxyPort");
        break;
        
      default:
        break;
    }
    
    if(useProxy)
    {
      Proxy.Type proxyType = Proxy.Type.SOCKS;
      
      if(proxyTypeString.compareToIgnoreCase("http") == 0)
      {
        proxyType = Proxy.Type.HTTP;
      }
      
      proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
    }
    
    return proxy;
  }
}
