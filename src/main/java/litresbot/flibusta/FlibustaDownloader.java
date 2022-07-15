package litresbot.flibusta;

import java.io.IOException;

import org.apache.log4j.Logger;

import litresbot.books.BookDownloader;
import litresbot.books.BookInfo;
import litresbot.books.DownloadedBook;
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

  public static DownloadedBook downloadAndUnzip(BookInfo bookInfo, String format) throws IOException
  {
    DownloadedBook book = new DownloadedBook();
    book.filename = FileExtensions.getFilenameFromBook(bookInfo, format);
    if(book.filename == null) return null;

    BookContentCache.CacheRecord rec = BookContentCache.cache.getIfPresent(bookInfo.id);
    if(rec != null)
    {
      logger.info("book " + bookInfo.id + " found in cache");
      book.content = rec.content;
      return book;
    }

    String root = bookInfo.site;
    if(root == null) return null;
    
    String url = FileExtensions.getUrlShortFromBook(bookInfo, format);
    if(url == null) return null;
    
    logger.info("Downloading book: " + url);
    book.content = BookDownloader.downloadAndUnzip(root, url, book.filename);
    logger.info("Downloading book done: " + url);

    rec = new BookContentCache.CacheRecord();
    rec.id = bookInfo.id;
    rec.content = book.content;
    BookContentCache.cache.put(bookInfo.id, rec);

    return book;
  }
}
