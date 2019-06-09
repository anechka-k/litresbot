package litresbot.localisation;

public class UserMessagesRu implements UserMessages
{
  public static final String errorUnknown = "Неизвестная ошибка";
  public static final String errorWrongBookId = "Неверный номер книги";
  public static final String errorCouldNotDownloadFile = "Не удалось скачать файл";
  public static final String errorSearchNotFound = "К сожалению ничего не найдено";
  public static final String searchFoundTotal = "Найдено: ";
  public static final String bookText = "книга";
  public static final String searchGoto = "Скачать: ";
  public static final String searchDownload = "Скачать";
  public static final String searchRead = "Читать";
  
  public String language()
  {
    return "ru";
  }
  
  public String get(String in)
  {
    if(in.contentEquals(UserMessagesEn.errorUnknown))
    {
      return errorUnknown;
    }
    
    if(in.contentEquals(UserMessagesEn.errorWrongBookId))
    {
      return errorWrongBookId;
    }
    
    if(in.contentEquals(UserMessagesEn.errorCouldNotDownloadFile))
    {
      return errorCouldNotDownloadFile;
    }
    
    if(in.contentEquals(UserMessagesEn.errorSearchNotFound))
    {
      return errorSearchNotFound;
    }
    
    if(in.contentEquals(UserMessagesEn.searchFoundTotal))
    {
      return searchFoundTotal;
    }
    
    if(in.contentEquals(UserMessagesEn.bookText))
    {
      return bookText;
    }
    
    if(in.contentEquals(UserMessagesEn.searchGoto))
    {
      return searchGoto;
    }
    
    if(in.contentEquals(UserMessagesEn.searchDownload))
    {
      return searchDownload;
    }
    
    if(in.contentEquals(UserMessagesEn.searchRead))
    {
      return searchRead;
    }
    
    return in;
  }
  
  public String welcomeScreen()
  {
    return "Введите название книги для поиска.";
  }
  
  public String helpScreen()
  {
    return 
        "Список доступных команд:\n" +
        "/start - начало работы с ботом.\n" +
        "/help - справка по работе с ботом.\n" +
        "/book - поиск по названию книги.";
  }
}
