package litresbot.books.convert;

import java.io.IOException;
import java.util.List;

class TextParagraphPrinter {
  public int pageSize;
  public String indent;

  protected StringBuilder currentPage = new StringBuilder();

  // add unfinished page to the pages list
  public void flush(List<String> pages) {
    if (currentPage.length() == 0) return;
    pages.add(currentPage.toString());
    currentPage = new StringBuilder();
  }

  public void printParagraph(ParagraphNode paragraph, List<String> pages, boolean fromTitle) throws IOException {
    String paragraphText = "\n" + Fb2Converter.PARAGRAPH_INDENT + paragraph.text;
    int paragraphSize = paragraphText.length();
    int currentPosition = 0;

    while (true) {
      if (currentPage.length() >= pageSize) {
        flush(pages);
      }

      int paragraphSizeToWrite = paragraphSize - currentPosition;
      if (paragraphSizeToWrite <= 0) break;

      int pageSizeLeft = pageSize - currentPage.length();
      
      if (paragraphSizeToWrite > pageSizeLeft) {
        paragraphSizeToWrite = pageSizeLeft;
      }

      currentPage.append(paragraphText.substring(currentPosition, currentPosition + paragraphSizeToWrite));
      currentPosition += paragraphSizeToWrite;
    }
  }
}