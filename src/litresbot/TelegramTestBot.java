package litresbot;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import litresbot.flibusta.FlibustaClient;
import litresbot.util.Logger;

public class TelegramTestBot extends TelegramLongPollingBot
{
  protected TelegramTestBot(DefaultBotOptions botOptions)
  {
    super(botOptions);
  }
  
  private void sendReply(Update update, String res)
  {
    SendMessage message = new SendMessage()
            .setChatId(String.valueOf(update.getMessage().getChatId()))
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
  
  @SuppressWarnings("unused")
  private void sendReply(Update update, SendMessage res)
  {
    res.setChatId(String.valueOf(update.getMessage().getChatId()));
    try
    {
      execute(res);
    }
    catch (TelegramApiException e)
    {
      Logger.logMessage("Could not sendReply", e);
    }
  }

  private Message sendReply(Update update, SendMessageList res)
  {
    Message result = null;
    for (SendMessage sm : res.getMessages())
    {
      if (sm.getText() != null && sm.getText().length() > 0)
      {
        sm.setChatId(String.valueOf(update.getMessage().getChatId()));
        try
        {
          result = execute(sm);
        }
        catch (TelegramApiException e)
        {
          Logger.logMessage("Could not sendReply", e);
        }
      }
    }
    return result;
  }

  private Message sendFile(Update update, SendDocument res)
  {
    Message result = null;
    res.setChatId(update.getMessage().getChatId().toString());
    
    TelegramApiException sendException = null;
    try
    {
      result = execute(res);
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
    
    return result;
  }

  private void sendBusy(Update update)
  {
    SendChatAction sca = new SendChatAction();
    sca.setChatId(update.getMessage().getChatId().toString());
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
    if (!update.hasMessage()) return;
    if (!update.getMessage().hasText()) return;
    
    String cmd = update.getMessage().getText();
    if (!cmd.startsWith("/")) return;
    
    String userName = update.getMessage().getFrom().getUserName();
    Logger.logInfoMessage("onUpdate: " + cmd + ", " + userName);
    
    String languageCode = update.getMessage().getFrom().getLanguageCode();
    Logger.logInfoMessage("user has language: " + languageCode);
    
    if (cmd.startsWith("/h"))
    {
      sendBusy(update);
      sendReply(update, "Help message");
      return;
    }
    
    if(cmd.startsWith("/f"))
    {
      sendBusy(update);
      String argument = cmdArgument(cmd, "/f");
      
      SendDocument doc = new SendDocument();
      doc.setDocument(new File(argument));
      sendFile(update, doc);
      return;
    }
    
    if(cmd.startsWith("/b"))
    {
      sendBusy(update);
      String argument = cmdArgument(cmd, "/b");
      
      SendMessageList reply = FlibustaClient.getBooks(argument);      
      sendReply(update, reply);
      return;
    }
    
    if(cmd.startsWith("/d"))
    {
      sendBusy(update);
      String bookId = cmdArgument(cmd, "/d");
      String bookUrlShort = FlibustaClient.getUrlFromId(bookId);
      
      if(bookUrlShort == null)
      {
        sendReply(update, "/d: " + "Expired book ID");
        return;
      }
      
      String fileName = FlibustaClient.getFilenameFromId(bookId);
      
      if(fileName == null)
      {
        sendReply(update, "/d: " + "Expired book ID");
        return;
      }
      
      byte[] book = null;
      try
      {
        Logger.logInfoMessage("Downloading book: " + bookUrlShort);
        book = FlibustaClient.download(bookUrlShort, fileName);
        Logger.logInfoMessage("Downloading book done: " + bookUrlShort);
      }
      catch (IOException e)
      {
        Logger.logMessage("Could not download", e);
      }
      
      if(book == null)
      {
        sendReply(update, "/d: " + "Could not download file");
        return;
      }
      
      ByteArrayInputStream fileStream = new ByteArrayInputStream(book);
      
      InputFile fileMedia = new InputFile(fileStream, fileName);
      SendDocument doc = new SendDocument();
      doc.setDocument(fileMedia);
      sendFile(update, doc);
      return;
    }
  }

  private String cmdArgument(String cmd, String selector)
  {
    if(cmd.startsWith("/d_"))
    {
      cmd = "/d " + cmd.substring(3);
    }
    
    /*if(cmd.startsWith("/z_"))
    {
      cmd = "/z " + cmd.substring(3);
    }*/
    
    return cmd.substring(selector.length() + 1).trim();
  }
}
