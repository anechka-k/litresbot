package litresbot.books;

import java.util.ArrayList;
import java.util.List;

// this is an entry of a single book

public class BookInfo
{
  public String id;
  public String title;
  public String author;
  public String site;
  public String annotation;
  
  public List<BookFileLink> links;
  
  public BookInfo() { }

  public BookInfo(BookInfo another) {
    this.id = another.id;
    this.title = another.title;
    this.author = another.author;
    this.site = another.site;
    this.annotation = another.annotation;
    if (another.links != null) {
      // make a deep copy of the links
      this.links = new ArrayList<BookFileLink>(another.links.size());
      for (BookFileLink l : another.links) {
        this.links.add(new BookFileLink(l));
      }
    }
  }
}
