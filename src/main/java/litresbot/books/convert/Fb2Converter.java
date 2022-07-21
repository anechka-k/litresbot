package litresbot.books.convert;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.kursx.parser.fb2.Body;
import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.Section;
import com.kursx.parser.fb2.Title;

public class Fb2Converter
{
  final static Logger logger = Logger.getLogger(Fb2Converter.class);
  public final static String PARAGRAPH_INDENT = "    ";

  public static class ConvertResult {
    public String text;
    public long nextParagraph;
    public long nextPosition;
  }

  public static ConvertResult convertToText(FictionBook book) throws UnsupportedEncodingException {
    return convertToText(book, 0, 0, -1);
  }

  public static ConvertResult convertToText(FictionBook book, long fromParagraph, long fromPosition, long size) throws UnsupportedEncodingException {
    ConvertResult result = new ConvertResult();
    Body fb2Body = book.getBody();
    if(fb2Body == null) return result;

    // prepare text printer to print a tree of sections
    TextSectionRangePrinter printer = new TextSectionRangePrinter();
    printer.fromParagraph = fromParagraph;
    printer.fromPosition = fromPosition;
    printer.size = size;

    try {
      Section root = new Section();
      Title fb2Title = fb2Body.getTitle();
      // the main title of the book
      if (fb2Title != null) {
        root.setTitle(fb2Title);
      }
      root.getSections().addAll(fb2Body.getSections());

      // now process sections.
      // NOTE: Section may contain other sections
      // NOTE: Paragraph may contain other paragraphs
      printer.depthFirstSearch(root);
    } catch(IOException e) {
      logger.warn("Failed to convert book", e);
    }

    result.text = printer.getText();
    result.nextParagraph = printer.getNextParagraph();
    result.nextPosition = printer.getNextPosition();
    return result;
  }

  public static List<String> convertToText(FictionBook book, int pageSize) throws UnsupportedEncodingException {
    List<String> pages = new ArrayList<String>();

    long fromParagraph = 0L;
    long fromPosition = 0L;
    while(true) {
      if (fromParagraph < 0) break;
      ConvertResult converted = convertToText(book, fromParagraph, fromPosition, pageSize);
      fromParagraph = converted.nextParagraph;
      fromPosition = converted.nextPosition;
      pages.add(converted.text);
    }
    return pages;
  }
}
