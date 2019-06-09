package litresbot.localisation;

public class UserMessagesEn implements UserMessages
{
  public static final String errorUnknown = "Unknown error";
  public static final String errorWrongBookId = "Wrong book ID";
  public static final String errorCouldNotDownloadFile = "Could not download file";
  public static final String errorSearchNotFound = "Nothing found";
  public static final String searchFoundTotal = "Found: ";
  public static final String bookText = "book";
  public static final String searchGoto = "Download: ";
  public static final String searchDownload = "Download";
  public static final String searchRead = "Read";
  
  public String language()
  {
    return "en";
  }
  
  public String get(String in)
  {
    return in;
  }
  
  public String welcomeScreen()
  {
    return "Type the book title for search.";
  }
  
  public String helpScreen()
  {
    return 
        "Available commands:\n" +
        "/start - start interacting with bot.\n" +
        "/help - bot usage info (this screen).\n" +
        "/book - search the book.";
  }
}
