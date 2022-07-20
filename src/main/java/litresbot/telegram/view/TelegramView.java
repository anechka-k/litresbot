package litresbot.telegram.view;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import litresbot.Application;
import litresbot.books.BookFileLink;
import litresbot.books.BookInfo;
import litresbot.books.plurals.PluralsTextRu;
import litresbot.books.plurals.PluralsTextEn;
import litresbot.localisation.UserMessagesEn;
import litresbot.telegram.SendMessageList;
import litresbot.telegram.TelegramEscape;

public class TelegramView
{
  public static String welcomeScreen()
  {
    return Application.userMessages.get(UserMessagesEn.welcomeScreen);
  }
  
  public static String helpScreen()
  {
    return
      Application.userMessages.get(UserMessagesEn.helpCommands) + "\n" +
      "/start - " + Application.userMessages.get(UserMessagesEn.helpStart) + "\n" +
      "/help - " + Application.userMessages.get(UserMessagesEn.helpHelp) + "\n" +
      "/book - " + Application.userMessages.get(UserMessagesEn.helpBook);
  }

  public static SendMessageList bookInfoNotFound() {
    SendMessageList result = new SendMessageList(4096);
    result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorSearchNotFound));
    result.endTextPage();
    return result;
  }

  public static SendMessageList bookCouldNotDownload() {
    SendMessageList result = new SendMessageList(4096);
    result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorCouldNotDownloadFile));
    result.endTextPage();
    return result;
  }

  public static SendMessageList searchInProgress() {
    SendMessageList result = new SendMessageList(4096);
    result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.searchInProgress));
    result.endTextPage();
    return result;
  }

  public static SendMessageList bookChooseFormat(BookInfo book) {
    SendMessageList result = new SendMessageList(4096);

    // generate the book info header
    
    result.appendTextPage("<b>");
    result.appendTextPage(book.title);
    result.appendTextPage("</b>\n");
        
    if (book.author != null)
    {
      result.appendTextPage(" (");
      result.appendTextPage(book.author);
      result.appendTextPage(")\n");
    }
    
    result.endTextPage();
    
    // generate keyboard with download formats

    List<InlineKeyboardButton> buttonsRow = new ArrayList<InlineKeyboardButton>();
    
    for(BookFileLink link : book.links)
    {
      InlineKeyboardButton btn1 = new InlineKeyboardButton();
      btn1.setText(link.format.toUpperCase());
      btn1.setCallbackData("/download " + link.format.toLowerCase() + " " + book.id);
      buttonsRow.add(btn1);
    }
    
    List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
    buttons.add(buttonsRow);
    result.appendButtons(buttons);
    
    return result;
  }

  public static SendMessageList bookChooseAction(BookInfo book)
  {
    SendMessageList result = new SendMessageList(4096);
        
    // generate the book info header
    
    result.appendTextPage("<b>");
    result.appendTextPage(book.title);
    result.appendTextPage("</b>\n");
        
    if (book.author != null)
    {
      result.appendTextPage(" (");
      result.appendTextPage(book.author);
      result.appendTextPage(")\n");
    }

    if (book.annotation != null) {
      result.appendTextPage("\n");
      result.appendTextPage(book.annotation);
      result.appendTextPage("\n");
    }
    
    result.endTextPage();
    
    // generate the book info download and read buttons
    
    List<InlineKeyboardButton> buttonsRow = new ArrayList<InlineKeyboardButton>();
    InlineKeyboardButton btn1 = new InlineKeyboardButton();
    InlineKeyboardButton btn2 = new InlineKeyboardButton();
    btn1.setText(litresbot.Application.userMessages.get(UserMessagesEn.searchDownload));
    btn1.setCallbackData("/format " + book.id);
    btn2.setText(litresbot.Application.userMessages.get(UserMessagesEn.searchRead));
    btn2.setCallbackData("/read " + book.id);
    
    buttonsRow.add(btn1);
    buttonsRow.add(btn2);
    
    List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
    buttons.add(buttonsRow);
    result.appendButtons(buttons);
    
    return result;
  }

  public static SendMessageList downloadInProgress() {
    SendMessageList result = new SendMessageList(4096);
    result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.downloadInProgress));
    result.endTextPage();
    return result;
  }

  public static SendMessageList downloadFinished() {
    SendMessageList result = new SendMessageList(4096);
    result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.downloadFinished));
    result.endTextPage();
    return result;
  }

  public static SendMessageList bookSearchResult(List<BookInfo> books, int searchId, int from, int pageSize, int found, String next)
  {
    SendMessageList result = new SendMessageList(4096);

    // generate the search result header - how much books found
    if (from == 0) {        
      String bookText = litresbot.Application.userMessages.get(UserMessagesEn.bookText);
      String booksText = PluralsTextEn.convert(bookText, found);
        
      if(litresbot.Application.userMessages.language().contentEquals("ru"))
      {
        booksText = PluralsTextRu.convert(bookText, found);
      }

      result.appendTextPage(
        litresbot.Application.userMessages.get(UserMessagesEn.searchFoundTotal) +
        found + " " + booksText + "\n\n");
      result.endTextPage();
    }
      
    // generate the search result body

    int bookNumber = from;
    for(BookInfo book : books)
    {
      bookNumber++;

      result.appendTextPage("<b>");
      result.appendTextPage("" + bookNumber + ". " + book.title);
      result.appendTextPage("</b>\n");
            
      if (book.author != null)
      {
        result.appendTextPage(" (");
        result.appendTextPage(book.author);
        result.appendTextPage(")\n");
      }

      result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.searchGoto));
      result.appendTextPage("/b_" + book.id);
              
      result.appendTextPage("\n\n");
      result.endTextPage();
    }

    // generate the book search next buttons
    
    List<InlineKeyboardButton> buttonsRow = new ArrayList<InlineKeyboardButton>();

    InlineKeyboardButton btn = new InlineKeyboardButton();
    btn.setText(litresbot.Application.userMessages.get(UserMessagesEn.endText));
    btn.setCallbackData("/");
    if (next != null) {
      btn.setText(litresbot.Application.userMessages.get(UserMessagesEn.nextText));
      btn.setCallbackData(next);
    }
    buttonsRow.add(btn);

    List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
    buttons.add(buttonsRow);
    result.appendButtons(buttons);
    return result;
  }

  public static SendMessageList readBookSection(SendMessageList output, String line, String next, int pageNumber)
  {
    String escapedLine = TelegramEscape.escapeText(line);
              
    output.appendTextPage(escapedLine + "\n\n");
    output.appendTextPage("------------------\n");
    output.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.pageNumberText) + pageNumber + "\n");
    output.endTextPage();

    // generate the book next page
    
    List<InlineKeyboardButton> buttonsRow = new ArrayList<InlineKeyboardButton>();

    InlineKeyboardButton btn = new InlineKeyboardButton();
    btn.setText(litresbot.Application.userMessages.get(UserMessagesEn.endText));
    btn.setCallbackData("/");
    if (next != null) {
      btn.setText(litresbot.Application.userMessages.get(UserMessagesEn.nextText));
      btn.setCallbackData(next);
    }
    buttonsRow.add(btn);

    List<List<InlineKeyboardButton>> buttons = new ArrayList<List<InlineKeyboardButton>>();
    buttons.add(buttonsRow);
    output.appendButtons(buttons);

    return output;
  }
}
