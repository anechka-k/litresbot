package litresbot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import litresbot.localisation.UserMessages;
import litresbot.localisation.UserMessagesRu;
import litresbot.telegram.TelegramBot;
import litresbot.util.Logger;

public class Application
{
  public static final String packageName = Application.class.getPackage().getName();
  
  public static final String version = AppConstants.APP_VERSION;
  public static Boolean terminated = false;
  
  private static TelegramBotsApi telegram;
  
  // set the locale depending on the bot type
  public static UserMessages userMessages = new UserMessagesRu();
  
  public static void main(String[] args)
  {
    Logger.logMessage(packageName + " " + version);
    
    terminated = false;
    
    ApiContextInitializer.init();
    DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
    telegram = new TelegramBotsApi();
    
    boolean telegramUseProxy = AppProperties.getBooleanProperty("telegramUseProxy");
    
    if(telegramUseProxy)
    {
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
    
    try
    {
      telegram.registerBot(new TelegramBot(botOptions));
      Logger.logInfoMessage("Bot registered OK");
    }
    catch (Exception e)
    {
      Logger.logMessage("Could not register bot", e);
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
      Logger.logWarningMessage(packageName + " terminated");
      terminated = true;
    }
    
    Logger.logWarningMessage(packageName + " stopped");
  }
}
