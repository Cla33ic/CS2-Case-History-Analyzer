package cla33ic.casefetcher.cache;

import java.util.Optional;

/**
 * Interface for a cache service.
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */
public interface CacheService<K, V> {
    void put(K key, V value);
    Optional<V> get(K key);
    void remove(K key);
}