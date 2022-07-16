package litresbot.books.convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.utils.Charsets;

import com.kursx.parser.fb2.Body;
import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.P;
import com.kursx.parser.fb2.Section;
import com.kursx.parser.fb2.Title;

public class Fb2Converter
{
  final static Logger logger = Logger.getLogger(Fb2Converter.class);
  public final static String PARAGRAPH_INDENT = "  ";

  static abstract class SectionPrinter {
    public ByteArrayOutputStream stream;
    // returns false if printer is no longer available
    public abstract boolean print(Section section) throws IOException;
  }

  static class TextSectionPrinter extends SectionPrinter {
    public boolean print(Section section) throws IOException {
      Title sectionTitle = section.getTitle();
          
      // show title if available
      if(sectionTitle != null) {
        List<P> titleParagraphs = sectionTitle.getParagraphs();
        for(P paragraph : titleParagraphs) {
          appendParagraph(paragraph.getText(), stream);
        }
      }
        
      List<P> paragraphs = section.getParagraphs();
      for(P paragraph : paragraphs) {
        appendParagraph(paragraph.getText(), stream);
      }

      return true;
    }
  }

  static class TextSectionRangePrinter extends SectionPrinter {
    public long fromPosition;
    public long toPosition;
    public long skipped;

    public boolean print(Section section) throws IOException {
      Title sectionTitle = section.getTitle();
          
      // show title if available
      if(sectionTitle != null) {
        List<P> titleParagraphs = sectionTitle.getParagraphs();
        for(P paragraph : titleParagraphs) {
          if (!printParagraph(paragraph)) return false;
        }
      }
          
      List<P> paragraphs = section.getParagraphs();
      for(P paragraph : paragraphs) {
        if (!printParagraph(paragraph)) return false;
      }

      return true;
    }

    private boolean printParagraph(P paragraph) throws IOException {
      ByteArrayOutputStream tmp = new ByteArrayOutputStream();
      appendParagraph(paragraph.getText(), tmp);

      int paragraphSize = tmp.size();
      int paragraphFrom = 0;

      if (fromPosition > 0 && (skipped < fromPosition)) {
        long paragraphEnd = skipped + paragraphSize;
        if (paragraphEnd <= fromPosition) {
          skipped += paragraphSize;
          return true;
        }
        paragraphFrom = (int)(fromPosition - skipped);
        paragraphSize -= paragraphFrom;
        skipped += paragraphFrom;
      }

      if (toPosition > 0) {
        if (skipped + stream.size() + paragraphSize > toPosition) {
          paragraphSize = (int)(toPosition - (skipped + stream.size()));
        }
        if (paragraphSize <= 0) return false;
      }

      stream.write(tmp.toByteArray(), paragraphFrom, paragraphSize);
      return true;
    }
  }

  public static byte[] convertToText(FictionBook book) {
    return convertToText(book, 0, -1);
  }

  public static byte[] convertToText(FictionBook book, long fromPosition, long toPosition) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Body fb2Body = book.getBody();
    if(fb2Body == null) return out.toByteArray();

    // prepare text printer to print a tree of sections
    TextSectionRangePrinter printer = new TextSectionRangePrinter();
    printer.fromPosition = fromPosition;
    printer.toPosition = toPosition;
    printer.stream = out;

    try {
      Title fb2Title = fb2Body.getTitle();
      // the main title of the book
      if (fb2Title != null) {
        List<P> titleParagraphs = fb2Title.getParagraphs();

        for(P paragraph : titleParagraphs) {
          if (!printer.printParagraph(paragraph)) break;
        }
      }

      // now process sections.
      // NOTE: Section may contain other sections

      List<Section> sections = fb2Body.getSections();
      
      for(Section section : sections) {
        depthFirstSearch(section, printer);
      }
    } catch(IOException e) {
      logger.warn("Failed to convert book", e);
    }

    return out.toByteArray();
  }

  private static void appendParagraph(String text, OutputStream result) throws IOException {
    String textLine = PARAGRAPH_INDENT + text + "\n";
    result.write(textLine.getBytes(Charsets.UTF8_CHARSET));
  }

  private static void depthFirstSearch(Section node, SectionPrinter printer) throws IOException {  
    Stack<Section> stk = new Stack<Section>();
    stk.push(node);
  
    while (!stk.empty()) {
      Section top = stk.pop();
          
      for (Section child: top.getSections()) {
        stk.push(child);
      }
      
      if (!printer.print(top)) return;
    }
  }
}
