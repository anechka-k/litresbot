package litresbot.books;

// this is an entry of a single book file URL

public class BookFileLink
{
  public String href;
  public String format;
  
  public BookFileLink() { }

  public BookFileLink(BookFileLink another) {
    this.href = another.href;
    this.format = another.format;
  }
}
