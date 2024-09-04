package cla33ic.casefetcher.cache;

import cla33ic.casefetcher.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCacheService<K, V> implements CacheService<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryCacheService.class);

    private final Map<K, CacheEntry<V>> cache;

    public InMemoryCacheService() {
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value));
        logger.debug("Added to cache: key={}", key);
    }

    @Override
    public Optional<V> get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            logger.debug("Cache hit: key={}", key);
            return Optional.of(entry.getValue());
        } else {
            logger.debug("Cache miss: key={}", key);
            if (entry != null) {
                remove(key);
            }
            return Optional.empty();
        }
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
        logger.debug("Removed from cache: key={}", key);
    }

    private static class CacheEntry<V> {
        private final V value;
        private final LocalDateTime expirationTime;

        public CacheEntry(V value) {
            this.value = value;
            this.expirationTime = LocalDateTime.now().plusHours(AppConfig.CACHE_EXPIRATION_HOURS);
        }

        public V getValue() {
            return value;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expirationTime);
        }
    }
}