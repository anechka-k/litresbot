package litresbot.books.convert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class TelegramParagraphPrinter extends TextParagraphPrinter {

  int printedSize = 0;

  // add unfinished page to the pages list
  public void flush(List<String> pages) {
    if (printedSize == 0) return;
    pages.add(currentPage.toString());
    currentPage = new StringBuilder();
    printedSize = 0;
  }

  ///TODO: think about escaping text
  ///TODO: check unsupported tags handling
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

      String paragraphPage = paragraphText.substring(currentPosition, currentPosition + paragraphSizeToWrite);
      List<TagPosition> shiftedTags = new ArrayList<>();
      int shiftRight = ("\n" + Fb2Converter.PARAGRAPH_INDENT).length();
      for (TagPosition t : paragraph.tags) {
        if ((t.from + shiftRight) < currentPosition) continue;
        TagPosition newTag = new TagPosition();
        newTag.from = t.from + shiftRight - currentPosition;
        newTag.to = t.to + shiftRight - currentPosition;
        newTag.type = t.type;
        shiftedTags.add(newTag);
      }
      String withTags = TagsInserter.insertTags(paragraphPage, shiftedTags);

      currentPage.append(withTags);
      currentPosition += paragraphSizeToWrite;
      printedSize += paragraphSizeToWrite;
    }
  }
}