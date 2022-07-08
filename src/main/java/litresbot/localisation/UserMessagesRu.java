package litresbot.localisation;

import java.util.HashMap;
import java.util.Map;

public class UserMessagesRu implements UserMessages
{
  public Map<String, String> translation = createMap();

  private static Map<String, String> createMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put(UserMessagesEn.errorUnknown, "Неизвестная ошибка");
    map.put(UserMessagesEn.errorWrongBookId, "Неверный ID книги");
    map.put(UserMessagesEn.errorCouldNotDownloadFile, "Не получилось загрузить файл");
    map.put(UserMessagesEn.errorSearchNotFound, "Ничего не найдено");
    map.put(UserMessagesEn.searchFoundTotal, "Найдено: ");
    map.put(UserMessagesEn.bookText, "книга");
    map.put(UserMessagesEn.searchGoto, "Загрузить: ");
    map.put(UserMessagesEn.searchDownload, "Загрузить");
    map.put(UserMessagesEn.searchRead, "Читать");
    map.put(UserMessagesEn.nextText, "Следующие");
    map.put(UserMessagesEn.welcomeScreen, "Введите название книги для поиска.");
    map.put(UserMessagesEn.helpCommands, "Список доступных команд:");
    map.put(UserMessagesEn.helpStart, "начало работы с ботом");
    map.put(UserMessagesEn.helpHelp, "справка по работе с ботом");
    map.put(UserMessagesEn.helpBook, "поиск по названию книги");
    return map;
  }
  
  public String language()
  {
    return "ru";
  }
  
  public String get(String in)
  {
    return translation.get(in);
  }
}
