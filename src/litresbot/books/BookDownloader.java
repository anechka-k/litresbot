package litresbot.books;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import litresbot.HttpClientWithProxy;
import litresbot.util.Logger;

public class BookDownloader
{
  public static String folder = "./tmp";
  
  public static byte[] download(String root, String bookUrl, String fileName) throws IOException
  {
    byte[] book = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    File file = new File(folder + "/" + fileName);
    if(file.exists() && file.canRead())
    {
      Logger.logInfoMessage("book " + bookUrl + " found in files");
      FileInputStream fis = new FileInputStream(file);
      BufferedInputStream bufferedInputStream = new BufferedInputStream(fis);
      
      byte data[] = new byte[1024];
      int read;
      
      try
      {
        while((read = bufferedInputStream.read(data, 0, 1024)) >= 0)
        {
          baos.write(data, 0, read);
        }
      }
      finally
      {
        fis.close();
      }
      
      book = baos.toByteArray();    
      return book;
    }
    
    String url = root + bookUrl;
    InputStream bookData = HttpClientWithProxy.download(url, fileName);
    
    BufferedInputStream in = new BufferedInputStream(bookData);
    FileOutputStream fos = new FileOutputStream(folder + "/" + fileName);
    BufferedOutputStream bout = new BufferedOutputStream(fos);
    
    byte data[] = new byte[1024];
    int read;
    
    try
    {
      while((read = in.read(data, 0, 1024)) >= 0)
      {
        bout.write(data, 0, read);
        baos.write(data, 0, read);
      }
    }
    finally
    {
      bout.close();
      in.close();
    }
    
    book = baos.toByteArray();    
    return book;
  }
  
  public static byte[] downloadWithCache(String root, String bookUrl, String fileName) throws IOException
  {
    byte[] book = BookCache.getBookFromId(bookUrl);
    if(book != null)
    {
      Logger.logInfoMessage("book " + bookUrl + " found in cache");
      return book;
    }
    
    book = download(root, bookUrl, fileName);
    
    BookCache.addBook(bookUrl, book);
    return book;
  }
}
