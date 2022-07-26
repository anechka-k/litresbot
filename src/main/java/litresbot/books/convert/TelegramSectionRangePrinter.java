package litresbot.books.convert;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

class TelegramSectionRangePrinter extends TextSectionRangePrinter {

  // return true if size limit has reached
  protected boolean printParagraph(ParagraphNode paragraph) throws IOException {
    if (nextParagraph < fromParagraph) {
      return false;
    }

    String paragraphText = paragraph.text;
    String tmpText = "\n" + Fb2Converter.PARAGRAPH_INDENT + paragraphText;
    String withTags = paragraphText;
    for (Pair<Integer, Integer> p : paragraph.italic) {
      int len = paragraphText.length();
      withTags = withTags.substring(0, p.getLeft()) +
        "<i>" + withTags.substring(p.getLeft(), p.getRight()) + "</i>" +
        withTags.substring(p.getRight(), len);
    }
    String tmpWithTags = "\n" + Fb2Converter.PARAGRAPH_INDENT + withTags;

    int paragraphSize = tmpText.length();
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

    ///TODO: do not break tags - they should be always included even if size limit is reached
    ///TODO: do not leave not closed tags. If size limit is reached - close all unclosed tags
    ///TODO: try to not break words - wait for space even if size limit is reached.
    ///      Do not wait too long - if word length is too far ahead of the size limit, break it.
    stream.append(tmpWithTags.substring(paragraphStart, paragraphStart + paragraphSize));

    nextPosition = paragraphStart + paragraphSize;
    if (nextPosition >= tmpText.length()) {
      fromPosition = 0;
    }

    return size < 0 ? false : stream.length() >= size;
  }
}