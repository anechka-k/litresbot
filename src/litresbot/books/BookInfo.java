package litresbot.books;

import java.util.List;

// this is an entry of a single book

public class BookInfo
{
  public String id;
  public String title;
  public String author;
  public String site;
  
  public List<BookFileLink> links;
  
  public BookInfo()
  {
  }
}
