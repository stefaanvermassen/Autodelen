package database.cache.EHCache;

import database.cache.CacheWrapper;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * Created by Benjamin on 19/02/14.
 */
public class EHCacheWrapper<K, V> implements CacheWrapper<K, V> {

    private final String cacheName;
    private final CacheManager manager;

    public EHCacheWrapper(final String cacheName, final CacheManager manager) {
        this.cacheName = cacheName;
        this.manager = manager;
    }

    @Override
    public void put(K key, V value) {
        if(!(getCache() == null))
            getCache().put(new Element(key, value));
    }

    @Override
    public V get(K key) {
        Element element = getCache().get(key);
        if (element != null) {
            return (V) element.getObjectValue();
        }
        return null;
    }

    public Ehcache getCache() {
        return manager.getCache(cacheName);
    }
}
