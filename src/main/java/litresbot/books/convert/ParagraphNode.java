package litresbot.books.convert;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

class ParagraphNode {
  public Node node;
  public String text;
  public List<TagPosition> tags = new ArrayList<>();
}