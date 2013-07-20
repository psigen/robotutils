/*
 *  The MIT License
 *
 *  Copyright 2010 Prasanna Velagapudi <psigen@gmail.com>.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package robotutils.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Implements a file-backed buffer for storing large objects that need to be
 * paged in and out of system memory.  It does so by implementing the Map
 * interface backed by a random-access binary data file.  The buffer only
 * supports write operations at this time: <b>objects cannot be deleted</b>.
 *
 * The objects are stored using java serialization into a file-backed linked
 * list, so the typical serialization rules apply:
 * <ul>
 * <li>objects must implement Serializable to be saved out</li>
 * <li>retrieved objects must have matching serialVersionUIDs</li>
 * </ul>
 *
 * @see java.util.Map
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class FileBuffer<T extends Serializable> implements Map<Long, T> {

    /**
     * The default size of a cache used to buffer objects loaded from file.
     */
    public static final int DEFAULT_CACHE_SIZE = 100;

    /**
     * The size of the header that accompanies each object in file (in bytes)
     */
    private static final int HEADER_SIZE = Long.SIZE * 5;


    /**
     * This is the internal linked list representation used to represent objects
     * in the file-backed buffer.
     */
    protected class Entry implements Map.Entry<Long, T> {
        long next;
        long self;
        long prev;
        long size;
        T obj;

        @Override
        public Long getKey() {
            return self;
        }

        @Override
        public T getValue() {
            return obj;
        }

        @Override
        public T setValue(T v) {
            throw new UnsupportedOperationException("Entry is write only.");
        }
    }

    final FileChannel _file;
    final LRUCache<Long, T> _cache;

    long _penultimatePosition = -1;
    long _lastPosition = 0;
    int _size;

    /**
     * Creates a new FileBuffer object backed by the specified data file.  Reuse
     * the same data file to have persistence over multiple instantiations.
     * 
     * <b>Note: If the data file does not exist, it will be created.</b>
     * 
     * The behavior of the FileBuffer is undefined if multiple instances attempt
     * to access the same file at the same time.
     * 
     * @param file The file that will be used as an object data store
     * @throws FileNotFoundException Occurs if the backing file could not be read or opened.
     */
    public FileBuffer(File file) throws FileNotFoundException {

        // Call main constructor using default cache size setting
        this(file, DEFAULT_CACHE_SIZE);
    }

    public FileBuffer(File file, int cacheSize) throws FileNotFoundException {

        // Open file for reading and writing
        _file = new RandomAccessFile(file, "rws").getChannel();
        
        // Initialize cache of given size
        _cache = new LRUCache(cacheSize);

        // Search file to determine size and last pointer
        while(true) {
            try {
                _penultimatePosition = _lastPosition;
                if (!isValid(_lastPosition)) break;
                _lastPosition = readHeader(_lastPosition).next;
                ++_size;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Adds a new object to the FileBuffer.
     *
     * If the backing file has an IO error or cannot serializable a given object
     * type, a RuntimeException will be thrown wrapping the original
     * corresponding IOException.
     *
     * @param obj The object to be added.
     * @return The UID to be used as a key to access the object.
     */
    public final long add(T obj) {
        try {
            long id = write(obj);
            _cache.put(id, obj);
            ++_size;
            return id;
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Adds a new sequential list of objects to the FileBuffer.
     *
     * @param objs The objects to be added.
     * @return A list of UIDs to be used as keys to access the objects.
     */
    public final List<Long> addAll(List<T> objs) {
        List<Long> uids = new ArrayList<Long>(objs.size());

        for (T obj : objs) {
            uids.add(add(obj));
        }
        
        return uids;
    }

    /**
     * Internal function used to check whether a provided ID could possibly
     * reference a valid location in the FileBuffer object.  This is not a
     * comprehensive test, use containsKey for that.
     *
     * @see FileBuffer#containsKey(java.lang.Object) 
     * @param uid The UID that is being tested.
     * @return True if the UID matches a valid object header in the FileBuffer.
     */
    protected final boolean isValid(long uid) {
        try {
            return (uid >= 0) && (uid < _file.size());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * De-serializes a header associated with a particular object.  Used
     * internally to move through the linked list and locate objects without
     * having to fully de-serialize them.
     * 
     * @param uid the UID of the object whose header is being accessed.
     * @return An Entry object with only the header portion filled in, or null if the UID does not match a valid object.
     * @throws IOException Indicates that de-serialization of the header failed.
     */
    protected final Entry readHeader(long uid) throws IOException {
        
        // Read header indicating [prev, size, next]
        ByteBuffer ptrs = ByteBuffer.allocate(HEADER_SIZE);
        if (_file.read(ptrs, uid) != ptrs.capacity()) {
            throw new IOException("Could not read file.");
        } else {
            ptrs.flip();
        }

        // Store this entry
        Entry entry = new Entry();
        entry.prev = ptrs.getLong();
        entry.self = ptrs.getLong();
        entry.next = ptrs.getLong();
        entry.size = ptrs.getLong();

        // If checksum is valid, return entry, if not, return null
        long checksum = ptrs.getLong() + entry.prev + entry.self + entry.next + entry.size;
        if (checksum == 0) {
            return entry;
        } else {
            return null;
        }
    }

    /**
     * De-serializes an entire object at a particular location.  Used
     * internally to retrieve objects from the linked list.
     *
     * @param uid the UID of the object that is being accessed.
     * @return An Entry object with both the header and object fields filled in.
     * @throws IOException Indicates that de-serialization of the object failed.
     * @throws ClassNotFoundException Indicates that the serialized class of the object is not known to the JVM.
     */
    protected final Entry read(long uid) throws IOException, ClassNotFoundException {
        
        // Get entry header
        Entry entry = readHeader(uid);
        if (entry == null) return null;
        
        // Read actual stored object
        ByteBuffer payload = ByteBuffer.allocate((int)entry.size);
        if (_file.read(payload, uid + HEADER_SIZE) != payload.capacity()) {
            throw new IOException("Could not read file");
        } else {
            payload.flip();
        }

        // Deserialize object 
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload.array()));
        entry.obj = (T)ois.readObject();

        return entry;
    }

    /**
     * Internal function used to write a new object to the end of the linked
     * list used by the FileBuffer.
     *
     * @param obj the object that will be inserted.
     * @return a UID that can be used to reference the object.
     * @throws IOException Indicates that serialization of the object failed.
     */
    protected long write(Serializable obj) throws IOException {

        // Move to the end of the file
        _file.position(_lastPosition);
        
        // Serialize the object
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();

        // Construct header information
        ByteBuffer ptrs = ByteBuffer.allocate(HEADER_SIZE);
        LongBuffer lptrs = ptrs.asLongBuffer();
        lptrs.put(_penultimatePosition); // prev
        lptrs.put(_lastPosition); // self
        lptrs.put(_lastPosition + baos.size() + ptrs.capacity()); // next
        lptrs.put(baos.size()); // size
        lptrs.put(  - _penultimatePosition
                    - _lastPosition
                    - _lastPosition - baos.size() - ptrs.capacity()
                    - baos.size() ); //checksum
        _file.write(ptrs);

        // Construct object information
        ByteBuffer objBuffer = ByteBuffer.wrap(baos.toByteArray());
        _file.write(objBuffer);

        // Return current position as object reference
        _penultimatePosition = _lastPosition;
        _lastPosition = _file.position();
        return _penultimatePosition;
    }

    /**
     * Implements a two-way iterator over the headers in the FileBuffer,
     * allowing fast traversal of all valid UIDs.
     */
    protected class FileBufferHeaderIterator implements ListIterator<Long> {

        long next = 0;
        int nextId = 0;

        long prev = -1;
        int prevId = -1;

        private FileBufferHeaderIterator(int index) {
            for (int i = 0; i < index; i++) {
                if (hasNext()) {
                    next();
                } else {
                    throw new IllegalArgumentException("Index out of bounds.");
                }
            }
        }

        public final boolean hasNext() {
            return isValid(next);
        }

        public final Long next() {
            try {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                Entry e = readHeader(next);

                prev = next;
                ++prevId;

                next = e.next;
                ++nextId;

                return e.self;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public final boolean hasPrevious() {
            return isValid(prev);
        }

        public final Long previous() {
            try {
                if (!hasPrevious()) {
                    throw new NoSuchElementException();
                }

                Entry e = readHeader(prev);

                next = prev;
                --nextId;

                prev = e.prev;
                --prevId;

                return e.self;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public int nextIndex() {
            return nextId;
        }

        public int previousIndex() {
            return prevId;
        }

        /* The following functions are unsupported because this is a write-only buffer (no modifications!) */

        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

        public void set(Long e) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public void add(Long e) {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    /**
     * Implements a two-way iterator over the object entries in the FileBuffer,
     * allowing fast traversal of all valid objects.
     *
     * <b>Note: As objects are traversed, they are de-serialized, making this
     * traversal possibly very slow and memory intensive.</b>
     */
    protected class FileBufferIterator implements ListIterator<Map.Entry<Long, T>> {

        long next = 0;
        int nextId = 0;

        long prev = -1;
        int prevId = -1;

        private FileBufferIterator(int index) {
            for (int i = 0; i < index; i++) {
                if (hasNext()) {
                    next();
                } else {
                    throw new IllegalArgumentException("Index out of bounds.");
                }
            }
        }

        public final boolean hasNext() {
            return isValid(next);
        }

        public final Entry next() {
            try {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                Entry e = read(next);
                
                prev = next;
                ++prevId;
                
                next = e.next;
                ++nextId;
                
                return e;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        public final boolean hasPrevious() {
            return isValid(prev);
        }

        public final Entry previous() {
            try {
                if (!hasPrevious()) {
                    throw new NoSuchElementException();
                }

                Entry e = read(prev);

                next = prev;
                --nextId;

                prev = e.prev;
                --prevId;

                return e;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        public int nextIndex() {
            return nextId;
        }

        public int previousIndex() {
            return prevId;
        }

        /* The following functions are unsupported because this is a write-only buffer (no modifications!) */

        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

        public void set(Map.Entry<Long, T> e) {
            throw new UnsupportedOperationException("Not supported.");
        }

        public void add(Map.Entry<Long, T> e) {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }

    /**
     * Returns the current number of objects stored in the FileBuffer.
     *
     * @return the number of objects currently stored in the FileBuffer.
     */
    public int size() {
        return _size;
    }

    /**
     * Constructs an abstract set implemented using an iterator over the
     * internal linked list structure.  This can be used to traverse the entries
     * in the FileBuffer.
     *
     * <b>Note: due to de-serialization costs, using this set can be extremely
     * slow!  Do not use it unless absolutely necessary.</b>
     *
     * @return A set representing the entries in the current FileBuffer.
     */
    public Set<Map.Entry<Long, T>> entrySet() {
        return new AbstractSet<Map.Entry<Long, T>>() {

            @Override
            public Iterator<Map.Entry<Long, T>> iterator() {
                return new FileBufferIterator(0);
            }

            @Override
            public int size() {
                return FileBuffer.this.size();
            }

        };
    }

    /**
     * Returns true if the FileBuffer currently contains no objects.
     * @return True if the FileBuffer contains no objects.
     */
    public boolean isEmpty() {
        return (size() == 0);
    }

    /**
     * Does a linear search over the entries to determine if the key is
     * contained as a valid entry in the full linked list.  This is not the most
     * optimal thing to do, but it is generally pretty fast.
     *
     * @param uid the UID whose membership is being tested.
     * @return True if this UID matches a valid object header in the FileBuffer.
     */
    public boolean containsKey(Object uid) {
        if (uid instanceof Long) {
            if (!isValid((Long)uid))
                return false;

            try {
                Entry header = readHeader((Long)uid);
                return (header != null);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            return false;
        }
    }

    /**
     * Does a linear search over the objects in the FileBuffer to determine
     * if the returned object matches any of the objects in the FileBuffer.
     *
     * <b>Note: due to de-serialization costs, this operation can be extremely
     * slow!  Do not use it unless absolutely necessary.</b>
     *
     * @param obj The object whose membership is being tested.
     * @return True if the object matches a valid object in the FileBuffer.
     */
    public boolean containsValue(Object obj) {

        // Check against each entry in the FileBuffer
        for (Map.Entry<Long, T> entry : entrySet()) {
            if (entry.getValue().equals(obj)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Constructs an abstract set implemented using an iterator over the
     * internal linked list structure.  This can be used to traverse the UIDs
     * used in the FileBuffer.  This set does not de-serialize any objects, and
     * should therefore be fast to traverse.
     *
     * @return A set representing the valid UIDs in the current FileBuffer.
     */
    public Set<Long> keySet() {
        return new AbstractSet<Long>() {

            @Override
            public Iterator<Long> iterator() {
                return new FileBufferHeaderIterator(0);
            }

            @Override
            public int size() {
                return FileBuffer.this.size();
            }
        };
    }

    /**
     * Constructs an abstract collection implemented using an iterator over the
     * internal linked list structure.  This can be used to traverse the objects
     * in the FileBuffer.
     *
     * <b>Note: due to de-serialization costs, using this collection can be
     * extremely slow!  Do not use it unless absolutely necessary.</b>
     *
     * @return A collection representing the objects in the current FileBuffer.
     */
    public Collection<T> values() {
        return new AbstractCollection<T>() {
            
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private FileBufferIterator _entryIterator = new FileBufferIterator(0);

                    public boolean hasNext() {
                        return _entryIterator.hasNext();
                    }

                    public T next() {
                        return _entryIterator.next().getValue();
                    }

                    public void remove() {
                        _entryIterator.remove();
                    }
                };
            }

            @Override
            public int size() {
                return FileBuffer.this.size();
            }
        };
    }

    /**
     * Attempts to retrieve an object from the FileBuffer.  
     * 
     * In order to be a fast operation, this is done directly using the memory
     * reference of the UID, without traversing the internal linked list.
     *
     * If the backing file has an IO error or contains an un-serializable object
     * type, a RuntimeException will be thrown wrapping the original
     * corresponding IOException or ClassNotFoundException.
     *
     * @param key The UID of the object that is being retrieved (must be a Long).
     * @return The object, or null if the reference is not valid.
     */
    public T get(Object key) {
        T obj = null;
        Long uid = null;

        // Throw out invalid object keys
        if (!(key instanceof Long)) {
            return obj;
        } else {
            uid = (Long)key;
        }

        // Check the cache
        obj = _cache.get(uid);

        // If not found, load from file
        if (obj == null) {

            // Verify the validity of the UID
            if (!isValid(uid))
                return obj;

            // Read the entry from file
            try {
                Entry e = read(uid);
                if (e == null) {
                    return null;
                } else {
                    obj = e.obj;
                    _cache.put(uid, obj);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        return obj;
    }

    /**
     * List wrapper that provides access to the elements of the buffer in the
     * order they were inserted.
     *
     * <i>Note: Since the internal representation of this buffer is a linked
     * list, lookups on this wrapper require a linear traversal of the buffer
     * headers. </i>
     *
     * @return A list backed by this buffer.
     */
    public List<T> asList() {
        return new AbstractList<T>() {
            ListIterator<Long> _iterator = new FileBufferHeaderIterator(0);

            @Override
            public T get(int i) {
                return FileBuffer.this.get(new FileBufferHeaderIterator(i).next());
            }

            @Override
            public int size() {
                return FileBuffer.this.size();
            }
        };
    }

    /* The following functions are unsupported because this is a write-only buffer (no modifications!) */

    /**
     * Not supported because this is a write-to-EOF buffer.
     *
     * @see FileBuffer#add(java.io.Serializable)
     *
     * @param k
     * @param v
     * @return Unspecified, this function will always throw an exception.
     */
    public T put(Long k, T v) {
        throw new UnsupportedOperationException("Not supported.  Please use the 'add' function.");
    }

    /**
     * Not supported because this is a write-to-EOF buffer.
     *
     * @see FileBuffer#addAll(java.util.List) 
     *
     * @param map
     */
    public void putAll(Map<? extends Long, ? extends T> map) {
        throw new UnsupportedOperationException("Not supported.  Please use the addAll function.");
    }

    /**
     * Not supported because this is a write-to-EOF buffer.
     *
     * @param o
     * @return Unspecified, this function will always throw an exception.
     */
    public T remove(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Not supported because this is a write-to-EOF buffer.
     */
    public void clear() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
