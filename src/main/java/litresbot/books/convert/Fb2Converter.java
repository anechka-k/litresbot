package litresbot.books.convert;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.kursx.parser.fb2.Body;
import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.P;
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

  static abstract class SectionPrinter {
    public StringBuilder stream = new StringBuilder();
    // returns false if printer is no longer available
    public abstract boolean print(Section section, boolean isLastSection) throws IOException;
  }

  static class TextSectionRangePrinter extends SectionPrinter {
    // input parameters for choosing range of the printer
    public long fromParagraph;
    public long fromPosition;
    public long size;

    // local variables to skip to the desired position and paragraph
    private long skipped;
    private long skippedParagraphs;

    // outputs
    private long nextParagraph;
    private long nextPosition;

    public long getNextParagraph() {
      return nextParagraph;
    }

    public long getNextPosition() {
      return nextPosition;
    }

    public boolean print(Section section, boolean isLastSection) throws IOException {
      nextParagraph = skippedParagraphs;
      Title sectionTitle = section.getTitle();
          
      // show title if available
      if(sectionTitle != null) {
        List<P> titleParagraphs = sectionTitle.getParagraphs();
        if (skippedParagraphs + titleParagraphs.size() >= fromParagraph) {
          for(int i = 0; i < titleParagraphs.size(); i++) {
            if (skippedParagraphs < fromParagraph) {
              skippedParagraphs++;
              nextPosition = 0;
              continue;
            }
            P paragraph = titleParagraphs.get(i);
            boolean isLastParagraph = ((i + 1) >= titleParagraphs.size() && section.getParagraphs().size() == 0);
            if (!printParagraph(paragraph, isLastSection, isLastParagraph)) return false;
          }
        } else {
          skippedParagraphs += titleParagraphs.size();
          nextPosition = 0;
        }
      }
          
      List<P> paragraphs = section.getParagraphs();
      if (skippedParagraphs + paragraphs.size() < fromParagraph) {
        skippedParagraphs += paragraphs.size();
        nextPosition = 0;
        return true;
      }

      for(int i = 0; i < paragraphs.size(); i++) {
        if (skippedParagraphs < fromParagraph) {
          skippedParagraphs++;
          nextPosition = 0;
          continue;
        }
        P paragraph = paragraphs.get(i);
        boolean isLastParagraph = (i + 1) >= paragraphs.size();
        if (!printParagraph(paragraph, isLastSection, isLastParagraph)) return false;
      }

      return true;
    }

    private boolean printParagraph(P paragraph, boolean isLastSection, boolean isLastParagraph) throws IOException {
      String tmp = "\n" + PARAGRAPH_INDENT + paragraph.getText();

      int paragraphSize = tmp.length();
      int paragraphStart = 0;

      if (fromPosition > 0 && (skipped < fromPosition)) {
        long paragraphEnd = skipped + paragraphSize;
        if (paragraphEnd <= fromPosition) {
          skipped += paragraphSize;

          if (isLastSection && isLastParagraph) {
            nextParagraph = -1;
            return false;
          }
          skippedParagraphs++;
          nextParagraph = skippedParagraphs;
          nextPosition = 0;
          return true;
        }
        paragraphStart = (int)(fromPosition - skipped);
        paragraphSize -= paragraphStart;
        skipped += paragraphStart;
      }

      if (size > 0) {
        if (stream.length() + paragraphSize > size) {
          paragraphSize = (int)(size - stream.length());
        }
        if (paragraphSize <= 0) return false;
      }

      stream.append(tmp.substring(paragraphStart, paragraphStart + paragraphSize));

      nextPosition = paragraphStart + paragraphSize;
      if (nextPosition >= tmp.length()) {
        if (isLastSection && isLastParagraph) {
          nextParagraph = -1;
          return false;
        }
        skippedParagraphs++;
        nextParagraph = skippedParagraphs;
        nextPosition = 0;
      }
      return true;
    }
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
      depthFirstSearch(root, printer);
    } catch(IOException e) {
      logger.warn("Failed to convert book", e);
    }

    result.text = printer.stream.toString();
    result.nextParagraph = printer.nextParagraph;
    result.nextPosition = printer.nextPosition;
    return result;
  }

  private static void depthFirstSearch(Section node, SectionPrinter printer) throws IOException {  
    Stack<Section> stk = new Stack<Section>();
    stk.push(node);
  
    while (!stk.empty()) {
      Section top = stk.pop();

      ListIterator<Section> listIterator = top.getSections().listIterator(top.getSections().size());
      while (listIterator.hasPrevious()) {
        stk.push(listIterator.previous());
      }
      
      if (top.getTitle() == null && top.getParagraphs().isEmpty()) continue;
      if (!printer.print(top, stk.empty())) return;
    }
  }
}
