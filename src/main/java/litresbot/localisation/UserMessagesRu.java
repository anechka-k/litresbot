package litresbot.localisation;

import java.util.HashMap;
import java.util.Map;

public class UserMessagesRu implements UserMessages
{
  public Map<String, String> translation = createMap();

  private static Map<String, String> createMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put(UserMessagesEn.welcomeScreen, "Введите название книги для поиска.");
    map.put(UserMessagesEn.helpCommands, "Список доступных команд:");
    map.put(UserMessagesEn.helpStart, "начало работы с ботом");
    map.put(UserMessagesEn.helpHelp, "справка по работе с ботом");
    map.put(UserMessagesEn.helpBook, "поиск по названию книги");
    map.put(UserMessagesEn.errorUnknown, "Неизвестная ошибка");
    map.put(UserMessagesEn.errorWrongBookId, "Неверный ID книги");
    map.put(UserMessagesEn.errorCouldNotDownloadFile, "Не получилось загрузить файл");
    map.put(UserMessagesEn.errorBadCommand, "Неверная команда");
    map.put(UserMessagesEn.errorSearchNotFound, "Ничего не найдено");
    map.put(UserMessagesEn.searchInProgress, "Поиск книги...");
    map.put(UserMessagesEn.searchFoundTotal, "Найдено: ");
    map.put(UserMessagesEn.bookText, "книга");
    map.put(UserMessagesEn.searchGoto, "Загрузить: ");
    map.put(UserMessagesEn.searchDownload, "Загрузить");
    map.put(UserMessagesEn.downloadInProgress, "Загружаю книгу...");
    map.put(UserMessagesEn.downloadFinished, "Книга загружена");
    map.put(UserMessagesEn.searchRead, "Читать");
    map.put(UserMessagesEn.previousText, "Назад");
    map.put(UserMessagesEn.nextText, "Дальше");
    map.put(UserMessagesEn.beginText, "Начало");
    map.put(UserMessagesEn.endText, "Конец");
    map.put(UserMessagesEn.annotationEmpty, "Нет аннотации");
    map.put(UserMessagesEn.pageNumberText, "Страница: ");
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
