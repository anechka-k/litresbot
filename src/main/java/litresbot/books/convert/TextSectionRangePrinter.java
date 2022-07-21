package litresbot.books.convert;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import com.kursx.parser.fb2.P;
import com.kursx.parser.fb2.Section;
import com.kursx.parser.fb2.Title;

class TextSectionRangePrinter {
  // input parameters for choosing range of the printer
  public long fromParagraph;
  public long fromPosition;
  public long size;

  // outputs
  private long nextParagraph;
  private long nextPosition;

  private StringBuilder stream = new StringBuilder();

  public long getNextParagraph() {
    return nextParagraph;
  }

  public long getNextPosition() {
    return nextPosition;
  }

  public String getText() {
    return stream.toString();
  }

  public boolean depthFirstSearch(Section node) throws IOException {  
    Stack<Section> stk = new Stack<Section>();
    stk.push(node);
  
    while (!stk.empty()) {
      Section top = stk.pop();

      ListIterator<Section> listIterator = top.getSections().listIterator(top.getSections().size());
      while (listIterator.hasPrevious()) {
        stk.push(listIterator.previous());
      }
      
      if (top.getTitle() == null && top.getParagraphs().isEmpty()) continue;
      if (!printSection(top, stk.empty())) return false;
    }
    return true;
  }

  private boolean printSection(Section section, boolean isLastSection) throws IOException {
    Title sectionTitle = section.getTitle();
        
    // show title if available
    if(sectionTitle != null) {
      List<P> titleParagraphs = sectionTitle.getParagraphs();
      if (nextParagraph + titleParagraphs.size() >= fromParagraph) {
        for(int i = 0; i < titleParagraphs.size(); i++) {
          if (nextParagraph < fromParagraph) {
            nextParagraph++;
            nextPosition = 0;
            continue;
          }
          P paragraph = titleParagraphs.get(i);
          boolean isLastParagraph = ((i + 1) >= titleParagraphs.size() && section.getParagraphs().size() == 0);
          if (!depthFirstParagraphSearch(paragraph, isLastSection, isLastParagraph)) return false;
        }
      } else {
        nextParagraph += titleParagraphs.size();
        nextPosition = 0;
      }
    }
        
    List<P> paragraphs = section.getParagraphs();
    if (nextParagraph + paragraphs.size() < fromParagraph) {
      nextParagraph += paragraphs.size();
      nextPosition = 0;
      return true;
    }

    for(int i = 0; i < paragraphs.size(); i++) {
      if (nextParagraph < fromParagraph) {
        nextParagraph++;
        nextPosition = 0;
        continue;
      }
      P paragraph = paragraphs.get(i);
      boolean isLastParagraph = (i + 1) >= paragraphs.size();
      if (!depthFirstParagraphSearch(paragraph, isLastSection, isLastParagraph)) return false;
    }

    return true;
  }

  private boolean printParagraph(P paragraph, boolean isLastSection, boolean isLastParagraph) throws IOException {
    String tmp = "\n" + Fb2Converter.PARAGRAPH_INDENT + paragraph.getText();

    int paragraphSize = tmp.length();
    int paragraphStart = 0;

    if (fromPosition > 0 && (nextPosition < fromPosition)) {
      long paragraphEnd = nextPosition + paragraphSize;
      if (paragraphEnd <= fromPosition) {
        nextPosition += paragraphSize;

        if (isLastSection && isLastParagraph) {
          nextParagraph = -1;
          return false;
        }
        nextParagraph++;
        nextPosition = 0;
        if (nextParagraph > fromParagraph) {
          fromParagraph++;
          fromPosition = (fromPosition > paragraphSize) ? (fromPosition - paragraphSize) : 0;
        }
        return true;
      }
      paragraphStart = (int)(fromPosition - nextPosition);
      paragraphSize -= paragraphStart;
      nextPosition += paragraphStart;
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
      nextParagraph++;
      nextPosition = 0;
      if (nextParagraph > fromParagraph) {
        fromParagraph++;
        fromPosition = (fromPosition > paragraphSize) ? (fromPosition - paragraphSize) : 0;
      }
    }
    return true;
  }

  private boolean depthFirstParagraphSearch(P node, boolean isLastSection, boolean isLastParagraph) throws IOException {  
    Stack<P> stk = new Stack<P>();
    stk.push(node);
    
    while (!stk.empty()) {
      P top = stk.pop();
  
      ListIterator<P> listIterator = top.getParagraphs().listIterator(top.getParagraphs().size());
      while (listIterator.hasPrevious()) {
        stk.push(listIterator.previous());
      }

      if (!printParagraph(top, isLastSection, isLastParagraph && stk.empty())) return false;
    }
    return true;
  }
}