package litresbot.flibusta;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import litresbot.books.BookFileLink;
import litresbot.books.BookInfo;
import litresbot.books.DownloadedBook;
import litresbot.books.FileExtensions;
import litresbot.localisation.UserMessagesEn;
import litresbot.opdssearch.flibusta.FlibustaOpdsClient;
import litresbot.opdssearch.flibusta.OpdsSearchResult;
import litresbot.telegram.SendMessageList;
import litresbot.telegram.TelegramFilterHtml;
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
    try {
      searchResult = FlibustaOpdsClient.searchBooks(searchQuery);
      searchId++;
      searchCache.put(searchId, searchResult);
    } catch (Exception e) {
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

    return TelegramView.bookSearchResult(firstPageBooks, searchId, 0, pageSize, found, nextPage);
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

    String nextPage = null;
    if (from + pageSize < found) {
      thisPageBooks = thisPageBooks.subList(from, from + pageSize);
      nextPage = "/next " + searchId + " " + (from + pageSize);
    }

    return TelegramView.bookSearchResult(thisPageBooks, searchId, from, pageSize, found, nextPage);
  }
  
  public static SendMessageList chooseBookAction(String bookId)
  {    
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null) return TelegramView.bookInfoNotFound();
    BookInfo bookInfoFilteredAnnotation = new BookInfo(bookInfo);
    String[] sections = bookInfoFilteredAnnotation.annotation.split("<br ?/?>");

    // search for format section in the book annotation. It starts with "Формат: "
    int formatSection = -1;
    for (int i = 0; i < sections.length; i++) {
      String s = sections[i];
      if (s.toLowerCase().startsWith("формат: ")) {
        formatSection = i;
        break;
      }
    }

    // keep only annotation or show "empty annotation" text
    if (formatSection == 0) {
      bookInfoFilteredAnnotation.annotation = litresbot.Application.userMessages.get(UserMessagesEn.annotationEmpty);
    } else {
      String[] annotationSections = Arrays.copyOf(sections, formatSection > 0 ? formatSection : sections.length);
      String annotation = String.join("\n", annotationSections);
      bookInfoFilteredAnnotation.annotation = TelegramFilterHtml.filterText(annotation);
    }

    boolean hasFb2Format = false;
    for(BookFileLink l : bookInfo.links) {
      String formatType = FileExtensions.detectFormat(l.format);
      if (formatType.startsWith(FlibustaDownloader.defaultReadFormat)) {
        hasFb2Format = true;
        break;
      }
    }

    return TelegramView.bookChooseAction(bookInfoFilteredAnnotation, hasFb2Format);
  }
  
  public static SendMessageList chooseBookFormat(String bookId)
  {    
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null) return TelegramView.bookInfoNotFound();

    BookInfo flibustaBookInfo = FileExtensions.stripZipFromExtensions(bookInfo);
    return TelegramView.bookChooseFormat(flibustaBookInfo);
  }

  public static DownloadedBook download(String bookId, String format) throws IOException
  {
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null) return null;

    return FlibustaDownloader.download(bookInfo, format);
  }

  public static SendMessageList readBook(String bookId, int pageNumber)
  {
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null) return null;

    return FlibustaReader.readBook(bookInfo, pageNumber);
  }
}
