package litresbot.books.convert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class TextSectionRangePrinter {
  class ParagraphNode {
    public Node node;
    public String text;
    public List<Pair<Integer, Integer>> strong = new ArrayList<>();
    public List<Pair<Integer, Integer>> italic = new ArrayList<>();
    public List<Pair<Integer, Integer>> strike = new ArrayList<>();
  }

  // input parameters for choosing range of the printer
  public long fromParagraph;
  public long fromPosition;
  public long size;

  // outputs
  protected long nextParagraph;
  protected long nextPosition;

  protected StringBuilder stream = new StringBuilder();

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
  protected boolean printParagraphTree(Node node) throws IOException {
    Stack<ParagraphNode> stk = new Stack<ParagraphNode>();
    ParagraphNode topParagraph = new ParagraphNode();
    topParagraph.node = node;
    topParagraph.text = node.getTextContent();
    stk.push(topParagraph);
  
    while (!stk.empty()) {
      ParagraphNode top = stk.pop();

      if (top.node != null) {
        NodeListIterator paragraphsIterator = new NodeListIterator(top.node);
        List<ParagraphNode> children = new ArrayList<ParagraphNode>();
        ParagraphNode currentParagraph = null;

        for (Node p : paragraphsIterator.getIterable()) {
          if (p.getNodeName() == "p") {
            if (currentParagraph != null) {
              children.add(currentParagraph);
              currentParagraph = null;
            }

            ParagraphNode childParagraph = new ParagraphNode();
            childParagraph.node = p;
            childParagraph.text = p.getTextContent();
            children.add(childParagraph);
            continue;
          }

          if (currentParagraph == null) {
            currentParagraph = new ParagraphNode();
            currentParagraph.text = "";
          }
          if (p.getNodeName() == "strong") {
            currentParagraph.strong.add(Pair.of(currentParagraph.text.length(), currentParagraph.text.length() + p.getTextContent().length()));
            currentParagraph.text += p.getTextContent();
            continue;
          }
          if (p.getNodeName() == "emphasis") {
            currentParagraph.italic.add(Pair.of(currentParagraph.text.length(), currentParagraph.text.length() + p.getTextContent().length()));
            currentParagraph.text += p.getTextContent();
            continue;
          }
          if (p.getNodeName() == "strikethrough") {
            currentParagraph.text += p.getTextContent();
            continue;
          }
          if (p.getNodeName() == "subtitle") {
            currentParagraph.text += p.getTextContent();
            continue;
          }
          currentParagraph.text += p.getTextContent();
        }

        if (currentParagraph != null) {
          children.add(currentParagraph);
        }

        ListIterator<ParagraphNode> iterator = children.listIterator(children.size());
        while (iterator.hasPrevious()) {
          ParagraphNode p = iterator.previous();
          stk.push(p);
        }

        // take first paragraph again
        top = stk.pop();
      }

      if (printParagraph(top)) return true;
      nextParagraph++;
      nextPosition = 0;
    }
    return false;
  }

  // return true if size limit has reached
  protected boolean printParagraph(ParagraphNode paragraph) throws IOException {
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