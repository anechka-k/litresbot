package litresbot.books.convert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

  public void printBody(Node body) throws IOException {
    Element fb2BodyElement = (Element)body;
    NodeList sections = fb2BodyElement.getElementsByTagName("section");

    // add first title of the body and finish search
    NodeListIterator titlesIterator = new NodeListIterator(fb2BodyElement);
    for (Node n : titlesIterator.getIterable()) {
      if (n.getNodeName() != "title") continue;
      if (printSection(n)) return;
      break;
    }

    // add all sections of the body (including childrens' children) to the sectionNodes
    NodeListIterator sectionsIterator = new NodeListIterator(sections);
    for (Node n : sectionsIterator.getIterable()) {
      if (printSection(n)) return;
    }

    nextParagraph = -1;
  }

  // return true if size limit has reached
  private boolean printSection(Node section) throws IOException {
    NodeListIterator sectionChildrenIterator = new NodeListIterator(section);
    
    List<Node> paragraphs = new ArrayList<>();
    for (Node c : sectionChildrenIterator.getIterable()) {
      if (c.getNodeName() == "title") {
        // process title paragraphs
        NodeListIterator titleChildrenIterator = new NodeListIterator(c);
        for (Node t : titleChildrenIterator.getIterable()) {
          if (t.getNodeName() != "p") continue;
          paragraphs.add(t);
        }
        continue;
      }
      if (c.getNodeName() == "p") {
        paragraphs.add(c);
        continue;
      }
    }

    if (nextParagraph + paragraphs.size() < fromParagraph) {
      nextParagraph += paragraphs.size();
      nextPosition = 0;
      return false;
    }

    for(int i = 0; i < paragraphs.size(); i++) {
      if (nextParagraph < fromParagraph) {
        nextParagraph++;
        nextPosition = 0;
        continue;
      }

      if (printParagraphTree(paragraphs.get(i))) return true;
    }

    return false;
  }

  // return true if size limit has reached
  private boolean printParagraphTree(Node node) throws IOException {
    Stack<Node> stk = new Stack<Node>();
    stk.push(node);
  
    while (!stk.empty()) {
      Node top = stk.pop();

      NodeListIterator paragraphsIterator = new NodeListIterator(top);
      paragraphsIterator.setEnd();
      while (paragraphsIterator.hasPrevious()) {
        Node p = paragraphsIterator.previous();
        if (p.getNodeName() != "p") continue;
        stk.push(p);
      }

      if (printParagraph(top)) return true;
    }
    return false;
  }

  // return true if size limit has reached
  private boolean printParagraph(Node paragraph) throws IOException {
    String paragraphText = "";
    NodeListIterator childrenIterator = new NodeListIterator(paragraph);
    for (Node c : childrenIterator.getIterable()) {
      if (c.getNodeName() == "p") continue;
      paragraphText += c.getTextContent();
    }
    String tmp = "\n" + Fb2Converter.PARAGRAPH_INDENT + paragraphText;

    int paragraphSize = tmp.length();
    int paragraphStart = 0;

    if (fromPosition > 0 && (nextPosition < fromPosition)) {
      long paragraphEnd = nextPosition + paragraphSize;
      if (paragraphEnd <= fromPosition) {
        nextPosition += paragraphSize;
        nextParagraph++;
        nextPosition = 0;
        if (nextParagraph > fromParagraph) {
          fromParagraph++;
          fromPosition = (fromPosition > paragraphSize) ? (fromPosition - paragraphSize) : 0;
        }
        return false;
      }
      paragraphStart = (int)(fromPosition - nextPosition);
      paragraphSize -= paragraphStart;
      nextPosition += paragraphStart;
    }

    if (size > 0) {
      if (stream.length() + paragraphSize > size) {
        paragraphSize = (int)(size - stream.length());
      }
    }

    if (size >=0 && stream.length() >= size) return true;
    if (paragraphSize <= 0) return false;

    stream.append(tmp.substring(paragraphStart, paragraphStart + paragraphSize));

    nextPosition = paragraphStart + paragraphSize;
    if (nextPosition >= tmp.length()) {
      nextParagraph++;
      nextPosition = 0;
      if (nextParagraph > fromParagraph) {
        fromParagraph++;
        fromPosition = (fromPosition > paragraphSize) ? (fromPosition - paragraphSize) : 0;
      }
    }

    return size < 0 ? false : stream.length() >= size;
  }
}