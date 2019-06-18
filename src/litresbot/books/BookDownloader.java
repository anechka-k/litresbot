package litresbot.books;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import litresbot.http.HttpClientWithProxy;
import litresbot.http.HttpSourceType;
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
    InputStream bookData = HttpClientWithProxy.download(url, fileName, HttpSourceType.BOOKS);
    
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
    byte[] book = BookContentCache.getBookFromId(bookUrl);
    if(book != null)
    {
      Logger.logInfoMessage("book " + bookUrl + " found in cache");
      return book;
    }
    
    book = download(root, bookUrl, fileName);
    
    BookContentCache.addBook(bookUrl, book);
    return book;
  }
  
  public static byte[] downloadUnzipBook(String root, String bookUrl, String fileName) throws IOException
  {
    byte[] bookZip = downloadWithCache(root, bookUrl, fileName);
    
    if(bookZip == null)
    {
      throw new IOException("Could not download book. URL: " + bookUrl);
    }
    
    // now unzip book in books folder
    
    if(!fileName.endsWith(".zip"))
    {
      throw new IOException("Not a zip archive. URL: " + bookUrl);
    }
    
    String filenameStripped = fileName.replace(".zip", "");
    String format = FileExtensions.detectExtension(filenameStripped);
    
    if(format == null)
    {
      throw new IOException("Not supported format. URL: " + bookUrl);
    }
    
    ByteArrayInputStream fileStream = new ByteArrayInputStream(bookZip);
    ZipInputStream zis = new ZipInputStream(fileStream);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
      
    try
    {
      ZipEntry zipEntry = null;
          
      while(true)
      {
        zipEntry = zis.getNextEntry();
        if(zipEntry == null) break;
            
        String zipEntryName = zipEntry.getName();
            
        if(zipEntryName.toLowerCase().endsWith("." + format)) break;
      }
          
      if(zipEntry == null)
      {
        throw new IOException("Book not found in zip archive. URL: " + bookUrl);
      }
          
      byte[] buffer = new byte[1024];
          
      File newFile = new File(BookDownloader.folder + "/" + filenameStripped);
      FileOutputStream fos = new FileOutputStream(newFile);
          
      try
      {
        int len;
        while ((len = zis.read(buffer)) > 0)
        {
          fos.write(buffer, 0, len);
          baos.write(buffer, 0, len);
        }
      }
      finally
      { 
        fos.close();
      }
    }
    finally
    {
      zis.closeEntry();
      zis.close();
    }
        
    byte[] book = baos.toByteArray();
    return book;
  }
}
