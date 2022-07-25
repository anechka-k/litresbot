package litresbot.books.convert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class TextSectionRangePrinter {
  class ParagraphNode {
    public Node node;
    public String text;
  }

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

  // return true if size limit has reached
  public boolean printBody(Node body) throws IOException {
    Element fb2BodyElement = (Element)body;
    NodeList sections = fb2BodyElement.getElementsByTagName("section");

    // add first title of the body and finish search
    NodeListIterator titlesIterator = new NodeListIterator(fb2BodyElement);
    for (Node n : titlesIterator.getIterable()) {
      if (n.getNodeName() != "title") continue;
      if (printSection(n)) return true;
      break;
    }

    // add all sections of the body (including childrens' children) to the sectionNodes
    NodeListIterator sectionsIterator = new NodeListIterator(sections);
    for (Node n : sectionsIterator.getIterable()) {
      if (printSection(n)) return true;
    }

    return false;
  }

  // return true if size limit has reached
  private boolean printSection(Node section) throws IOException {
    NodeListIterator sectionChildrenIterator = new NodeListIterator(section);

    for (Node c : sectionChildrenIterator.getIterable()) {
      if (c.getNodeName() == "title") {
        // process title paragraphs
        NodeListIterator titleChildrenIterator = new NodeListIterator(c);
        for (Node t : titleChildrenIterator.getIterable()) {
          if (t.getNodeName() != "p") continue;
          if (printParagraphTree(t)) return true;
        }
        continue;
      }
      if (c.getNodeName() == "p") {
        if (printParagraphTree(c)) return true;
        continue;
      }
    }

    return false;
  }

  // return true if size limit has reached
  private boolean printParagraphTree(Node node) throws IOException {
    Stack<Node> stk = new Stack<Node>();
    stk.push(node);
  
    while (!stk.empty()) {
      Node top = stk.pop();
      ParagraphNode topParagraph = new ParagraphNode();
      topParagraph.node = top;
      topParagraph.text = "";

      if (!top.hasChildNodes()) {
        topParagraph.text = top.getTextContent();
      }

      NodeListIterator paragraphsIterator = new NodeListIterator(top);
      List<Node> children = new ArrayList<Node>();
      for (Node p : paragraphsIterator.getIterable()) {
        if (p.getNodeName() == "p") {
          children.add(p);
          continue;
        }
        if (children.isEmpty()) {
          topParagraph.text += p.getTextContent();
          continue;
        }
        children.add(p);
      }

      ListIterator<Node> iterator = children.listIterator(children.size());
      while (iterator.hasPrevious()) {
        Node p = iterator.previous();
        stk.push(p);
      }

      if (printParagraph(topParagraph)) return true;
      nextParagraph++;
      nextPosition = 0;
    }
    return false;
  }

  // return true if size limit has reached
  private boolean printParagraph(ParagraphNode paragraph) throws IOException {
    if (nextParagraph < fromParagraph) {
      return false;
    }

    String paragraphText = paragraph.text;
    String tmp = "\n" + Fb2Converter.PARAGRAPH_INDENT + paragraphText;

    int paragraphSize = tmp.length();
    int paragraphStart = 0;

    if (fromPosition > 0 && (nextPosition < fromPosition)) {
      long paragraphEnd = nextPosition + paragraphSize;
      if (paragraphEnd <= fromPosition) {
        fromPosition = (fromPosition > paragraphSize) ? (fromPosition - paragraphSize) : 0;
        return false;
      }

      paragraphStart = (int)(fromPosition - nextPosition);
      paragraphSize -= paragraphStart;
      nextPosition = fromPosition;
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
      fromPosition = 0;
    }

    return size < 0 ? false : stream.length() >= size;
  }
}