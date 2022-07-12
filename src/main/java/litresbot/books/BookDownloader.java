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

import org.apache.log4j.Logger;

import litresbot.http.HttpClientWithProxy;

public class BookDownloader
{
  final static Logger logger = Logger.getLogger(BookDownloader.class);
  final static HttpClientWithProxy http = new HttpClientWithProxy();

  public static String folder = "./tmp";

  public static void setDownloadPath(String path) {
    File dir = new File(path);
    logger.info("Setting book downloader folder to: " + dir.getAbsolutePath());
    if (!dir.exists()) {
      logger.info("Book downloader folder does not exist. Creating new folder.");
      if (!dir.mkdirs()) {
        logger.warn("Book downloader folder creating failed.");
      }
    }
    BookDownloader.folder = path;
  }
  
  public static byte[] download(String root, String bookUrl, String fileName) throws IOException
  {
    byte[] book = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    File file = new File(folder + "/" + fileName);
    if(file.exists() && file.canRead())
    {
      logger.info("book " + bookUrl + " found in files");
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
    InputStream bookData = http.getDownloadStream(url);
    
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
      logger.info("book " + bookUrl + " found in cache");
      return book;
    }
    
    book = download(root, bookUrl, fileName);
    
    BookContentCache.addBook(bookUrl, book);
    return book;
  }

  public static byte[] unzipBook(byte[] content, String filename) throws IOException
  {    
    if(!filename.endsWith(".zip"))
    {
      throw new IOException("Not a zip archive. File: " + filename);
    }
    
    String filenameStripped = filename.replace(".zip", "");
    String format = FileExtensions.detectExtension(filenameStripped);
    
    if(format == null)
    {
      throw new IOException("Not supported format. File: " + filename);
    }
    
    ByteArrayInputStream fileStream = new ByteArrayInputStream(content);
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
        throw new IOException("Book not found in zip archive. File: " + filename);
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
