package litresbot.books.convert;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

class TelegramParagraphPrinter extends TextParagraphPrinter {

  int printedSize = 0;

  // add unfinished page to the pages list
  public void flush(List<String> pages) {
    if (printedSize == 0) return;
    pages.add(currentPage.toString());
    currentPage = new StringBuilder();
    printedSize = 0;
  }

  ///TODO: do not break tags - they should be always included even if size limit is reached
  ///TODO: do not leave not closed tags. If size limit is reached - close all unclosed tags
  ///TODO: try to not break words - wait for space even if size limit is reached.
  ///      Do not wait too long - if word length is too far ahead of the size limit, break it.

  public void printParagraph(ParagraphNode paragraph, List<String> pages) throws IOException {
    String paragraphText = "\n" + Fb2Converter.PARAGRAPH_INDENT + paragraph.text;
    int paragraphSize = paragraphText.length();
    int currentPosition = 0;

    while (true) {
      if (printedSize >= pageSize) {
        flush(pages);
      }

      int paragraphSizeToWrite = paragraphSize - currentPosition;
      if (paragraphSizeToWrite <= 0) break;

      int pageSizeLeft = pageSize - printedSize;
      
      if (paragraphSizeToWrite > pageSizeLeft) {
        paragraphSizeToWrite = pageSizeLeft;
      }

      String withTags = paragraphText.substring(currentPosition, currentPosition + paragraphSizeToWrite);
      int len = paragraph.text.length();
      
      for (Pair<Integer, Integer> p : paragraph.italic) {
        withTags = paragraph.text.substring(0, p.getLeft()) +
          "<i>" + paragraph.text.substring(p.getLeft(), p.getRight()) + "</i>" +
          paragraph.text.substring(p.getRight(), len);
      }

      for (Pair<Integer, Integer> p : paragraph.strong) {
        withTags = paragraph.text.substring(0, p.getLeft()) +
          "<b>" + paragraph.text.substring(p.getLeft(), p.getRight()) + "</b>" +
          paragraph.text.substring(p.getRight(), len);
      }

      currentPage.append("\n" + Fb2Converter.PARAGRAPH_INDENT + withTags);
      currentPosition += paragraphSizeToWrite;
      printedSize += paragraphSizeToWrite;
    }
  }
}