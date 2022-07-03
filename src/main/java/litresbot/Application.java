package litresbot;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import litresbot.books.BookDownloader;
import litresbot.localisation.UserMessages;
import litresbot.localisation.UserMessagesRu;
import litresbot.telegram.TelegramBot;

public class Application
{
  public static final String packageName = Application.class.getPackage().getName();
  
  public static Boolean terminated = false;
  
  public static TelegramBotsApi telegram;
  
  // set the locale depending on the bot type
  public static UserMessages userMessages = new UserMessagesRu();

  final static Logger logger = Logger.getLogger(Application.class);
  
  public static void main(String[] args)
  {
    String version = AppProperties.versionProperties.getProperty("version");
    logger.info(packageName + " " + ((version == null) ? "(no version)" : version));
    
    terminated = false;

    DefaultBotOptions botOptions = new DefaultBotOptions();
    String botToken = AppProperties.getStringProperty("botToken");
    if(botToken == null) {
      logger.error("botToken is not defined. Unable to register bot.");
      return;
    }
    
    Boolean useProxy = AppProperties.getBooleanProperty("useProxy");
    if(useProxy == null) {
      useProxy = false;
    }

    if(useProxy)
    {
      String host = AppProperties.getStringProperty("proxyHost");
      Integer port = AppProperties.getIntProperty("proxyPort");
      String proxyType = AppProperties.getStringProperty("proxyType");

      if(host == null) {
        logger.error("proxyHost is not defined. Define it to proxy host or switch off the proxy.");
        return;
      }

      if(port == null) {
        logger.error("proxyPort is not defined. Define it to proxy port or switch off the proxy.");
        return;
      }

      if(proxyType == null) {
        logger.error("proxyType is not defined. Define it to proxy type (eg SOCKS5) or switch off the proxy.");
        return;
      }

      botOptions.setProxyHost(host);
      botOptions.setProxyPort(port);
      // default SOCKS5
      botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
      
      if(proxyType.compareToIgnoreCase("http") == 0)
      {
        botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
      }
      
      if(proxyType.compareToIgnoreCase("socks4") == 0)
      {
        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS4);
      } 
    }

    String flibustaDownloadPath = AppProperties.getStringProperty("flibustaDownloadPath");
    if(flibustaDownloadPath == null) {
      flibustaDownloadPath = "./tmp";
    }
    BookDownloader.setDownloadPath(flibustaDownloadPath);

    try {
      telegram = new TelegramBotsApi(DefaultBotSession.class);
      telegram.registerBot(new TelegramBot(botOptions));
      logger.info("Bot registered OK");
    } catch(TelegramApiException e) {
      logger.error("Could not register a bot", e);
      return;
    }
    
    try
    {
      while(true)
      {
        if(terminated) break;
        Thread.sleep(1000);
      }
    } 
    catch (InterruptedException e)
    {
      logger.warn(packageName + " terminated");
      terminated = true;
    }
    
    logger.warn(packageName + " stopped");
  }
}
