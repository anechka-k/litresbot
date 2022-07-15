package litresbot.telegram;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TelegramFilterHtml
{
  // Telegram supported tags
  // <b>bold</b>, <strong>bold</strong>
  // <i>italic</i>, <em>italic</em>
  // <u>underline</u>, <ins>underline</ins>
  // <s>strikethrough</s>, <strike>strikethrough</strike>, <del>strikethrough</del>
  public static String filterText(String text)
  {
    Document doc = Jsoup.parseBodyFragment(text);
    Elements divElements = doc.select("a, b, strong, i, em, u, ins, s, strike, del, br, :matchText");
    String[] formatElements = { "a", "b", "strong", "i", "em", "u", "ins", "s", "strike", "del" };
    List<String> texts = new ArrayList<>(divElements.size());
    for (Element el: divElements) {
      // add newline in the end of <p>
      if (el.tagName().equalsIgnoreCase("p")) {
        String pText = el.wholeText();
        if (el.parent() != null && (el.siblingIndex() + 1 >= el.parent().childNodeSize())) {
          pText += "\n";
        }
        texts.add(pText);
        continue;
      }
      // add newline when we get <br>
      if (el.tagName().equalsIgnoreCase("br")) {
        texts.add("\n");
        continue;
      }
      boolean gotFormat = false;
      for(String f : formatElements) {
        if (el.tagName().equalsIgnoreCase(f)) {
          texts.add(el.outerHtml());
          gotFormat = true;
          break;
        }
      }
      if (gotFormat) continue;
      
      if (el.hasText()) {
        texts.add(el.wholeText());
      }
    }

    String result = String.join("", texts);
    return result;
  }
}
