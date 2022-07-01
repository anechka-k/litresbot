package litresbot;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.apache.log4j.Logger;

import litresbot.localisation.UserMessages;
import litresbot.localisation.UserMessagesRu;
import litresbot.telegram.TelegramBot;

public class Application
{
  public static final String packageName = Application.class.getPackage().getName();
  
  //public static final String version = AppConstants.APP_VERSION;
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

    Boolean telegramUseProxy = AppProperties.getBooleanProperty("telegramUseProxy");
    if(telegramUseProxy == null) {
      logger.warn("telegramUseProxy is not defined. Falling back to default (false) setting");
      telegramUseProxy = false;
    }

    String botToken = AppProperties.getStringProperty("botToken");
    if(botToken == null) {
      logger.error("botToken is not defined. Unable to register bot.");
      return;
    }
    
    if(telegramUseProxy)
    {
      String host = AppProperties.getStringProperty("telegramProxyHost");
      Integer port = AppProperties.getIntProperty("telegramProxyPort");
      String proxyType = AppProperties.getStringProperty("telegramProxyType");

      if(host == null) {
        logger.error("telegramProxyHost is not defined. Define it to proxy host or switch off the proxy.");
        return;
      }

      if(port == null) {
        logger.error("telegramProxyPort is not defined. Define it to proxy port or switch off the proxy.");
        return;
      }

      if(proxyType == null) {
        logger.error("telegramProxyType is not defined. Define it to proxy type (eg SOCKS5) or switch off the proxy.");
        return;
      }

      botOptions.setProxyHost(AppProperties.getStringProperty("telegramProxyHost"));
      botOptions.setProxyPort(AppProperties.getIntProperty("telegramProxyPort"));
      // default SOCKS5
      botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
      
      String proxyTypeString = AppProperties.getStringProperty("telegramProxyType");
      
      if(proxyTypeString.compareToIgnoreCase("http") == 0)
      {
        botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
      }
      
      if(proxyTypeString.compareToIgnoreCase("socks4") == 0)
      {
        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS4);
      } 
    }

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
