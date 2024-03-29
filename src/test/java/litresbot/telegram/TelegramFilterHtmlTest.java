package litresbot.telegram;

import org.junit.Test;
import org.junit.Assert;

public class TelegramFilterHtmlTest {
  String htmlText =
    "<p class=\"book\">Миллион книг <b>рассказывают</b>, как заработать миллион. Рисковать всем и любить работать хотя бы по 16 часо" +
    "в в день. Я не готов рисковать <i>всем</i> и не очень люблю работать. Если вы по той же причине до сих пор не заработали миллио" +
    "н, попробую научить вас зарабатывать для начала 100–200 тысяч долларов в год. Конечно же, не ходя каждый день на работу. Ведь в" +
    "ы, как и я, не очень любите работать.</p><br/>Год издания: 2015<br/>Формат: fb2<br/>Язык: ru<br/>Размер: 464 Kb<br/>Скачиваний:" +
    " 1280<br/>";

  String htmlWithLinksText =
    "<p class=\"book\">Миллион книг рассказывают, как заработать миллион. Рисковать всем и любить работать хотя бы по 16 часов в ден" +
    "ь. Я не готов рисковать всем и не очень люблю работать. Если вы по той же причине до сих пор не заработали миллион, попробую на" +
    "учить вас зарабатывать для начала 100–200 тысяч долларов в год. Конечно же, не ходя каждый день на работу. Ведь вы, как и я, не" +
    " очень любите работать.<a href=\"url\">link text</a></p><br/>Год издания: 2015<br/>Формат: fb2<br/>Язык: ru<br/>Размер: 464 Kb<" +
    "br/>Скачиваний: 1280<br/>";

  String filteredText =
    "Миллион книг <b>рассказывают</b>, как заработать миллион. Рисковать всем и любить работать хотя бы по 16 часов в день. Я не гот" +
    "ов рисковать <i>всем</i> и не очень люблю работать. Если вы по той же причине до сих пор не заработали миллион, попробую научит" +
    "ь вас зарабатывать для начала 100–200 тысяч долларов в год. Конечно же, не ходя каждый день на работу. Ведь вы, как и я, не оче" +
    "нь любите работать.\n\n" +
    "Год издания: 2015\nФормат: fb2\nЯзык: ru\nРазмер: 464 Kb\nСкачиваний: 1280\n";

  String filteredWithLinksText =
    "Миллион книг рассказывают, как заработать миллион. Рисковать всем и любить работать хотя бы по 16 часов в день. Я не готов риск" +
    "овать всем и не очень люблю работать. Если вы по той же причине до сих пор не заработали миллион, попробую научить вас зарабаты" +
    "вать для начала 100–200 тысяч долларов в год. Конечно же, не ходя каждый день на работу. Ведь вы, как и я, не очень любите рабо" +
    "тать.<a href=\"url\">link text</a>\n" +
    "Год издания: 2015\nФормат: fb2\nЯзык: ru\nРазмер: 464 Kb\nСкачиваний: 1280\n";

  @Test 
  public void testFilterOk() {
    String text = TelegramFilterHtml.filterText(htmlText);
    Assert.assertEquals(filteredText, text); 
  }

  @Test 
  public void testFilterLinksOk() {
    String text = TelegramFilterHtml.filterText(htmlWithLinksText);
    Assert.assertEquals(filteredWithLinksText, text); 
  }
}
