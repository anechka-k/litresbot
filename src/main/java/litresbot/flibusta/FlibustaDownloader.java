package litresbot.flibusta;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.kursx.parser.fb2.FictionBook;

import litresbot.books.BookDownloader;
import litresbot.books.BookInfo;
import litresbot.books.DownloadedBook;
import litresbot.books.DownloadedFb2Book;
import litresbot.books.FileExtensions;

public class FlibustaDownloader
{
  final static Logger logger = Logger.getLogger(FlibustaDownloader.class);

  public static DownloadedBook download(BookInfo bookInfo, String format) throws IOException
  {
    String root = bookInfo.site;
    if(root == null) return null;
    
    String url = FileExtensions.getUrlShortFromBook(bookInfo, format);
    if(url == null) return null;
    
    DownloadedBook book = new DownloadedBook();
    book.filename = FileExtensions.getFilenameFromBook(bookInfo, format);
    if(book.filename == null) return null;
    
    logger.info("Downloading book: " + url);
    book.content = BookDownloader.download(root, url, book.filename);
    logger.info("Downloading book done: " + url);
    return book;
  }

  public static DownloadedFb2Book downloadAndUnzip(BookInfo bookInfo, String format) throws IOException, OutOfMemoryError, ParserConfigurationException, SAXException
  {
    DownloadedFb2Book book = new DownloadedFb2Book();
    book.filename = FileExtensions.getFilenameFromBook(bookInfo, format);
    if(book.filename == null) return null;

    BookContentCache.CacheRecord rec = BookContentCache.cache.getIfPresent(bookInfo.id);
    if(rec != null)
    {
      logger.info("book " + bookInfo.id + " found in cache");
      book.book = rec.book;
      return book;
    }

    String root = bookInfo.site;
    if(root == null) return null;
    
    String url = FileExtensions.getUrlShortFromBook(bookInfo, format);
    if(url == null) return null;
    
    logger.info("Downloading book: " + url);
    byte[] content = BookDownloader.downloadAndUnzip(root, url, book.filename);
    logger.info("Downloading book done: " + url);

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);
    FictionBook fb2 = new FictionBook(byteArrayInputStream);
    book.book = fb2;

    rec = new BookContentCache.CacheRecord();
    rec.id = bookInfo.id;
    rec.book = fb2;
    BookContentCache.cache.put(bookInfo.id, rec);

    return book;
  }
}
