package litresbot.flibusta;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class BookContentCache
{
  static public class CacheRecord
  {
    String id;
    byte[] content;
  }

  static Cache<String, CacheRecord> cache = CacheBuilder.newBuilder().
      maximumSize(1000).
      expireAfterAccess(20, TimeUnit.DAYS).
      build();
}
