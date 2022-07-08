package litresbot.telegram;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import litresbot.Application;
import litresbot.books.FileExtensions;
import litresbot.flibusta.FlibustaClient;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.view.TelegramView;

public class TelegramBotCommands
{
  final static Logger logger = Logger.getLogger(TelegramBotCommands.class);

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
        bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorUnknown));
        return;
      }
      
      if (!update.getMessage().hasText())
      {
        bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorUnknown));
        return;
      }
      
      cmd = update.getMessage().getText();
      userName = update.getMessage().getFrom().getUserName();
      languageCode = update.getMessage().getFrom().getLanguageCode();
    }
    
    if (cmd == null)
    {
      bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorUnknown));
      return;
    }
    
    logger.debug("commandReceived: " + StringEscapeUtils.escapeJava(cmd) + ", " + StringEscapeUtils.escapeJava(userName));
    logger.debug("user has language: " + StringEscapeUtils.escapeJava(languageCode));
    
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
      bot.sendReply(update, TelegramView.welcomeScreen());
      return;
    }
    
    if (normalCmd.startsWith("/help"))
    {
      bot.sendBusy(update);
      bot.sendReply(update, TelegramView.helpScreen());
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
      
      SendMessageList reply = FlibustaClient.chooseBookAction(bookId);      
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
      String readArgument = cmdArgument(cmd, "/read");
      String bookId = detectBookFromReadArgument(readArgument);
      
      if(bookId == null)
      {
        bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorWrongBookId));
        return;
      }
      
      String readPositionString = readArgument.substring(bookId.length() + 1);
      
      Long readPosition = 0L;
      try
      {
        readPosition = Long.parseLong(readPositionString);
      }
      catch(Exception e)
      { 
      }
      
      SendMessageList reply = FlibustaClient.readBook(bookId, readPosition);
      bot.sendReply(update, reply);
      return;
    }
    
    if(normalCmd.startsWith("/download"))
    {
      bot.sendBusy(update);
      
      // bookFileId is not a bookId. bookFileId is an id of the URL to download e-book file
      String downloadArguments = cmdArgument(cmd, "/download");
      String downloadFormat = FileExtensions.detectFormat(downloadArguments);
      
      if(downloadFormat == null)
      {
        bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorWrongBookId));
        return;
      }
      
      String bookId = downloadArguments.substring(downloadFormat.length());
      
      byte[] book = null;
      try
      {
        book = FlibustaClient.downloadWithCache(bookId, downloadFormat);
      }
      catch (IOException e)
      {
        logger.warn("Could not download", e);
      }
      
      if(book == null)
      {
        bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorCouldNotDownloadFile));
        return;
      }
      
      ByteArrayInputStream fileStream = new ByteArrayInputStream(book);
      
      String fileName = FlibustaClient.getDownloadFileName(bookId, downloadFormat);
      
      if(fileName == null)
      {
        bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorWrongBookId));
        return;
      }
      
      InputFile fileMedia = new InputFile(fileStream, fileName);
      SendDocument doc = new SendDocument();
      doc.setDocument(fileMedia);
      bot.sendFile(update, doc);
      return;
    }

    if(normalCmd.startsWith("/searchnext "))
    {
      String argument = cmdArgument(cmd, "/searchnext ");
      bookSearchNext(bot, update, argument);
      return;
    }
  }

  private static String detectBookFromReadArgument(String readArgument)
  {
    int posIndex = readArgument.indexOf("p");
    
    if(posIndex < 0)
    {
      return null;
    }
    
    String bookId = readArgument.substring(0, posIndex);
    return bookId;
  }

  private static void bookSearch(TelegramBot bot, Update update, String searchQuery)
  {
    bot.sendBusy(update);    
    SendMessageList reply = FlibustaClient.getBooks(searchQuery, 0, 10);      
    bot.sendReply(update, reply);
  }

  private static void bookSearchNext(TelegramBot bot, Update update, String searchQuery)
  {
    bot.sendBusy(update);    
    String[] args = searchQuery.split(" ");
    int from = Integer.parseInt(args[1]);
    SendMessageList reply = FlibustaClient.getBooks(args[0], from, 10);
    bot.sendReply(update, reply);
  }

  private static String cmdArgument(String cmd, String prefix)
  {    
    return cmd.substring(prefix.length()).trim();
  }
}
