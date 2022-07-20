package litresbot.flibusta;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import litresbot.books.BookInfo;
import litresbot.books.DownloadedFb2Book;
import litresbot.books.convert.Fb2Converter;
import litresbot.books.convert.Fb2Converter.ConvertResult;
import litresbot.telegram.SendMessageList;
import litresbot.telegram.view.TelegramView;

public class FlibustaReader
{
  final static Logger logger = Logger.getLogger(FlibustaReader.class);
  final static String defaultReadFormat = "fb2";
  ///TODO: make it a property
  final static int pageSize = 3000;

  public static SendMessageList readBook(BookInfo bookInfo, Long paragraph, Long position, int pageNumber)
  {
    DownloadedFb2Book book = null;
    try
    {
      book = FlibustaDownloader.downloadAndUnzip(bookInfo, defaultReadFormat);
    }
    catch(IOException | OutOfMemoryError | ParserConfigurationException | SAXException e)
    {
      logger.warn("Book download for reading failed", e);
    }
    
    if(book == null || book.book == null) return TelegramView.bookCouldNotDownload();
    
    // now we have a parsed fb2 book
    // let's read it
    
    try
    {
      ConvertResult converted = Fb2Converter.convertToText(book.book, paragraph, position, pageSize);
      return readBookPage(converted.text, bookInfo.id, converted.nextParagraph, converted.nextPosition, pageNumber);
    }
    catch(IOException e)
    {
      logger.warn("Read book failed", e);
    }
    
    return TelegramView.bookInfoNotFound();
  }

  private static SendMessageList readBookPage(String content, String bookId, Long nextParagraph, Long nextPosition, int pageNumber) {
    SendMessageList result = new SendMessageList(4096);
    if(content == null) return TelegramView.bookCouldNotDownload();

    String nextPage = null;
    if (nextParagraph >= 0) {
      nextPage = "/read " + bookId + " " + nextParagraph + " " + nextPosition + " " + (pageNumber + 1);
    }

    result = TelegramView.readBookSection(result, content, nextPage, pageNumber);

    ///TODO: add page numbers

    return result;
  }
}
