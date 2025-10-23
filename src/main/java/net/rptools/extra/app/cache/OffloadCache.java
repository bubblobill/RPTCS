package net.rptools.extra.app.cache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

public class OffloadCache {
    CachingProvider cachingProvider = Caching.getCachingProvider();
    CacheManager cacheManager = cachingProvider.getCacheManager();
    MutableConfiguration<String, String> config = new MutableConfiguration<>();
    Cache<String, String> cache = cacheManager.createCache("simpleCache", config);

//    cache.put("key1", "value1");
//    cache.put("key2", "value2");
//    cacheManager.close();
}
