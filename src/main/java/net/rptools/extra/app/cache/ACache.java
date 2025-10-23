package net.rptools.extra.app.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class ACache {
    private static final com.github.benmanes.caffeine.cache.Cache<Object, Object> delegate = Caffeine.newBuilder()
            .initialCapacity(0)
            .maximumSize(500)
            .build();


    public static ConcurrentMap<Object, @NonNull Object> asMap() {
        return delegate.asMap();
    }

    public static void putAll(Map<?, ?> map) {
        delegate.putAll(map);
    }

    public static Map<Object, @NonNull Object> getAllPresent(Iterable<?> keys) {
        return delegate.getAllPresent(keys);
    }

    public static void invalidate(Object key) {
        delegate.invalidate(key);
    }

    public static void invalidateAll(Iterable<?> keys) {
        delegate.invalidateAll(keys);
    }
    public static Policy<Object, @NonNull Object> policy() {
        return delegate.policy();
    }
    public static Map<Object, @NonNull Object> getAll(Iterable<?> keys, Function<? super Set<?>, ? extends Map<?, ?>> mappingFunction) {
        return delegate.getAll(keys, mappingFunction);
    }

    public static void cleanUp() {
        delegate.cleanUp();
    }

    public static void invalidateAll() {
        delegate.invalidateAll();
    }

    @Nullable public static Object getIfPresent(Object key) {
        if(key == null) {
            return null;
        } else {
            return delegate.getIfPresent(key);
        }
    }
    @Nullable public static String getString(Object key) {
        while(key != null) {
            key = delegate.getIfPresent(key);
            if (key instanceof String string) {
                return string;
            }
        }
        return null;
    }
    @Nullable public static Image getImage(Object key) {
        while(key != null) {
            key = delegate.getIfPresent(key);
            if (key instanceof Image image) {
                return image;
            }
        }
        return null;
    }

    public static void put(Object key, Object value) {
        if(value == null){
            return;
        }
        delegate.put(key, value);
    }

    public static CacheStats stats() {
        return delegate.stats();
    }

    public static Object get(Object key, Function<? super Object, ?> mappingFunction) {
        return delegate.get(key, mappingFunction);
    }

    public static long estimatedSize() {
        return delegate.estimatedSize();
    }
}
