package litresbot.flibusta;

import java.io.IOException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class BooksCache
{
  public static final int CACHE_SIZE = 20;
  
  private static Cache<String, byte[]> booksCache;
  static
  {
    booksCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();
  }
  
  public static byte[] getBookFromId(String id)
  {
    return booksCache.getIfPresent(id);
  }
  
  public static void addBook(String id, byte[] book) throws IOException
  {
    booksCache.put(id, book);
  }
}
