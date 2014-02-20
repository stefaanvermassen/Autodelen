package database.cache;

/**
 * Created by Benjamin on 19/02/14.
 */
public interface CacheWrapper<K, V> {
    public void put(K key, V value);
    public V get(K key);
}