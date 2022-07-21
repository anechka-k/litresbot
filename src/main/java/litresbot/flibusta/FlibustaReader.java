package litresbot.flibusta;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import litresbot.books.BookInfo;
import litresbot.books.DownloadedFb2Book;
import litresbot.telegram.SendMessageList;
import litresbot.telegram.view.TelegramView;

public class FlibustaReader
{
  final static Logger logger = Logger.getLogger(FlibustaReader.class);

  public static SendMessageList readBook(BookInfo bookInfo, int pageNumber)
  {
    DownloadedFb2Book book = null;
    try
    {
      book = FlibustaDownloader.downloadForRead(bookInfo);
    }
    catch(IOException | OutOfMemoryError | ParserConfigurationException | SAXException e)
    {
      logger.warn("Book download for reading failed", e);
    }
    
    if(book == null || book.pages.size() == 0) return TelegramView.bookCouldNotDownload();
    
    // now we have a parsed fb2 book
    // let's read it
    return readBookPage(book.pages, bookInfo.id, pageNumber);
  }

  private static SendMessageList readBookPage(List<String> pages, String bookId, int pageNumber) {
    SendMessageList result = new SendMessageList(4096);

    String nextPage = null;
    if (pages.size() > pageNumber) {
      nextPage = "/read " + bookId + " " + (pageNumber + 1);
    } else {
      pageNumber = pages.size() - 1;
    }

    result = TelegramView.readBookSection(result, pages.get(pageNumber), nextPage, pages.size(), pageNumber + 1);
    return result;
  }
}
