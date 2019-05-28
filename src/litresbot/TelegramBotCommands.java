package litresbot;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import litresbot.flibusta.FlibustaClient;
import litresbot.util.Logger;

public class TelegramBotCommands
{
  public static void commandReceived(TelegramBot bot, Update update)
  {
    String cmd = "";
    String userName = "";
    String languageCode = "";
    
    if(update.hasCallbackQuery())
    {
      cmd = update.getCallbackQuery().getData();
      userName = update.getCallbackQuery().getFrom().getUserName();
      languageCode = update.getCallbackQuery().getFrom().getLanguageCode();
    }
    else
    {
      if (!update.hasMessage())
      {
        bot.sendReply(update, "����������� ������");
        return;
      }
      
      if (!update.getMessage().hasText())
      {
        bot.sendReply(update, "����������� ������");
        return;
      }
      
      cmd = update.getMessage().getText();
      userName = update.getMessage().getFrom().getUserName();
      languageCode = update.getMessage().getFrom().getLanguageCode();
    }
    
    if (cmd == null)
    {
      bot.sendReply(update, "����������� ������");
      return;
    }
    
    Logger.logInfoMessage("commandReceived: " + cmd + ", " + userName);
    Logger.logInfoMessage("user has language: " + languageCode);
    
    String normalCmd = cmd;
    normalCmd = normalCmd.toLowerCase();
    
    // default is search the book
    if (!cmd.startsWith("/"))
    {
      bookSearch(bot, update, cmd);
      return;
    }
    
    if (normalCmd.startsWith("/start"))
    {
      bot.sendBusy(update);
      bot.sendReply(update, welcomeScreen());
      return;
    }
    
    if (normalCmd.startsWith("/help"))
    {
      bot.sendBusy(update);
      bot.sendReply(update, helpScreen());
      return;
    }
    
    if(normalCmd.startsWith("/book "))
    {
      String argument = cmdArgument(cmd, "/book ");
      bookSearch(bot, update, argument);
      return;
    }
    
    if(normalCmd.startsWith("/bookinfo"))
    {
      bot.sendBusy(update);
      String bookId = cmdArgument(cmd, "/bookinfo");
      
      SendMessageList reply = FlibustaClient.getBookInfo(bookId);      
      bot.sendReply(update, reply);
      return;
    }
    
    if(normalCmd.startsWith("/format"))
    {
      bot.sendBusy(update);
      String bookId = cmdArgument(cmd, "/format");
      
      SendMessageList reply = FlibustaClient.chooseBookFormat(bookId);
      bot.sendReply(update, reply);
      return;
    }
    
    if(normalCmd.startsWith("/read"))
    {
      bot.sendBusy(update);
      String bookId = cmdArgument(cmd, "/read");
      
      SendMessageList reply = FlibustaClient.readBook(bookId);
      bot.sendReply(update, reply);
      return;
    }
    
    if(normalCmd.startsWith("/download"))
    {
      bot.sendBusy(update);
      
      // bookFileId is not a bookId. bookFileId is an id of the URL to download e-book file
      String bookFileId = cmdArgument(cmd, "/download");
      
      byte[] book = null;
      try
      {
        book = FlibustaClient.downloadWithCache(bookFileId);
      }
      catch (IOException e)
      {
        Logger.logMessage("Could not download", e);
      }
      
      if(book == null)
      {
        bot.sendReply(update, "�� ������� ������� ����");
        return;
      }
      
      ByteArrayInputStream fileStream = new ByteArrayInputStream(book);
      
      String fileName = FlibustaClient.getFilenameFromId(bookFileId);
      
      if(fileName == null)
      {
        bot.sendReply(update, "�������� ����� �����");
        return;
      }
      
      InputFile fileMedia = new InputFile(fileStream, fileName);
      SendDocument doc = new SendDocument();
      doc.setDocument(fileMedia);
      bot.sendFile(update, doc);
      return;
    }
  }
  
  public static void bookSearch(TelegramBot bot, Update update, String searchQuery)
  {
    bot.sendBusy(update);    
    SendMessageList reply = FlibustaClient.getBooks(searchQuery);      
    bot.sendReply(update, reply);
  }
  
  public static String welcomeScreen()
  {
    return "������� �������� ����� ��� ������.";
  }
  
  public static String helpScreen()
  {
    return 
      "������ ��������� ������:\n" +
      "/start - ������ ������ � �����.\n" +
      "/help - ������� �� ������ � �����.\n" +
      "/book - ����� �� �������� �����.";
  }

  private static String cmdArgument(String cmd, String prefix)
  {    
    return cmd.substring(prefix.length()).trim();
  }
}
