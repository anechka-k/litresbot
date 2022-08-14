package litresbot.books.convert;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Node;

class ParagraphNode {
  public Node node;
  public String text;
  public List<Pair<Integer, Integer>> strong = new ArrayList<>();
  public List<Pair<Integer, Integer>> italic = new ArrayList<>();
  public List<Pair<Integer, Integer>> strike = new ArrayList<>();
}