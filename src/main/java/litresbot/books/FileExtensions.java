package litresbot.books;

public class FileExtensions
{
  public static String[] supportexExt =
  {
    "fb2",
    "fb3",
    "pdf",
    "epub",
    "mobi",
    "txt",
    "doc",
    "docx",
    "html",
    "htm",
    "zip",
    "rar",
    "chm",
    "djvu",
    "iso",
    "lit",
    "lrf",
    "mp3",
    "png",
    "ppt",
    "rtf",
    "tar",
    "xml",
    "word",
    "tex",
    "rgo",
    "prc",
    "pdg",
    "pdb",
    "odt",
    "mhtm",
    "mht",
    "7zip",
    "azw",
    "azw3",
    "cbr",
    "cbz",
    "dat",
    "dic",
    "djv",
    "exe",
    "jpg",
    "ha"
  };
  
  public static String detectFormat(String downloadString)
  {
    downloadString = downloadString.toLowerCase();
    
    for(String ext : supportexExt)
    {
      if(downloadString.startsWith(ext))
      {
        return ext;
      }
    }
    
    return null;
  }
  
  public static String detectExtension(String filename)
  {
    filename = filename.toLowerCase();
    
    for(String ext : supportexExt)
    {
      if(filename.endsWith("." + ext))
      {
        return ext;
      }
    }
    
    return null;
  }

  // convert ext.zip to ext
  public static BookInfo stripZipFromExtensions(BookInfo book) {
    BookInfo strippedBook = new BookInfo(book);
    for(BookFileLink l : strippedBook.links) {
      String formatType = FileExtensions.detectFormat(l.format);
      l.format = formatType;
    }
    return strippedBook;
  }

  public static String getUrlShortFromBook(BookInfo book, String format) {    
    format = format.toLowerCase();
    
    for(BookFileLink link : book.links) {
      String linkFormat = FileExtensions.detectFormat(link.format);
      if(linkFormat == null) continue;
      if(!linkFormat.contentEquals(format)) continue;
      
      return link.href;
    }
    
    return null;
  }

  public static String getUrlFromBook(BookInfo book, String format) {
    String root = book.site;
    if(root == null) return null;
    
    String url = getUrlShortFromBook(book, format);
    if(url == null) return null;
    
    return root + url;
  }
   
  public static String getFilenameFromBook(BookInfo book, String format) {    
    for(BookFileLink link : book.links) {
      String linkFormat = FileExtensions.detectFormat(link.format);
      if(linkFormat == null) continue;
      if(!linkFormat.contentEquals(format)) continue;
      
      return book.id + "." + link.format;
    }
    
    return book.id;
  }
}
