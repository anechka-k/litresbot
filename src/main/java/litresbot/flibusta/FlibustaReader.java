package litresbot.flibusta;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.kursx.parser.fb2.Body;
import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.P;
import com.kursx.parser.fb2.Section;
import com.kursx.parser.fb2.Title;

import litresbot.books.BookInfo;
import litresbot.books.DownloadedBook;
import litresbot.telegram.SendMessageList;
import litresbot.telegram.view.TelegramView;

public class FlibustaReader
{
  final static Logger logger = Logger.getLogger(FlibustaReader.class);
  final static String defaultReadFormat = "fb2";
  // 100 symbols on page
  ///TODO: make it a property
  final static int pageSize = 100;

  public static SendMessageList readBook(BookInfo bookInfo, Long position)
  {
    DownloadedBook book = null;
    try
    {
      book = FlibustaDownloader.downloadAndUnzip(bookInfo, defaultReadFormat);
    }
    catch(IOException e)
    {
      logger.warn("Book download for reading failed", e);
    }
    
    if(book == null || book.content == null) return TelegramView.bookCouldNotDownload();
    
    // now we have a book content in byte array
    // let's read it
    
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(book.content);
    
    try
    {      
      FictionBook fb2 = new FictionBook(byteArrayInputStream);
      return readBookPage(fb2, position, pageSize);
    }
    catch(IOException e)
    {
      logger.warn("Read book failed", e);
    }
    catch (ParserConfigurationException | SAXException e)
    {
      logger.warn("Parse book failed", e);
    }
    
    return TelegramView.bookInfoNotFound();
  }

  private static SendMessageList readBookPage(FictionBook book, Long from, int size) {
    SendMessageList result = new SendMessageList(4096);

    Body fb2Body = book.getBody();
    if(fb2Body == null) return TelegramView.bookCouldNotDownload();
      
    List<Section> sections = fb2Body.getSections();

    long skipSize = 0;
    int collectedSize = 0;
      
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
            ///TODO: split paragraph to words and do not break in the middle of the word
            ///TODO: unless it is way too long
            collectedSize += line.length();
            skipSize += line.length();

            if (skipSize < from) continue;

            result = TelegramView.readBookSection(result, line);

            if (collectedSize > size) {
              return result;
            }
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
          collectedSize += line.length();
          skipSize += line.length();

          if (skipSize < from) continue;

          result = TelegramView.readBookParagraph(result, line);

          if (collectedSize > size) {
            return result;
          }
        }
      }
    }

    ///TODO: add read next button
    ///TODO: add page numbers

    return result;
  }
}
