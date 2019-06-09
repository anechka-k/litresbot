package litresbot.localisation;

public class UserMessagesRu implements UserMessages
{
  public static final String errorUnknown = "����������� ������";
  public static final String errorWrongBookId = "�������� ����� �����";
  public static final String errorCouldNotDownloadFile = "�� ������� ������� ����";
  public static final String errorSearchNotFound = "� ��������� ������ �� �������";
  public static final String searchFoundTotal = "�������: ";
  public static final String bookText = "�����";
  public static final String searchGoto = "�������: ";
  public static final String searchDownload = "�������";
  public static final String searchRead = "������";
  
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
    return "������� �������� ����� ��� ������.";
  }
  
  public String helpScreen()
  {
    return 
        "������ ��������� ������:\n" +
        "/start - ������ ������ � �����.\n" +
        "/help - ������� �� ������ � �����.\n" +
        "/book - ����� �� �������� �����.";
  }
}
