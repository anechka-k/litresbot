package litresbot.books.convert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class TelegramParagraphPrinter extends TextParagraphPrinter {

  int printedSize = 0;
  int tooLongWordMaxSize = 200;

  // add unfinished page to the pages list
  public void flush(List<String> pages) {
    if (printedSize == 0) return;
    pages.add(currentPage.toString());
    currentPage = new StringBuilder();
    printedSize = 0;
  }

  ///TODO: think about escaping text
  ///TODO: check unsupported tags handling

  public void printParagraph(ParagraphNode paragraph, List<String> pages, boolean fromTitle) throws IOException {
    int currentPosition = 0;
    int initialSpaces = 0;
    boolean paragraphStart = true;

    while (true) {
      if (printedSize >= pageSize) {
        flush(pages);
      }

      int pageSizeLeft = pageSize - printedSize;

      int paragraphSizeToWrite = paragraph.text.length() - currentPosition;
      if (paragraphSizeToWrite <= 0) break;
      
      if (paragraphSizeToWrite > pageSizeLeft) {
        paragraphSizeToWrite = pageSizeLeft;
      }

      // now start truncating paragraph.text
      String truncParagraphPage = "";
      for (int i = currentPosition; i < paragraph.text.length(); i++) {
        char letter = paragraph.text.charAt(i);
        if (letter == ' ') {
          if (truncParagraphPage.isEmpty()) {
            initialSpaces++;
            continue;
          }
          if (i > currentPosition + paragraphSizeToWrite + initialSpaces) {
            break;
          }
        }
        truncParagraphPage += letter;
        if (i > currentPosition + paragraphSizeToWrite + tooLongWordMaxSize + initialSpaces) {
          break;
        }
      }

      List<TagPosition> shiftedTags = new ArrayList<>();

      for (TagPosition t : paragraph.tags) {
        if (t.from < (currentPosition + initialSpaces)) continue;
        TagPosition newTag = new TagPosition();
        newTag.from = t.from - currentPosition - initialSpaces;
        newTag.to = t.to - currentPosition - initialSpaces;
        newTag.type = t.type;
        shiftedTags.add(newTag);
      }
      String withTags = TagsInserter.insertTags(truncParagraphPage, shiftedTags);

      if (paragraphStart) {
        withTags = "\n" + Fb2Converter.PARAGRAPH_INDENT + withTags;
        paragraphStart = false;
      }

      if (fromTitle) {
        if (!pages.isEmpty()) {
          withTags = "\n" + withTags;
        }
        withTags = "<b>" + withTags + "</b>\n";
      }

      currentPage.append(withTags);

      printedSize += truncParagraphPage.length();
      currentPosition += (truncParagraphPage.length() + initialSpaces);
    }
  }
}