package litresbot.books.convert;

import java.util.Comparator;
import java.util.List;

import litresbot.books.convert.TagPosition.TagType;

public class TagsInserter {
  public static String insertTags(String text, List<TagPosition> tagPositions) {
    tagPositions.sort(Comparator.comparing(TagPosition::getFrom));
    int appended = 0;

    for (int i = 0; i < tagPositions.size(); i++) {
      TagPosition t = tagPositions.get(i);
      if ((t.from + appended) >= text.length()) break;

      String textToTag = text.substring(0, t.from + appended);
      String textLeft = text.substring(t.from + appended, text.length());
      String tagText = "";
      if (t.type == TagType.ITALIC) {
        tagText = "<i>";
      } else if (t.type == TagType.BOLD) {
        tagText = "<b>";
      } else if (t.type == TagType.STRIKE) {
        tagText = "<s>";
      }
      appended += tagText.length();
      text = textToTag + tagText + textLeft;

      if ((t.to + appended) >= text.length()) {
        if (t.type == TagType.ITALIC) {
          text += "</i>";
        } else if (t.type == TagType.BOLD) {
          text += "</b>";
        } else if (t.type == TagType.STRIKE) {
          text += "</s>";
        }
        continue;
      }

      textToTag = text.substring(0, t.to + appended);
      textLeft = text.substring(t.to + appended, text.length());
      tagText = "";
      if (t.type == TagType.ITALIC) {
        tagText = "</i>";
      } else if (t.type == TagType.BOLD) {
        tagText = "</b>";
      } else if (t.type == TagType.STRIKE) {
        tagText = "</s>";
      }
      text = textToTag + tagText + textLeft;

      for (int j = i + 1; j < tagPositions.size(); j++) {
        TagPosition tt = tagPositions.get(j);
        if (tt.to < t.from) continue;
        if (tt.from >= t.to) tt.from += tagText.length();
        if (tt.to >= t.to) tt.to += tagText.length();
      }
    }

    return text;
  }
}
