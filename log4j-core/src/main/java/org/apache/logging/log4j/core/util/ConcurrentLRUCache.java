/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package org.apache.logging.log4j.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe implementation of a Least Recently Used cache.
 * <p>
 * The map part is supplied by {@code java.util.concurrent.ConcurrentHashMap}, the tracking of the least recently used
 * element is done with code based on Doug Lea's <a
 * href="http://www.java2s.com/Code/Java/Collections-Data-Structure/ConcurrentDoublyLinkedList.htm"
 * >ConcurrentDoublyLinkedList</a>.
 */
public class ConcurrentLRUCache<K, V> {

    private final Map<K, ConcurrentLRUCache.Node<K, V>> table = new ConcurrentHashMap<K, ConcurrentLRUCache.Node<K, V>>();
    private final Node<K, V> header;
    private final Node<K, V> trailer; // the least recently used element
    private final int capacity;

    /**
     * Constructs an empty cache.
     * 
     * @param capacity the maximum number of key-value pairs this cache is allowed to contain
     */
    public ConcurrentLRUCache(final int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive number but was " + capacity);
        }
        Node<K, V> h = new Node<K, V>(null, null, null, null);
        Node<K, V> t = new Node<K, V>(null, null, null, h);
        h.setNext(t);
        this.header = h;
        this.trailer = t;
        this.capacity = capacity;
    }

    public V get(final K key) {
        Node<K, V> node = table.get(key);
        if (node != null) {
            if (node.delete()) {
                // make this the new header
                while (header.append(key, node.element) == null) {
                    // loop until success
                }
            } // delete failed: some other thread already re-inserted this at the head
            return node.element;
        } else {
            return null;
        }
    }

    public void set(final K key, final V value) {
        Node<K, V> cur = table.get(key);
        if (cur != null) {
            cur.delete(); // move the most recently used node to the front
            while (header.append(key, value) == null) {
                // loop until success
            }
        } else {
            // most recently used node to the front
            while ((cur = header.append(key, value)) == null) {
                // loop until success
            }
            table.put(key, cur);
            while (table.size() > capacity) {
                final Node<K, V> leastRecentlyUsed = trailer.back();
                if (leastRecentlyUsed != null && leastRecentlyUsed.delete()) {
                    // At most one thread succeeds in deleting this LRU node.
                    // That thread then removes its key from the table.
                    // Worst case, multiple threads all remove LRU nodes from the table,
                    // resulting in table.size being (capacity - threadCount).
                    // This is not a problem.
                    table.remove(leastRecentlyUsed.key);
                }
            }
        }
    }

    /**
     * Returns the maximum number of key-value mappings in this cache.
     * 
     * @return the maximum number of key-value mappings in this cache
     */
    public int capacity() {
        return this.capacity;
    }

    /**
     * Returns the number of key-value mappings in this cache.
     * 
     * @return the number of key-value mappings in this cache.
     */
    public int size() {
        return table.size();
    }

    /**
     * Linked Nodes. As a minor efficiency hack, this class opportunistically inherits from AtomicReference, with the
     * atomic ref used as the "next" link.
     * <p>
     * Nodes are in doubly-linked lists. There are three kinds of special nodes, distinguished by:
     * <ul>
     * <li>The list header has a null prev link
     * <li>The list trailer has a null next link
     * <li>A deletion marker has a prev link pointing to itself.
     * </ul>
     * All three kinds of special nodes have null element fields.
     * <p>
     * Regular nodes have non-null element, next, and prev fields. To avoid visible inconsistencies when deletions
     * overlap element replacement, replacements are done by replacing the node, not just setting the element.
     * <p>
     * Nodes can be traversed by read-only ConcurrentLinkedDeque class operations just by following raw next pointers,
     * so long as they ignore any special nodes seen along the way. (This is automated in method forward.) However,
     * traversal using prev pointers is not guaranteed to see all live nodes since a prev pointer of a deleted node can
     * become unrecoverably stale.
     */
    static class Node<KEY, VAL> extends AtomicReference<Node<KEY, VAL>> {
        private static final long serialVersionUID = -1798997222261941417L;

        private volatile Node<KEY, VAL> prev;

        final KEY key;
        final VAL element;

        /** Creates a node with given contents */
        Node(KEY key, VAL element, Node<KEY, VAL> next, Node<KEY, VAL> prev) {
            super(next);
            this.prev = prev;
            this.key = key;
            this.element = element;
        }

        /** Creates a marker node with given successor */
        Node(Node<KEY, VAL> next) {
            super(next);
            this.prev = this;
            this.key = null;
            this.element = null;
        }

        /**
         * Gets next link (which is actually the value held as atomic reference).
         */
        private Node<KEY, VAL> getNext() {
            return get();
        }

        /**
         * Sets next link
         * 
         * @param n the next node
         */
        void setNext(Node<KEY, VAL> n) {
            set(n);
        }

        /**
         * compareAndSet next link
         */
        private boolean casNext(Node<KEY, VAL> cmp, Node<KEY, VAL> val) {
            return compareAndSet(cmp, val);
        }

        /**
         * Gets prev link
         */
        private Node<KEY, VAL> getPrev() {
            return prev;
        }

        /**
         * Sets prev link
         * 
         * @param b the previous node
         */
        void setPrev(Node<KEY, VAL> b) {
            prev = b;
        }

        /**
         * Returns true if this is a header, trailer, or marker node
         */
        boolean isSpecial() {
            return element == null;
        }

        /**
         * Returns true if this is a trailer node
         */
        boolean isTrailer() {
            return getNext() == null;
        }

        /**
         * Returns true if this is a header node
         */
        boolean isHeader() {
            return getPrev() == null;
        }

        /**
         * Returns true if this is a marker node
         */
        boolean isMarker() {
            return getPrev() == this;
        }

        /**
         * Returns true if this node is followed by a marker, meaning that it is deleted.
         * 
         * @return true if this node is deleted
         */
        boolean isDeleted() {
            Node<KEY, VAL> f = getNext();
            return f != null && f.isMarker();
        }

        /**
         * Returns next node, ignoring deletion marker
         */
        private Node<KEY, VAL> nextNonmarker() {
            Node<KEY, VAL> f = getNext();
            return (f == null || !f.isMarker()) ? f : f.getNext();
        }

        /**
         * Returns the next non-deleted node, swinging next pointer around any encountered deleted nodes, and also
         * patching up successor''s prev link to point back to this. Returns null if this node is trailer so has no
         * successor.
         * 
         * @return successor, or null if no such
         */
        Node<KEY, VAL> successor() {
            Node<KEY, VAL> f = nextNonmarker();
            for (;;) {
                if (f == null)
                    return null;
                if (!f.isDeleted()) {
                    if (f.getPrev() != this && !isDeleted())
                        f.setPrev(this); // relink f's prev
                    return f;
                }
                Node<KEY, VAL> s = f.nextNonmarker();
                if (f == getNext())
                    casNext(f, s); // unlink f
                f = s;
            }
        }

        /**
         * Returns the apparent predecessor of target by searching forward for it starting at this node, patching up
         * pointers while traversing. Used by predecessor().
         * 
         * @return target's predecessor, or null if not found
         */
        private Node<KEY, VAL> findPredecessorOf(Node<KEY, VAL> target) {
            Node<KEY, VAL> n = this;
            for (;;) {
                Node<KEY, VAL> f = n.successor();
                if (f == target)
                    return n;
                if (f == null)
                    return null;
                n = f;
            }
        }

        /**
         * Returns the previous non-deleted node, patching up pointers as needed. Returns null if this node is header so
         * has no successor. May also return null if this node is deleted, so doesn't have a distinct predecessor.
         * 
         * @return predecessor or null if not found
         */
        Node<KEY, VAL> predecessor() {
            Node<KEY, VAL> n = this;
            for (;;) {
                Node<KEY, VAL> b = n.getPrev();
                if (b == null)
                    return n.findPredecessorOf(this);
                Node<KEY, VAL> s = b.getNext();
                if (s == this)
                    return b;
                if (s == null || !s.isMarker()) {
                    Node<KEY, VAL> p = b.findPredecessorOf(this);
                    if (p != null)
                        return p;
                }
                n = b;
            }
        }

        /**
         * Returns the next node containing a nondeleted user element. Use for forward list traversal.
         * 
         * @return successor, or null if no such
         */
        Node<KEY, VAL> forward() {
            Node<KEY, VAL> f = successor();
            return (f == null || f.isSpecial()) ? null : f;
        }

        /**
         * Returns previous node containing a nondeleted user element, if possible. Use for backward list traversal, but
         * beware that if this method is called from a deleted node, it might not be able to determine a usable
         * predecessor.
         * 
         * @return predecessor, or null if no such could be found
         */
        Node<KEY, VAL> back() {
            Node<KEY, VAL> f = predecessor();
            return (f == null || f.isSpecial()) ? null : f;
        }

        /**
         * Tries to insert a node holding element as successor, failing if this node is deleted.
         * 
         * @param element the element
         * @return the new node, or null on failure.
         */
        Node<KEY, VAL> append(KEY key, VAL element) {
            for (;;) {
                Node<KEY, VAL> f = getNext();
                if (f == null || f.isMarker())
                    return null;
                Node<KEY, VAL> x = new Node<KEY, VAL>(key, element, f, this);
                if (casNext(f, x)) {
                    f.setPrev(x); // optimistically link
                    return x;
                }
            }
        }

        /**
         * Tries to insert a node holding element as predecessor, failing if no live predecessor can be found to link
         * to.
         * 
         * @param element the element
         * @return the new node, or null on failure.
         */
        Node<KEY, VAL> prepend(KEY key, VAL element) {
            for (;;) {
                Node<KEY, VAL> b = predecessor();
                if (b == null)
                    return null;
                Node<KEY, VAL> x = new Node<KEY, VAL>(key, element, this, b);
                if (b.casNext(this, x)) {
                    setPrev(x); // optimistically link
                    return x;
                }
            }
        }

        /**
         * Tries to mark this node as deleted, failing if already deleted or if this node is header or trailer
         * 
         * @return true if successful
         */
        boolean delete() {
            Node<KEY, VAL> b = getPrev();
            Node<KEY, VAL> f = getNext();
            if (b != null && f != null && !f.isMarker() && casNext(f, new Node<KEY, VAL>(f))) {
                if (b.casNext(this, f))
                    f.setPrev(b);
                return true;
            }
            return false;
        }

        /**
         * Tries to insert a node holding element to replace this node. Failing if already deleted.
         * 
         * @param newElement the new element
         * @return the new node, or null on failure.
         */
        Node<KEY, VAL> replace(KEY key, VAL newElement) {
            for (;;) {
                Node<KEY, VAL> b = getPrev();
                Node<KEY, VAL> f = getNext();
                if (b == null || f == null || f.isMarker())
                    return null;
                Node<KEY, VAL> x = new Node<KEY, VAL>(key, newElement, f, b);
                if (casNext(f, new Node<KEY, VAL>(x))) {
                    b.successor(); // to relink b
                    x.successor(); // to relink f
                    return x;
                }
            }
        }
        
        @Override
        public String toString() {
            if (isHeader()) {
                return "header";
            } 
            if (isTrailer()) {
                return "trailer";
            }
            if (isMarker()) {
                return key + "=" + element + " (marker)";
            }
            if (isDeleted()) {
                return key + "=" + element + " (deleted)";
            }
            return key + "=" + element;
        }
    }
}