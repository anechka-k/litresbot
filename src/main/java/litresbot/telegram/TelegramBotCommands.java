package litresbot.telegram;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import litresbot.Application;
import litresbot.books.DownloadedBook;
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
    String[] args = normalCmd.split(" ");

    try
    {
      // default is search the book
      if (!cmd.startsWith("/"))
      {
        bookSearch(bot, update, cmd);
        return;
      }

      // special case for /bookinfo command encoding - it should not contain spaces
      if (normalCmd.startsWith("/b_") && normalCmd.length() > "/b_".length()) {
        args = new String[2];
        args[0] = "/bookinfo";
        args[1] = normalCmd.substring("/b_".length());
      } else if (normalCmd.startsWith("/bookinfo") && normalCmd.length() > "/bookinfo".length()) {
        args = new String[2];
        args[0] = "/bookinfo";
        args[1] = normalCmd.substring("/bookinfo".length());
      }
      
      String command = args[0];

      if (command.contentEquals("/start"))
      {
        bot.sendBusy(update);
        bot.sendReply(update, TelegramView.welcomeScreen());
        return;
      }
      
      if (command.contentEquals("/help"))
      {
        bot.sendBusy(update);
        bot.sendReply(update, TelegramView.helpScreen());
        return;
      }
      
      if(command.contentEquals("/book"))
      {
        // take the rest of the command as an argument since it may contain spaces
        String argument = cmd.substring("/book ".length()).trim();
        bookSearch(bot, update, argument);
        return;
      }
      
      if(command.contentEquals("/bookinfo"))
      {
        // should contain book id as an argument
        if (args.length < 2) {
          bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorBadCommand));
          return;
        }

        bot.sendBusy(update);
        SendMessageList reply = FlibustaClient.chooseBookAction(args[1]);      
        bot.sendReply(update, reply);
        return;
      }
      
      if(command.contentEquals("/format"))
      {
        // should contain book id as an argument
        if (args.length < 2) {
          bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorBadCommand));
          return;
        }

        bot.sendBusy(update);
        SendMessageList reply = FlibustaClient.chooseBookFormat(args[1]);
        bot.sendReply(update, reply);
        return;
      }
      
      if(command.contentEquals("/read"))
      {
        // should contain book id as an argument and optional read position
        if (args.length < 2) {
          bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorBadCommand));
          return;
        }

        bot.sendBusy(update);
        String bookId = args[1];
        int pageNumber = 0;

        if (args.length > 2) {
          String pageNumberString = args[2];
          try {
            pageNumber = Integer.parseInt(pageNumberString);
          } catch(Exception e) { }
        }
        
        // show the download message if it is first time book access
        if (pageNumber == 0) {
          bot.sendReply(update, TelegramView.downloadInProgress());
          bot.sendBusy(update);
        }

        SendMessageList reply = FlibustaClient.readBook(bookId, pageNumber);
        bot.sendReply(update, reply);
        return;
      }
      
      if(command.contentEquals("/download"))
      {
        // should contain book id and download format
        if (args.length < 3) {
          bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorBadCommand));
          return;
        }

        bot.sendReply(update, TelegramView.downloadInProgress());
        bot.sendBusy(update);

        String downloadFormat = args[1];
        String bookId = args[2];
        
        DownloadedBook book = null;
        try
        {
          book = FlibustaClient.download(bookId, downloadFormat);
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
        
        if(book.filename == null)
        {
          bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorWrongBookId));
          return;
        }

        bot.sendReply(update, TelegramView.downloadFinished());
        
        ByteArrayInputStream fileStream = new ByteArrayInputStream(book.content);
        InputFile fileMedia = new InputFile(fileStream, book.filename);
        SendDocument doc = new SendDocument();
        doc.setDocument(fileMedia);
        bot.sendFile(update, doc);
        return;
      }

      if(command.contentEquals("/next"))
      {
        // should contain search id and optional current page
        if (args.length < 2) {
          bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorBadCommand));
          return;
        }

        Integer searchId = null;
        try {
          searchId = Integer.parseInt(args[1]);
        } catch(NumberFormatException e) {
          bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorBadCommand));
          return;
        }

        int from = 0;

        if (args.length > 2) {
          String fromString = args[2];
          try {
            from = Integer.parseInt(fromString);
          } catch(Exception e) { }
        }

        bookSearchNext(bot, update, searchId, from);
        return;
      }
    }
    catch(Exception e) {
      bot.sendReply(update, Application.userMessages.get(UserMessagesEn.errorUnknown));
    }
  }

  private static void bookSearch(TelegramBot bot, Update update, String searchQuery)
  {
    bot.sendBusy(update);
    ///TODO: extract page size to AppConstants or AppProperties
    bot.sendReply(update, TelegramView.searchInProgress());
    SendMessageList reply = FlibustaClient.getBooks(searchQuery, 10);      
    bot.sendReply(update, reply);
  }

  private static void bookSearchNext(TelegramBot bot, Update update, int searchId, int currentPage)
  {
    bot.sendBusy(update);
    SendMessageList reply = FlibustaClient.getBooksById(searchId, currentPage, 10);
    bot.sendReply(update, reply);
  }
}
