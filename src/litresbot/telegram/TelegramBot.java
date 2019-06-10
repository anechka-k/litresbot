package litresbot.telegram;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.AppProperties;
import litresbot.util.Logger;

public class TelegramBot extends TelegramLongPollingBot
{
  public TelegramBot(DefaultBotOptions botOptions)
  {
    super(botOptions);
  }
  
  private static Message getChatMessage(Update update)
  {
    Message chatMessage = update.getMessage();
    
    if(update.hasCallbackQuery())
    {
      chatMessage = update.getCallbackQuery().getMessage();
    }
    
    return chatMessage;
  }
  
  public void sendReply(Update update, String res)
  {
    Message chatMessage = getChatMessage(update);
        
    if(chatMessage == null)
    {
      Logger.logMessage("Could not sendReply");
      return;
    }
    
    SendMessage message = new SendMessage()
            .setChatId(String.valueOf(chatMessage.getChatId()))
            .setText(res)
            .enableHtml(true);
    try
    {
      execute(message);
    }
    catch (TelegramApiException e)
    {
      Logger.logMessage("Could not sendReply", e);
    }
  }
  
  public void sendReply(Update update, SendMessage res)
  {
    Message chatMessage = getChatMessage(update);
    
    if(chatMessage == null)
    {
      Logger.logMessage("Could not sendReply");
      return;
    }
    
    res.setChatId(String.valueOf(chatMessage.getChatId()));
    try
    {
      execute(res);
    }
    catch (TelegramApiException e)
    {
      Logger.logMessage("Could not sendReply", e);
    }
  }

  public void sendReply(Update update, SendMessageList res)
  {
    Message chatMessage = getChatMessage(update);
    
    if(chatMessage == null)
    {
      Logger.logMessage("Could not sendReply");
      return;
    }
    
    for (SendMessage sm : res.getMessages())
    {
      if (sm.getText() != null && sm.getText().length() > 0)
      {
        sm.setChatId(String.valueOf(chatMessage.getChatId()));
        try
        {
          execute(sm);
        }
        catch (TelegramApiException e)
        {
          Logger.logMessage("Could not sendReply", e);
        }
      }
    }
    return;
  }

  public void sendFile(Update update, SendDocument res)
  {
    Message chatMessage = getChatMessage(update);
    
    if(chatMessage == null)
    {
      Logger.logMessage("Could not sendReply");
      return;
    }
    
    res.setChatId(chatMessage.getChatId().toString());
    
    TelegramApiException sendException = null;
    try
    {
      execute(res);
    }
    catch (TelegramApiException e)
    {
      Logger.logMessage("Could not sendFile", e);
      sendException = e;
    }
    
    if(sendException != null)
    {
      sendReply(update, sendException.getMessage());
    }
    
    return;
  }

  public void sendBusy(Update update)
  {
    Message chatMessage = getChatMessage(update);
    
    if(chatMessage == null)
    {
      Logger.logMessage("Could not sendReply");
      return;
    }
    
    SendChatAction sca = new SendChatAction();
    sca.setChatId(chatMessage.getChatId().toString());
    sca.setAction(ActionType.UPLOADDOCUMENT);
    try
    {
      execute(sca);
    }
    catch (TelegramApiException e)
    {
      Logger.logMessage("Could not sendBusy", e);
    }
  }
  
  @Override
  public String getBotUsername()
  {
    return AppProperties.getStringProperty("botName");
  }

  @Override
  public String getBotToken()
  {
    return AppProperties.getStringProperty("botToken");
  }
  
  @Override
  public void onUpdateReceived(Update update)
  {    
    TelegramBotCommands.commandReceived(this, update);
  }
}
