package litresbot.books;

import java.io.IOException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class BookContentCache
{
  public static final int CACHE_SIZE = 20;
  
  private static Cache<String, byte[]> cache;
  static
  {
    cache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();
  }
  
  public static byte[] getBookFromId(String id)
  {
    return cache.getIfPresent(id);
  }
  
  public static void addBook(String id, byte[] book) throws IOException
  {
    cache.put(id, book);
  }
}
