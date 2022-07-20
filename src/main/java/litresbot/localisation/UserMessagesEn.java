package litresbot.localisation;

public class UserMessagesEn implements UserMessages
{
  public static final String welcomeScreen = "Type the book title for search.";
  public static final String helpCommands = "Available commands:";
  public static final String helpStart = "Start interacting with bot";
  public static final String helpHelp = "Bot usage info (this screen)";
  public static final String helpBook = "Search the book";
  public static final String errorUnknown = "Unknown error";
  public static final String errorWrongBookId = "Wrong book ID";
  public static final String errorCouldNotDownloadFile = "Could not download file";
  public static final String errorBadCommand = "Bad command";
  public static final String errorSearchNotFound = "Nothing found";
  public static final String searchInProgress = "Searching book... Please wait";
  public static final String searchFoundTotal = "Found: ";
  public static final String bookText = "book";
  public static final String searchGoto = "Download: ";
  public static final String searchDownload = "Download";
  public static final String downloadInProgress = "Downloading book... Please wait";
  public static final String downloadFinished = "Book downloaded";
  public static final String searchRead = "Read";
  public static final String previousText = "Previous";
  public static final String nextText = "Next";
  public static final String beginText = "Start";
  public static final String endText = "End";
  public static final String annotationEmpty = "No annotation";
  public static final String pageNumberText = "Page: ";
  
  public String language()
  {
    return "en";
  }
  
  public String get(String in)
  {
    return in;
  }
}
