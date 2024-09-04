package cla33ic.casefetcher.cache;

import java.util.Optional;

public interface CacheService<K, V> {
    void put(K key, V value);
    Optional<V> get(K key);
    void remove(K key);
}