package litresbot.books.convert;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.kursx.parser.fb2.FictionBook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;

public class Fb2ConverterTest {
  String fb2Text =
    "<?xml version=\"1.0\" encoding=\"utf8\"?>\n" +
    "<FictionBook xmlns:l=\"http://www.w3.org/1999/xlink\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.gribuser.ru/xml/fictionbook/2.0\">\n" +
    "  <description>\n" +
    "    <title-info>\n" +
    "      <genre>fiction</genre>\n" +
    "      <author>\n" +
    "        <first-name>John</first-name>\n" +
    "        <last-name>Doe</last-name>\n" +
    "      </author>\n" +
    "      <book-title>Fiction Book</book-title>\n" +
    "      <annotation>\n" +
    "        <p>Hello</p>\n" +
    "      </annotation>\n" +
    "      <keywords>john, doe, fiction</keywords>\n" +
    "      <date value=\"2011-07-18\">18.07.2011</date>\n" +
    "      <coverpage></coverpage>\n" +
    "      <lang>en</lang>\n" +
    "    </title-info>\n" +
    "    <document-info>\n" +
    "      <author>\n" +
    "        <first-name></first-name>\n" +
    "        <last-name></last-name>\n" +
    "        <nickname></nickname>\n" +
    "      </author>\n" +
    "      <program-used>Fb2 Gem</program-used>\n" +
    "      <date value=\"2011-07-18\">18.07.2011</date>\n" +
    "      <src-url></src-url>\n" +
    "      <src-ocr></src-ocr>\n" +
    "      <id></id>\n" +
    "      <version>1.0</version>\n" +
    "    </document-info>\n" +
    "    <publish-info>\n" +
    "    </publish-info>\n" +
    "  </description>\n" +
    "  <body>\n" +
    "    <title>\n" +
    "      <p>John Doe</p>\n" +
    "      <empty-line/>\n" +
    "      <p>Fiction Book</p>\n" +
    "    </title>\n" +
    "    <section>\n" +
    "      <title>\n" +
    "        <p>Chapter 1</p>\n" +
    "      </title>\n" +
    "      <p>Line <emphasis>one</emphasis> of the first chapter</p>\n" +
    "      <p>Line two of the first chapter</p>\n" +
    "      <p>Line three of the first chapter</p>\n" +
    "    </section>\n" +
    "    <section>\n" +
    "      <title>\n" +
    "        <p>Chapter 2</p>\n" +
    "      </title>\n" +
    "      <p>Line one of the second chapter</p>\n" +
    "      <p>Line two of the second chapter</p>\n" +
    "      <p>Line three of the second chapter</p>\n" +
    "      <p>Line four of the second chapter</p>\n" +
    "    </section>\n" +
    "  </body>\n" +
    "</FictionBook>";

  String fb2TextNoDescription =
    "<?xml version=\"1.0\" encoding=\"utf8\"?>\n" +
    "<FictionBook xmlns:l=\"http://www.w3.org/1999/xlink\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.gribuser.ru/xml/fictionbook/2.0\">\n" +
    "  <body>\n" +
    "    <title>\n" +
    "      <p>John Doe</p>\n" +
    "      <empty-line/>\n" +
    "      <p>Fiction Book</p>\n" +
    "    </title>\n" +
    "    <section>\n" +
    "      <title>\n" +
    "        <p>Chapter 1</p>\n" +
    "      </title>\n" +
    "      <p>Line <emphasis>one</emphasis> of the first chapter</p>\n" +
    "      <p>Line two of the first chapter</p>\n" +
    "      <p>Line three of the first chapter</p>\n" +
    "    </section>\n" +
    "    <section>\n" +
    "      <title>\n" +
    "        <p>Chapter 2</p>\n" +
    "      </title>\n" +
    "      <p>Line one of the second chapter</p>\n" +
    "      <p>Line two of the second chapter</p>\n" +
    "      <p>Line three of the second chapter</p>\n" +
    "      <p>Line four of the second chapter</p>\n" +
    "    </section>\n" +
    "  </body>\n" +
    "</FictionBook>";

  String fb2TextNoTitle =
    "<?xml version=\"1.0\" encoding=\"utf8\"?>\n" +
    "<FictionBook xmlns:l=\"http://www.w3.org/1999/xlink\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.gribuser.ru/xml/fictionbook/2.0\">\n" +
    "  <body>\n" +
    "    <section>\n" +
    "      <title>\n" +
    "        <p>Chapter 1</p>\n" +
    "      </title>\n" +
    "      <p>Line <emphasis>one</emphasis> of the first chapter</p>\n" +
    "      <p>Line two of the first chapter</p>\n" +
    "      <p>Line three of the first chapter</p>\n" +
    "    </section>\n" +
    "    <section>\n" +
    "      <title>\n" +
    "        <p>Chapter 2</p>\n" +
    "      </title>\n" +
    "      <p>Line one of the second chapter</p>\n" +
    "      <p>Line two of the second chapter</p>\n" +
    "      <p>Line three of the second chapter</p>\n" +
    "      <p>Line four of the second chapter</p>\n" +
    "    </section>\n" +
    "  </body>\n" +
    "</FictionBook>";

  String fb2TextNoParagraphs =
    "<?xml version=\"1.0\" encoding=\"utf8\"?>\n" +
    "<FictionBook xmlns:l=\"http://www.w3.org/1999/xlink\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.gribuser.ru/xml/fictionbook/2.0\">\n" +
    "  <body>\n" +
    "    <section>\n" +
    "      <title>\n" +
    "      </title>\n" +
    "    </section>\n" +
    "    <section>\n" +
    "      <title>\n" +
    "      </title>\n" +
    "    </section>\n" +
    "  </body>\n" +
    "</FictionBook>";

  String textConverted =
    "  John Doe\n" +
    "  Fiction Book\n" +
    "  Chapter 1\n" +
    "  Line one of the first chapter\n" +
    "  Line two of the first chapter\n" +
    "  Line three of the first chapter\n" +
    "  Chapter 2\n" +
    "  Line one of the second chapter\n" +
    "  Line two of the second chapter\n" +
    "  Line three of the second chapter\n" +
    "  Line four of the second chapter\n";

  String textConvertedNoTitle =
    "  Chapter 1\n" +
    "  Line one of the first chapter\n" +
    "  Line two of the first chapter\n" +
    "  Line three of the first chapter\n" +
    "  Chapter 2\n" +
    "  Line one of the second chapter\n" +
    "  Line two of the second chapter\n" +
    "  Line three of the second chapter\n" +
    "  Line four of the second chapter\n";

  String textRangeConverted =
    "  Chapter 1\n" +
    "  Line one of the first chapter\n";

  String textConvertedNoParagraphs = "";

  @Test 
  public void testConvertOk() throws OutOfMemoryError, ParserConfigurationException, IOException, SAXException {
    InputStream inputStream = new ByteArrayInputStream(fb2Text.getBytes(Charset.forName("UTF-8")));
    FictionBook book = new FictionBook(inputStream);
    byte[] content = Fb2Converter.convertToText(book);
    String contentText = new String(content);
    Assert.assertEquals(textConverted, contentText);
  }

  @Test 
  public void testConvertNoDescriptionOk() throws OutOfMemoryError, ParserConfigurationException, IOException, SAXException {
    InputStream inputStream = new ByteArrayInputStream(fb2TextNoDescription.getBytes(Charset.forName("UTF-8")));
    FictionBook book = new FictionBook(inputStream);
    byte[] content = Fb2Converter.convertToText(book);
    String contentText = new String(content);
    Assert.assertEquals(textConverted, contentText);
  }

  @Test 
  public void testConvertNoTitleOk() throws OutOfMemoryError, ParserConfigurationException, IOException, SAXException {
    InputStream inputStream = new ByteArrayInputStream(fb2TextNoTitle.getBytes(Charset.forName("UTF-8")));
    FictionBook book = new FictionBook(inputStream);
    byte[] content = Fb2Converter.convertToText(book);
    String contentText = new String(content);
    Assert.assertEquals(textConvertedNoTitle, contentText);
  }

  @Test 
  public void testConvertNoParagraphsOk() throws OutOfMemoryError, ParserConfigurationException, IOException, SAXException {
    InputStream inputStream = new ByteArrayInputStream(fb2TextNoParagraphs.getBytes(Charset.forName("UTF-8")));
    FictionBook book = new FictionBook(inputStream);
    byte[] content = Fb2Converter.convertToText(book);
    String contentText = new String(content);
    Assert.assertEquals(textConvertedNoParagraphs, contentText);
  }

  @Test 
  public void testConvertRangedOk() throws OutOfMemoryError, ParserConfigurationException, IOException, SAXException {
    InputStream inputStream = new ByteArrayInputStream(fb2Text.getBytes(Charset.forName("UTF-8")));
    FictionBook book = new FictionBook(inputStream);
    byte[] content = Fb2Converter.convertToText(book, 26, (26 + 44));
    String contentText = new String(content);
    Assert.assertEquals(textRangeConverted, contentText);
  }
}
