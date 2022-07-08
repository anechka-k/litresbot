package litresbot.opdssearch.flibusta;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import litresbot.opdssearch.opds.Entry;

public class FlibustaOpdsCache
{
  static public class CacheRecord
  {
    String url;
    List<Entry> entries;
  }

  static Cache<String, CacheRecord> cache = CacheBuilder.newBuilder().
      maximumSize(1000).
      expireAfterAccess(1, TimeUnit.DAYS).
      build();
}
