package org.apache.logging.log4j.core.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConcurrentLRUCacheTest {

    @SuppressWarnings("rawtypes")
    @Test(expected=IllegalArgumentException.class)
    public void testConstructorDisallowsZeroCapacity() {
        new ConcurrentLRUCache(0);
    }

    @SuppressWarnings("rawtypes")
    @Test(expected=IllegalArgumentException.class)
    public void testConstructorDisallowsNegativeCapacity() {
        new ConcurrentLRUCache(-1);
    }

    @Test
    public void testGetReturnsNullForUnknownKey() {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<String, String>(44);
        assertNull("cache is empty", cache.get("a")); // cache is empty
        assertNull("cache is empty", cache.get("b")); // cache is empty
    }

    @Test
    public void testGetReturnsValueForKnownKey() {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<String, String>(44);
        assertNull("cache initially empty", cache.get("a")); // cache is initially empty
        
        cache.set("a", "aa");
        assertEquals("aa", cache.get("a")); // value now in cache
    }

    @Test
    public void testGetReturnsNullForLeastRecentlyUsedKeyWhenCapacityExceeded() {
        final int INITIAL_CAPACITY = 3;
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<String, String>(INITIAL_CAPACITY);
        cache.set("a", "aa");
        assertEquals("aa", cache.get("a")); // "a" most recently used

        cache.set("b", "bb"); // "a" is now 2nd recently used
        cache.set("c", "cc"); //"a" 3rd recently used
        cache.set("d", "dd"); // "a" least recently used & size exceeded capacity...
        assertNull("value for key a", cache.get("a")); // evicted from cache

        cache.set("e", "ee");
        assertNull("value for key b", cache.get("b")); // evicted from cache
    }

    @Test
    public void testSetAddsKeyValuePairToCache() {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<String, String>(3);
        assertEquals(0, cache.size());
        
        cache.set("a", "b");
    }

    @Test
    public void testSizeInitiallyReturnsXero() {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<String, String>(3);
        assertEquals(0, cache.size());
    }

    @Test
    public void testSizeReturnsNumberOfElements() {
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<String, String>(5);
        cache.set("a", "ab");
        assertEquals(1, cache.size());

        cache.set("b", "ab");
        assertEquals(2, cache.size());

        cache.set("c", "ab");
        assertEquals(3, cache.size());
    }

    @Test
    public void testSizeReturnsCapacityIfCapacityExceeded() {
        final int INITIAL_CAPACITY = 3;
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<String, String>(INITIAL_CAPACITY);
        assertEquals(0, cache.size());
        cache.set("a", "ab");
        assertEquals(1, cache.size());
        cache.set("b", "ab");
        assertEquals(2, cache.size());
        cache.set("c", "ab");
        assertEquals(3, cache.size());
        cache.set("d", "ab");
        assertEquals(3, cache.size()); // does not increase
        cache.set("e", "ab");
        assertEquals(3, cache.size()); // does not increase
    }

    @Test
    public void testCapacityReturnsConstructorValue() {
        final int INITIAL_CAPACITY = 15;
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<String, String>(INITIAL_CAPACITY);
        assertEquals(INITIAL_CAPACITY, cache.capacity());
        cache.set("a", "ab");
        assertEquals(INITIAL_CAPACITY, cache.capacity());

        cache.set("b", "ab");
        assertEquals(INITIAL_CAPACITY, cache.capacity());

        cache.set("c", "ab");
        assertEquals(INITIAL_CAPACITY, cache.capacity());
    }

    @Test
    public void testCapacityReturnsInitialValueRegardlessOfSize() {
        final int INITIAL_CAPACITY = 3;
        ConcurrentLRUCache<String, String> cache = new ConcurrentLRUCache<String, String>(INITIAL_CAPACITY);
        assertEquals(INITIAL_CAPACITY, cache.capacity());
        cache.set("a", "ab");
        assertEquals(INITIAL_CAPACITY, cache.capacity());
        cache.set("b", "ab");
        assertEquals(INITIAL_CAPACITY, cache.capacity());
        cache.set("c", "ab");
        assertEquals(INITIAL_CAPACITY, cache.capacity());
        cache.set("d", "ab");
        assertEquals(INITIAL_CAPACITY, cache.capacity());
        cache.set("e", "ab");
        assertEquals(INITIAL_CAPACITY, cache.capacity());
    }

}
