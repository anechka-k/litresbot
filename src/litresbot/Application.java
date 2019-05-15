package litresbot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import litresbot.util.Logger;

public class Application
{
  public static final String packageName = Application.class.getPackage().getName();
  
  public static final String version = AppConstants.APP_VERSION;
  public static Boolean terminated = false;
  
  private static TelegramBotsApi telegram;
  
  public static void main(String[] args)
  {
    Logger.logMessage(packageName + " " + version);
    
    terminated = false;
    
    ApiContextInitializer.init();
    DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
    telegram = new TelegramBotsApi();
    
    boolean useProxy = AppProperties.getBooleanProperty("useProxy");
    
    if(useProxy)
    {
      botOptions.setProxyHost(AppProperties.getStringProperty("proxyHost"));
      botOptions.setProxyPort(AppProperties.getIntProperty("proxyPort"));
      // default SOCKS5
      botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
      
      String proxyTypeString = AppProperties.getStringProperty("proxyType");
      
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
