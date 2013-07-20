/*
 * Copyright (c) 1998-2002 Carnegie Mellon University.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY CARNEGIE MELLON UNIVERSITY ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package robotutils.util;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

/**
 * Priority queue.  Objects stored in a priority queue must implement 
 * the Prioritized interface.
 * 
 */
public class PriorityQueue<E> {

    /**
     * Attempts to generate a natural comparator to try to use if necessary.
     */
    private final Comparator NATURAL_COMPARATOR = new Comparator<Comparable<E> >() {
        public int compare(Comparable o1, Comparable o2) {
            return o1.compareTo(o2);
        }
    };

    /**
     * The queue of elements.
     */
    private Vector<E> _queue;

    /**
     * A backing hashmap used to find elements
     */
    private HashMap<E, Integer> _hashmap;

    /**
     * The comparator used to order elements.
     */
    private Comparator _comparator;

    /**
     * Make an empty PriorityQueue.
     */
    public PriorityQueue() {
        _queue = new Vector();
        _hashmap = new HashMap();
        _comparator = NATURAL_COMPARATOR;
    }

    /**
     * Make an empty PriorityQueue.
     * @param comparator comparison operator to use when ordering elements
     */
    public PriorityQueue(Comparator<? super E> comparator) {
        _queue = new Vector();
        _hashmap = new HashMap();
        _comparator = comparator;
    }

    /**
     * Make an empty PriorityQueue with an initial capacity.
     * @param initialCapacity number of elements initially allocated in queue
     */
    public PriorityQueue(int initialCapacity) {
        _queue = new Vector(initialCapacity);
        _hashmap = new HashMap(initialCapacity);
        _comparator = NATURAL_COMPARATOR;
    }

    /**
     * Make an empty PriorityQueue with an initial capacity.
     * @param initialCapacity number of elements initially allocated in queue
     * @param comparator comparison operator to use when ordering elements
     */
    public PriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
        _queue = new Vector(initialCapacity);
        _hashmap = new HashMap(initialCapacity);
        _comparator = comparator;
    }

    /**
     * Make a copy of another PriorityQueue.
     * @param c the PriorityQueue that will be cloned
     */
    public PriorityQueue(PriorityQueue<? extends E> c) {
        _queue = (Vector)c._queue.clone();
        _hashmap = (HashMap)c._hashmap.clone();
        _comparator = c._comparator;
    }

    /**
     * Put an object on the queue.  Doesn't check for
     * duplicate puts.
     * @param x object to put on the queue 
     */
    public synchronized void add(E x) {
        int newSize = _queue.size()+1;
        _queue.setSize(newSize);

        int i, p;
        for (i=newSize-1, p = ((i+1)/2)-1; // i's parent
             i > 0 && _comparator.compare(_queue.get(p), x) > 0;
             i = p, p = ((i+1)/2)-1) {
            _queue.setElementAt(_queue.get(p), i);
            _hashmap.put(_queue.get(p), i);
        }

        _queue.setElementAt(x, i);
        _hashmap.put(x, i);
    }

    /**
     * Get object with lowest priority from queue.
     * @return object with lowest priority, or null if queue is empty
     */
    public synchronized E peek() {
        return !isEmpty() ? _queue.get(0) : null;
    }

    /**
     * Get and delete the object with lowest priority.
     * @return object with lowest priority, or null if queue is empty
     */
    public synchronized E poll() {
        if (isEmpty()) {
            return null;
        }
        
        E obj = _queue.get(0);
        deleteElement(0);
        return obj;
    }

    /**
     * Delete an object from queue.  If object was inserted more than
     * once, this method deletes only one occurrence of it.
     * @param x object to delete
     * @return true if x was found and deleted, false if x not found in queue
     */
    public synchronized boolean remove(E x) {
        int i = _queue.indexOf(x);
        if (i == -1) {
            return false;
        }
        
        deleteElement(i);
        return true;
    }

    /**
     * Remove all objects from queue.
     */
    public synchronized void clear() {
        _queue.clear();
        _hashmap.clear();
    }

    
    /**
     * Enumerate the objects in the queue, in no particular order
     * @return enumeration of objects in queue
     */
    public synchronized Enumeration elements() {
        return _queue.elements();
    }

    
    /**
     * Get number of objects in queue.
     * @return number of objects
     */
    public synchronized int size() {
        return _queue.size();
    }
    
    /**
     * Test whether queue is empty.
     * @return true iff queue is empty.
     */
    public synchronized boolean isEmpty() {
        return _queue.isEmpty();
    }

    /**
     * Rebuild priority queue if the priority of at most one element
     * has changed since insertion.
     */
    public synchronized void update(E x) {
        Integer i = _hashmap.get(x);
        if (i == null) {
            throw new IllegalArgumentException("Attempted to update unknown state");
        }

        // If we are using equality to locate objects, presumably the updated
        // object reference should also be used from now on.
        _queue.set(i, x);
        sift(i, x);
    }

    /**
     * Checks whether element is currently somewhere in queue.
     * @param x the element for which to test
     * @return {@code true} if the element is contained in the queue
     */
    public synchronized boolean contains(E x) {
        return _hashmap.containsKey(x);
    }

    final void deleteElement(int i) {
        int last = _queue.size()-1;

        _hashmap.remove(_queue.get(i));
        E x = _queue.get(last);

        _queue.setElementAt(x, i);
        _queue.setElementAt(null, last); // avoid holding extra reference
        _queue.setSize(last);

        if (last != i) {
            _hashmap.put(x, i);
            sift(i, x);
        }
    }

    final void sift(int k, E x) {
        // Try to sort down.  If that doesn't do anything, sort up.
        siftDown(k, x);
        if (_queue.get(k) == x) {
            siftUp(k, x);
        }
    }

    final void siftUp(int k, E x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            E e = _queue.get(parent);
            if (_comparator.compare(x, (E) e) >= 0)
                break;
            _queue.set(k, e);
            _hashmap.put(e, k);
            k = parent;
        }
        _queue.set(k, x);
        _hashmap.put(x, k);
    }

    final void siftDown(int k, E x) {
        int half = size() >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            E c = _queue.get(child);
            int right = child + 1;
            if (right < size() &&
                _comparator.compare(c, _queue.get(right)) > 0)
                c = _queue.get(child = right);
            if (_comparator.compare(x, c) <= 0)
                break;
            _queue.set(k, c);
            _hashmap.put(c, k);
            k = child;
        }
        _queue.set(k, x);
        _hashmap.put(x, k);
    }

    /**
     * Establishes the heap property over the entire tree.  This rebuilds the
     * priority queue if the priorities of arbitrary elements have changed since
     * they were inserted.
     */
    public synchronized final void heapify() {
        int size = size();

        for (int i = (size >>> 1) - 1; i >= 0; i--)
            siftDown(i, _queue.get(i));
    }

    /**
     * Swap elements at positions i and j in the table.
     */
    final void swap(int i, int j) {
        E tmp = _queue.get(i);
        _queue.setElementAt(_queue.get (j), i);
        _queue.setElementAt(tmp, j);
    }
}
