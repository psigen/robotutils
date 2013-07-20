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

package robotutils.util;

import java.util.Comparator;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class PriorityQueueTest {

    private static Random _rnd;
    private static final int NUM_ELEMENTS = 1000;

    @BeforeClass
    public static void setUpClass() throws Exception {
        _rnd = new Random();
    }

    /**
     * Test of add method, of class PriorityQueue.
     */
    @Test
    public void testAdd() {
        System.out.println("add");

        // Add NUM_ELEMENTS integers to a list
        ArrayList<Integer> numbers = new ArrayList();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(x);

        // Insert those numbers randomly into the queue
        PriorityQueue<Integer> instance = new PriorityQueue();
        while (!numbers.isEmpty())
            instance.add(numbers.remove(_rnd.nextInt(numbers.size())));
        assertEquals(NUM_ELEMENTS, instance.size());

        // When we remove the numbers, they should be in order
        for (int x = 0; x < NUM_ELEMENTS; x++)
            assertEquals(x, (int)instance.poll());
    }

    /**
     * Test of peek method, of class PriorityQueue.
     */
    @Test
    public void testPeek() {
        System.out.println("peek");

        // Add NUM_ELEMENTS integers to a list
        ArrayList<Integer> numbers = new ArrayList();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(x);

        // Insert those numbers randomly into the queue
        PriorityQueue<Integer> instance = new PriorityQueue();
        while (!numbers.isEmpty())
            instance.add(numbers.remove(_rnd.nextInt(numbers.size())));
        assertEquals(NUM_ELEMENTS, instance.size());

        // When we remove the numbers, they should be in order
        // Also, peeking should not affect the next element
        for (int x = 0; x < NUM_ELEMENTS; x++) {
            assertEquals(x, (int)instance.peek());
            assertEquals(x, (int)instance.peek());
            assertEquals(x, (int)instance.poll());
        }
    }

    /**
     * Test of poll method, of class PriorityQueue.
     */
    @Test
    public void testPoll() {
        System.out.println("poll");

        // Add NUM_ELEMENTS integers to a list
        ArrayList<Integer> numbers = new ArrayList();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(x);
        
        // Insert those numbers randomly into the queue
        PriorityQueue<Integer> instance = new PriorityQueue();
        while (!numbers.isEmpty())
            instance.add(numbers.remove(_rnd.nextInt(numbers.size())));
        assertEquals(NUM_ELEMENTS, instance.size());

        // When we remove the numbers, they should be in order
        // Also, polling should affect the next element
        for (int x = 0; x < 99; x++) {
            assertEquals(x, (int)instance.poll());
            assertEquals(x+1, (int)instance.peek());
        }
    }

    /**
     * Test of remove method, of class PriorityQueue.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");

        // Add NUM_ELEMENTS integers to a list
        ArrayList<Integer> numbers = new ArrayList();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(x);

        // Insert those numbers randomly into the queue
        PriorityQueue<Integer> instance = new PriorityQueue();
        while (!numbers.isEmpty())
            instance.add(numbers.remove(_rnd.nextInt(numbers.size())));
        assertEquals(NUM_ELEMENTS, instance.size());

        // Remove odd numbers from 1 - NUM_ELEMENTS/2
        for (int x = 1; x < NUM_ELEMENTS/2; x+=2)
            instance.remove(x);

        // When we remove the first numbers, they should only be even
        for (int x = 0; x < NUM_ELEMENTS/4; x++)
            assertEquals(x*2, (int)instance.poll());
        assertEquals(NUM_ELEMENTS/2, instance.size());

        // Now remove some stuff that isn't there, just for effect
        for (int x = 0; x < NUM_ELEMENTS/4; x++)
            instance.remove(x + NUM_ELEMENTS);

        // The queue should not have changed from the last test
        // (50 is the next element, and there should be 50 elements total)
        assertEquals(NUM_ELEMENTS/2, (int)instance.size());
        assertEquals(NUM_ELEMENTS/2, (int)instance.peek());

        // Now remove the even numbers from the remaining queue
        for (int x = NUM_ELEMENTS/2; x < NUM_ELEMENTS; x+=2)
            instance.remove(x);
        assertEquals(NUM_ELEMENTS/4, instance.size());

        // When we remove the next 25 numbers, they should only be odd
        for (int x = 0; x < NUM_ELEMENTS/4; x++)
            assertEquals(NUM_ELEMENTS/2 + x*2 + 1, (int)instance.poll());
    }

    /**
     * Test of clear method, of class PriorityQueue.
     */
    @Test
    public void testClear() {
        System.out.println("clear");

        // Add NUM_ELEMENTS integers to a list
        ArrayList<Integer> numbers = new ArrayList();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(x);

        // Insert those numbers randomly into the queue
        PriorityQueue<Integer> instance = new PriorityQueue();
        while (!numbers.isEmpty())
            instance.add(numbers.remove(_rnd.nextInt(numbers.size())));
        assertEquals(NUM_ELEMENTS, instance.size());

        // Make sure that they all get cleared
        instance.clear();
        assertEquals(0, instance.size());

        // Add NUM_ELEMENTS larger integers to a list
        numbers.clear();
        for (int x = NUM_ELEMENTS; x < 200; x++)
            numbers.add(x);

        // Insert those numbers randomly into the queue
        while (!numbers.isEmpty())
            instance.add(numbers.remove(_rnd.nextInt(numbers.size())));

        // Make sure none of the older smaller integers appear
        for (int x = NUM_ELEMENTS; x < 200; x++) {
            assertEquals(x, (int)instance.peek());
            assertEquals(x, (int)instance.poll());
        }
    }

    /**
     * Test of elements method, of class PriorityQueue.
     */
    @Test
    public void testElements() {
        System.out.println("elements");

        // Add NUM_ELEMENTS integers to a list
        ArrayList<Integer> numbers = new ArrayList();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(x);

        // Insert those numbers randomly into the queue
        PriorityQueue<Integer> instance = new PriorityQueue();
        while (!numbers.isEmpty())
            instance.add(numbers.remove(_rnd.nextInt(numbers.size())));
        assertEquals(NUM_ELEMENTS, instance.size());

        // Add NUM_ELEMENTS integers to a list
        numbers.clear();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(x);

        // Check that we get all those numbers out of the enumeration
        while (!instance.isEmpty()) {
            Integer next = instance.poll();
            assertTrue(numbers.remove(next));
        }
        assertTrue("Missed the numbers: " + numbers, numbers.isEmpty());
    }

    /**
     * Test of size method, of class PriorityQueue.
     */
    @Test
    public void testSize() {
        System.out.println("size");

        // Add NUM_ELEMENTS integers to a list
        ArrayList<Integer> numbers = new ArrayList();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(x);

        // Insert those numbers randomly into the queue
        PriorityQueue<Integer> instance = new PriorityQueue();
        while (!numbers.isEmpty())
            instance.add(numbers.remove(_rnd.nextInt(numbers.size())));
        assertEquals(NUM_ELEMENTS, instance.size());

        // Randomly add and remove numbers, check the size of the queue
        int size = instance.size();
        for (int i = 0; i < NUM_ELEMENTS; i++) {
            if (_rnd.nextBoolean()) {
                size++;
                instance.add(NUM_ELEMENTS + i);
            } else {
                size--;
                instance.poll();
            }
            assertEquals(size, instance.size());
        }
    }

    /**
     * Test of isEmpty method, of class PriorityQueue.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");

        // Add NUM_ELEMENTS integers to a list
        ArrayList<Integer> numbers = new ArrayList();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(x);

        // Insert those numbers randomly into the queue
        PriorityQueue<Integer> instance = new PriorityQueue();
        while (!numbers.isEmpty())
            instance.add(numbers.remove(_rnd.nextInt(numbers.size())));
        assertEquals(NUM_ELEMENTS, instance.size());
        assertFalse(instance.isEmpty());

        // Remove all the numbers
        for (int x = 0; x < NUM_ELEMENTS; x++)
            instance.remove(x);
        assertTrue(instance.isEmpty());
    }

    /**
     * Test of update method, of class PriorityQueue.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        
        // Add NUM_ELEMENTS numbers to a list in an encapsulated datatype
        ArrayList<AtomicInteger> numbers = new ArrayList();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(new AtomicInteger(x));

        // Create a comparator that orders this datatype
        PriorityQueue<AtomicInteger> instance = new PriorityQueue(new Comparator<AtomicInteger>() {
            @Override
            public int compare(AtomicInteger o1, AtomicInteger o2) {
                return Integer.signum(o1.get() - o2.get());
            }
        });

        // Insert those numbers randomly into the queue
        for (AtomicInteger number : numbers)
            instance.add(number);
        assertEquals(NUM_ELEMENTS, instance.size());

        // Do a handful of random swaps in the queue
        for (int i = 0; i < 200; i++) {
            AtomicInteger a = numbers.get(_rnd.nextInt(NUM_ELEMENTS));
            AtomicInteger b = numbers.get(_rnd.nextInt(NUM_ELEMENTS));

            // AtomicInteger swapping is fun!
            a.set(b.getAndSet(a.get()));
            instance.update(a);
            instance.update(b);
        }

        // Test that the queue is still ordered correctly
        for (int x = 0; x < NUM_ELEMENTS; x++)
            assertEquals(x, instance.poll().get());
        assertTrue(instance.isEmpty());
    }

    /**
     * Test of contains method, of class PriorityQueue.
     */
    @Test
    public void testContains() {
        System.out.println("contains");

        // Add NUM_ELEMENTS integers to a list
        ArrayList<Integer> numbers = new ArrayList();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(x);

        // Insert those numbers randomly into the queue
        PriorityQueue<Integer> instance = new PriorityQueue();
        while (!numbers.isEmpty())
            instance.add(numbers.remove(_rnd.nextInt(numbers.size())));
        assertEquals(NUM_ELEMENTS, instance.size());

        // Add NUM_ELEMENTS integers to a list
        numbers.clear();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(x);

        // Randomly remove them and check that contains works
        for (int i = 0; i < 50; i++) {
            // Remove a random number
            int x = numbers.remove(_rnd.nextInt(numbers.size()));

            assertTrue(instance.contains(x));
            instance.remove(x);
            assertFalse(instance.contains(x));

            // Now pop a number off the top of the queue
            int y = instance.poll();
            numbers.remove((Integer)y);
            assertFalse(instance.contains(y));
        }
    }

    /**
     * Test of heapify method, of class PriorityQueue.
     */
    @Test
    public void testHeapify() {
        System.out.println("heapify");

        // Add NUM_ELEMENTS numbers to a list in an encapsulated datatype
        ArrayList<AtomicInteger> numbers = new ArrayList();
        for (int x = 0; x < NUM_ELEMENTS; x++)
            numbers.add(new AtomicInteger(x));

        // Create a comparator that orders this datatype
        PriorityQueue<AtomicInteger> instance = new PriorityQueue(new Comparator<AtomicInteger>() {
            @Override
            public int compare(AtomicInteger o1, AtomicInteger o2) {
                return Integer.signum(o1.get() - o2.get());
            }
        });

        // Insert those numbers randomly into the queue
        for (AtomicInteger number : numbers)
            instance.add(number);
        assertEquals(NUM_ELEMENTS, instance.size());

        // Do a handful of random swaps in the queue
        for (int i = 0; i < 200; i++) {
            AtomicInteger a = numbers.get(_rnd.nextInt(NUM_ELEMENTS));
            AtomicInteger b = numbers.get(_rnd.nextInt(NUM_ELEMENTS));

            // AtomicInteger swapping is fun!
            a.set(b.getAndSet(a.get()));
        }

        // Apply heap reordering
        instance.heapify();

        // Test that the queue is still ordered correctly
        for (int x = 0; x < NUM_ELEMENTS; x++)
            assertEquals(x, instance.poll().get());
        assertTrue(instance.isEmpty());
    }
    
}