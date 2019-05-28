package litresbot;

import java.io.ByteArrayInputStream;
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

public class TelegramBot extends TelegramLongPollingBot
{
  protected TelegramBot(DefaultBotOptions botOptions)
  {
    super(botOptions);
  }
  
  private Message getChatMessage(Update update)
  {
    Message chatMessage = update.getMessage();
    
    if(update.hasCallbackQuery())
    {
      chatMessage = update.getCallbackQuery().getMessage();
    }
    
    return chatMessage;
  }
  
  private void sendReply(Update update, String res)
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
  
  @SuppressWarnings("unused")
  private void sendReply(Update update, SendMessage res)
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

  private Message sendReply(Update update, SendMessageList res)
  {
    Message result = null;
    Message chatMessage = getChatMessage(update);
    
    if(chatMessage == null)
    {
      Logger.logMessage("Could not sendReply");
      return result;
    }
    
    for (SendMessage sm : res.getMessages())
    {
      if (sm.getText() != null && sm.getText().length() > 0)
      {
        sm.setChatId(String.valueOf(chatMessage.getChatId()));
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
    Message chatMessage = getChatMessage(update);
    
    if(chatMessage == null)
    {
      Logger.logMessage("Could not sendReply");
      return result;
    }
    
    res.setChatId(chatMessage.getChatId().toString());
    
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
    if(update.hasCallbackQuery())
    {
      onCallbackQueryReceived(update);
      return;
    }
    
    if (!update.hasMessage()) return;
    if (!update.getMessage().hasText()) return;
    
    String cmd = update.getMessage().getText();
    
    String userName = update.getMessage().getFrom().getUserName();
    Logger.logInfoMessage("onUpdate: " + cmd + ", " + userName);
    
    String languageCode = update.getMessage().getFrom().getLanguageCode();
    Logger.logInfoMessage("user has language: " + languageCode);
    
    String normalCmd = cmd;
    normalCmd = normalCmd.toLowerCase();
    
    // default is search the book
    if (!cmd.startsWith("/"))
    {
      bookSearch(update, cmd);
      return;
    }
    
    if (normalCmd.startsWith("/start"))
    {
      sendBusy(update);
      sendReply(update, welcomeScreen());
      return;
    }
    
    if (normalCmd.startsWith("/help"))
    {
      sendBusy(update);
      sendReply(update, helpScreen());
      return;
    }
    
    if(normalCmd.startsWith("/book "))
    {
      String argument = cmdArgument(cmd, "/book ");
      bookSearch(update, argument);
      return;
    }
    
    if(normalCmd.startsWith("/bookinfo"))
    {
      String argument = cmdArgument(cmd, "/bookinfo");
      sendBusy(update);    
      SendMessageList reply = FlibustaClient.getBookInfo(argument);      
      sendReply(update, reply);
      return;
    }
    
    if(normalCmd.startsWith("/format"))
    {
      String argument = cmdArgument(cmd, "/format");
      sendBusy(update);    
      SendMessageList reply = FlibustaClient.chooseBookFormat(argument);      
      sendReply(update, reply);
      return;
    }
    
    if(normalCmd.startsWith("/read"))
    {
      String argument = cmdArgument(cmd, "/read");
      sendBusy(update);    
      SendMessageList reply = FlibustaClient.readBook(argument);      
      sendReply(update, reply);
      return;
    }
    
    if(normalCmd.startsWith("/download"))
    {
      sendBusy(update);
      String bookId = cmdArgument(cmd, "/download");
      
      byte[] book = null;
      try
      {
        book = FlibustaClient.downloadWithCache(bookId);
      }
      catch (IOException e)
      {
        Logger.logMessage("Could not download", e);
      }
      
      if(book == null)
      {
        sendReply(update, "Не удалось скачать файл");
        return;
      }
      
      ByteArrayInputStream fileStream = new ByteArrayInputStream(book);
      
      String fileName = FlibustaClient.getFilenameFromId(bookId);
      
      if(fileName == null)
      {
        sendReply(update, "Неверный номер книги");
        return;
      }
      
      InputFile fileMedia = new InputFile(fileStream, fileName);
      SendDocument doc = new SendDocument();
      doc.setDocument(fileMedia);
      sendFile(update, doc);
      return;
    }
  }

  private void onCallbackQueryReceived(Update update)
  {    
    String cmd = update.getCallbackQuery().getData();
    
    String userName = update.getCallbackQuery().getFrom().getUserName();
    Logger.logInfoMessage("onCallback: " + cmd + ", " + userName);
    
    String languageCode = update.getCallbackQuery().getFrom().getLanguageCode();
    Logger.logInfoMessage("user has language: " + languageCode);
    
    String normalCmd = cmd;
    normalCmd = normalCmd.toLowerCase();
    
    if(!cmd.startsWith("/"))
    {
      sendReply(update, "Неверная команда");
      return;
    }
    
    if(normalCmd.startsWith("/read"))
    {
      String argument = cmdArgument(cmd, "/read");
      sendBusy(update);    
      SendMessageList reply = FlibustaClient.readBook(argument);      
      sendReply(update, reply);
      return;
    }
    
    if(normalCmd.startsWith("/format"))
    {
      String argument = cmdArgument(cmd, "/format");
      sendBusy(update);    
      SendMessageList reply = FlibustaClient.chooseBookFormat(argument);      
      sendReply(update, reply);
      return;
    }
    
    if(normalCmd.startsWith("/download"))
    {
      sendBusy(update);
      String bookId = cmdArgument(cmd, "/download");
      
      byte[] book = null;
      try
      {
        book = FlibustaClient.downloadWithCache(bookId);
      }
      catch (IOException e)
      {
        Logger.logMessage("Could not download", e);
      }
      
      if(book == null)
      {
        sendReply(update, "Не удалось скачать файл");
        return;
      }
      
      ByteArrayInputStream fileStream = new ByteArrayInputStream(book);
      
      String fileName = FlibustaClient.getFilenameFromId(bookId);
      
      if(fileName == null)
      {
        sendReply(update, "Неверный номер книги");
        return;
      }
      
      InputFile fileMedia = new InputFile(fileStream, fileName);
      SendDocument doc = new SendDocument();
      doc.setDocument(fileMedia);
      sendFile(update, doc);
      return;
    }
    
  }

  private void bookSearch(Update update, String searchQuery)
  {
    sendBusy(update);    
    SendMessageList reply = FlibustaClient.getBooks(searchQuery);      
    sendReply(update, reply);
  }

  private String cmdArgument(String cmd, String prefix)
  {    
    return cmd.substring(prefix.length()).trim();
  }
  
  private String welcomeScreen()
  {
    return "Введите название книги для поиска.";
  }
  
  private String helpScreen()
  {
    return 
      "Список доступных команд:\n" +
      "/start - начало работы с ботом.\n" +
      "/help - справка по работе с ботом.\n" +
      "/book - поиск по названию книги.";
  }
}
