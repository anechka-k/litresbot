package litresbot.flibusta;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.kursx.parser.fb2.Body;
import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.P;
import com.kursx.parser.fb2.Section;
import com.kursx.parser.fb2.Title;

import litresbot.books.BookDownloader;
import litresbot.books.BookInfo;
import litresbot.books.FileExtensions;
import litresbot.localisation.UserMessagesEn;
import litresbot.opdssearch.flibusta.FlibustaOpdsClient;
import litresbot.opdssearch.flibusta.OpdsSearchResult;
import litresbot.telegram.SendMessageList;
import litresbot.telegram.view.TelegramView;
import litresbot.util.TelegramEscape;

public class FlibustaClient
{
  final static Logger logger = Logger.getLogger(FlibustaClient.class);
  final static String defaultReadFormat = "fb2";
  
  private static HashMap<String, BookInfo> booksCache;
  static
  {
    booksCache = new HashMap<String, BookInfo>();
  }
  
  public static SendMessageList getBooks(String searchQuery, int from, int size)
  {
    OpdsSearchResult searchResult = new OpdsSearchResult();
    try
    {
      searchResult = FlibustaOpdsClient.searchBooks(searchQuery, from, size);
    }
    catch (Exception e)
    {
      logger.warn("Http request failed", e);
    }

    if(searchResult.found == 0) {
      return TelegramView.bookInfoNotFound();
    }
      
    // create cache data to allow instant access by book id
    for(BookInfo book : searchResult.books) {
      booksCache.put(book.id, book);
    }

    return TelegramView.bookSearchResult(searchResult.books, searchResult.found);
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

  public static SendMessageList readBook(String bookId, Long position)
  {
    SendMessageList result = new SendMessageList(4096);
    
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null) return null;

    String root = bookInfo.site;
    if(root == null) return null;
    
    String url = FileExtensions.getUrlShortFromBook(bookInfo, defaultReadFormat);
    if(url == null) return null;
    
    String fileName = FileExtensions.getFilenameFromBook(bookInfo, defaultReadFormat);
    if(fileName == null) return null;
    
    byte[] book = null;
        
    try
    {
      logger.info("Downloading book: " + url);
      book = BookDownloader.downloadUnzipBook(root, url, fileName);
      logger.info("Downloading book done: " + url);
    }
    catch(IOException e)
    {
      logger.warn("Book unzip failed", e);
    }
    
    if(book == null)
    {
      result = new SendMessageList(4096);
      result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorCouldNotDownloadFile));
      result.endTextPage();
      return result;
    }
    
    // now we have a book content in byte array
    // let's read it
    
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(book);
    
    try
    {      
      FictionBook fb2 = new FictionBook(byteArrayInputStream);
      
      Body fb2Body = fb2.getBody();
      
      if(fb2Body == null)
      {
        result = new SendMessageList(4096);
        result.appendTextPage(litresbot.Application.userMessages.get(UserMessagesEn.errorCouldNotDownloadFile));
        result.endTextPage();
        return result;
      }
      
      List<Section> sections = fb2Body.getSections();
      
      int i = 0;
      
      for(Section section : sections)
      {
        Title sectionTitle = section.getTitle();
        
        // show title if available
        if(sectionTitle != null)
        {
          List<P> titleParagraphs = sectionTitle.getParagraphs();
          
          // show title paragraphs if available
          if(titleParagraphs != null)
          {
            for(P paragraph : titleParagraphs)
            {
              String line = paragraph.getText();
              String escapedLine = TelegramEscape.escapeText(line);
              
              result.appendTextPage("<b>");
              result.appendTextPage("\n" + escapedLine + "\n\n");
              result.appendTextPage("</b>");
              result.endTextPage();
            }
          }
        }
        
        List<P> paragraphs = section.getParagraphs();
        
        // show text if available
        if(paragraphs != null)
        {
          for(P paragraph : paragraphs)
          {
            String line = paragraph.getText();
            String escapedLine = TelegramEscape.escapeText(line);
            
            result.appendTextPage(escapedLine + "\n\n");
            result.endTextPage();
          }
        }
        
        i++;
        
        if(i > 3) break;
      }
    }
    catch(IOException e)
    {
      logger.warn("Read book failed", e);
    }
    catch (ParserConfigurationException e)
    {
      logger.warn("Parse book failed", e);
    }
    catch (SAXException e)
    {
      logger.warn("Parse book failed", e);
    }
    
    return result;
  }

  public static byte[] downloadWithCache(String bookId, String format) throws IOException
  {
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null) return null;

    String root = bookInfo.site;
    if(root == null) return null;
    
    String url = FileExtensions.getUrlShortFromBook(bookInfo, format);
    if(url == null) return null;
    
    String fileName = FileExtensions.getFilenameFromBook(bookInfo, format);
    if(fileName == null) return null;
    
    logger.info("Downloading book: " + url);
    byte[] bookContent = BookDownloader.downloadWithCache(root, url, fileName);
    logger.info("Downloading book done: " + url);
    
    return bookContent;
  }

  public static String getDownloadFileName(String bookId, String format)
  {
    BookInfo bookInfo = booksCache.get(bookId);
    if(bookInfo == null) return null;

    String fileName = FileExtensions.getFilenameFromBook(bookInfo, format);
    return fileName;
  }
}
