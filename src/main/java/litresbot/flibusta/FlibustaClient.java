package litresbot.flibusta;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import litresbot.books.BookDownloader;
import litresbot.books.BookInfo;
import litresbot.books.DownloadedBook;
import litresbot.books.FileExtensions;
import litresbot.opdssearch.flibusta.FlibustaOpdsClient;
import litresbot.opdssearch.flibusta.OpdsSearchResult;
import litresbot.telegram.SendMessageList;
import litresbot.telegram.view.TelegramView;

public class FlibustaClient
{
  final static Logger logger = Logger.getLogger(FlibustaClient.class);
  
  private static HashMap<String, BookInfo> booksCache;
  static
  {
    booksCache = new HashMap<String, BookInfo>();
  }

  private static HashMap<Integer, OpdsSearchResult> searchCache;
  static
  {
    searchCache = new HashMap<Integer, OpdsSearchResult>();
  }

  private static int searchId = 0;
  
  public static SendMessageList getBooks(String searchQuery, int pageSize)
  {
    OpdsSearchResult searchResult = new OpdsSearchResult();
    try
    {
      searchResult = FlibustaOpdsClient.searchBooks(searchQuery);
      searchId++;
      searchCache.put(searchId, searchResult);
    }
    catch (Exception e)
    {
      logger.warn("Http request failed", e);
    }

    int found = searchResult.books.size();
    if(found == 0) {
      return TelegramView.bookInfoNotFound();
    }
    List<BookInfo> firstPageBooks = searchResult.books;
      
    // create cache data to allow instant access by book id
    for(BookInfo book : searchResult.books) {
      booksCache.put(book.id, book);
    }

    String nextPage = null;
    if (pageSize < found) {
      firstPageBooks = firstPageBooks.subList(0, pageSize);
      nextPage = "/next " + searchId + " " + pageSize;
    }

    return TelegramView.bookSearchResult(firstPageBooks, searchId, 0, pageSize, found, null, nextPage);
  }

  public static SendMessageList getBooksById(int searchId, int from, int pageSize)
  {
    OpdsSearchResult searchResult = searchCache.get(searchId);

    if(searchResult == null) {
      return TelegramView.bookInfoNotFound();
    }

    int found = searchResult.books.size();
    if(found == 0) {
      return TelegramView.bookInfoNotFound();
    }
    List<BookInfo> thisPageBooks = searchResult.books;

    String prevPage = null;
    if (from > 0) {
      int newFrom = 0;
      if (from - pageSize > 0) newFrom = from - pageSize;
      thisPageBooks = thisPageBooks.subList(from, thisPageBooks.size());
      prevPage = "/next " + searchId + " " + newFrom;
    }

    String nextPage = null;
    if (from + pageSize < found) {
      thisPageBooks = thisPageBooks.subList(0, pageSize);
      nextPage = "/next " + searchId + " " + (from + pageSize);
    }

    return TelegramView.bookSearchResult(thisPageBooks, searchId, from, pageSize, found, prevPage, nextPage);
  }
  
  public static SendMessageList chooseBookAction(String bookId)
  {    
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null) return TelegramView.bookInfoNotFound();
    return TelegramView.bookChooseAction(bookInfo);
  }
  
  public static SendMessageList chooseBookFormat(String bookId)
  {    
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null) return TelegramView.bookInfoNotFound();

    BookInfo flibustaBookInfo = FileExtensions.stripZipFromExtensions(bookInfo);
    return TelegramView.bookChooseFormat(flibustaBookInfo);
  }

  public static DownloadedBook downloadWithCache(String bookId, String format) throws IOException
  {
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null) return null;

    String root = bookInfo.site;
    if(root == null) return null;
    
    String url = FileExtensions.getUrlShortFromBook(bookInfo, format);
    if(url == null) return null;
    
    DownloadedBook book = new DownloadedBook();
    book.filename = FileExtensions.getFilenameFromBook(bookInfo, format);
    if(book.filename == null) return null;
    
    logger.info("Downloading book: " + url);
    book.content = BookDownloader.downloadWithCache(root, url, book.filename);
    logger.info("Downloading book done: " + url);
    return book;
  }
}
