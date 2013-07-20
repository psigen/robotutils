/*
 *  The MIT License
 * 
 *  Copyright 2011 pkv.
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

package robotutils.planning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pkv
 */
public class BellmanFordTest {

    private static double[][] _edges;
    private static double[][] _minDists;

    private static void loadGraph() {
        // Set infinite edge weights to non-existent edges
        _edges = new double[6][6];
        for (int i = 0; i < _edges.length; ++i) {
            for (int j = 0; j < _edges[i].length; ++j) {
                _edges[i][j] = Double.POSITIVE_INFINITY;
            }
        }

        // Load example edge weights as per example in Figure 21.26
        // from Algorithms in Java: Third Edition, p285
        _edges[0][1] =  .41;
        _edges[1][2] =  .51;
        _edges[2][3] =  .50;
        _edges[4][3] =  .36;
        _edges[3][5] = -.38;
        _edges[3][0] =  .45;
        _edges[0][5] =  .29;
        _edges[5][4] =  .21;
        _edges[1][4] =  .32;
        _edges[4][2] =  .32;
        _edges[5][1] = -.29;

        // Load correct distances from example to check values later
        _minDists = new double[][] {
            {      0,    0,  .51,  .68,  .32,  .29   },
            {   1.13,    0,  .51,  .68,  .32,  .30   },
            {    .95, -.17,    0,  .50,  .15,  .12   },
            {    .45, -.67, -.16,    0, -.35, -.38   },
            {    .81, -.31,  .20,  .36,    0, -.02   },
            {    .84, -.29,  .22,  .39,  .03,    0   }
        };
    }

    @Before
    public void setUp() throws Exception {
        loadGraph();
    }

    /**
     * Test of succ method, of class BellmanFord.
     */
    @Test
    public void testSucc() {
        System.out.println("succ");
        
        BellmanFord instance = new BellmanFordImpl();

        for (int i = 0; i < _edges.length; ++i) {
            Collection successors = instance.succ(i);
            for (int j = 0; j < _edges[i].length; ++j) {
                assertEquals(!Double.isInfinite(_edges[i][j]), successors.contains(j));
            }
        }
    }

    /**
     * Test of c method, of class BellmanFord.
     */
    @Test
    public void testC() {
        System.out.println("c");

        BellmanFord instance = new BellmanFordImpl();
        
        for (int i = 0; i < _edges.length; ++i) {
            for (int j = 0; j < _edges[i].length; ++j) {
                assertEquals(_edges[i][j], instance.c(i, j), 1e-6);
            }
        }
    }

    /**
     * Test of searchFrom method, of class BellmanFord.
     */
    @Test
    public void testSearchFrom() {
        System.out.println("searchFrom");

        BellmanFord instance = new BellmanFordImpl();

        for (int i = 0; i < _edges.length; ++i) {
            instance.searchFrom(i);

            for (int j = 0; j < _edges[i].length; ++j) {
                assertEquals(_minDists[i][j], instance.costTo(j), 1e-4);
            }
        }
    }

    /**
     * Test of source method, of class BellmanFord.
     */
    @Test
    public void testSource() {
        System.out.println("source");

        BellmanFord instance = new BellmanFordImpl();

        // For valid edges, should get actual source node
        for (int i = 0; i < _edges.length; ++i) {
            instance.searchFrom(i);
            assertEquals(i, instance.source());
        }

        // If null node is used, source should be null
        try {
            instance.searchFrom(null);
        } catch (IllegalArgumentException ex) {}
        assertNull(instance.source());

        // If orphan node is used, source should still match
        instance.searchFrom(-1);
        assertEquals(-1, instance.source());

        // If negative loop occurs, source should be null
        _edges[5][1] = -1000.0;
        try {
            instance.searchFrom(0);
        } catch (IllegalStateException ex) {}
        assertNull(instance.source());
    }

    /**
     * Test of pathTo method, of class BellmanFord.
     */
    @Test
    public void testPathTo() {
        System.out.println("pathTo");
        
        BellmanFord instance = new BellmanFordImpl();
        
        for (int i = 0; i < _edges.length; ++i) {
            instance.searchFrom(i);

            for (int j = 0; j < _edges[i].length; ++j) {
                List<Integer> path = instance.pathTo(j);
                assertTrue(path.size() > 0);

                assertEquals(i, (int)path.get(0));
                assertEquals(j, (int)path.get(path.size() - 1));

                double cost = 0.0;
                Integer prev = null;
                
                for (Integer node : path) {
                    if (prev != null) {
                        assertTrue(instance.succ(prev).contains(node));
                        cost += instance.c(prev, node);
                    }

                    prev = node;
                }

                assertEquals(_minDists[i][j], cost, 1e-4);
            }
        }
    }

    /**
     * Test of costTo method, of class BellmanFord.
     */
    @Test
    public void testCostTo() {
        System.out.println("costTo");

        BellmanFord instance = new BellmanFordImpl();
        
        for (int i = 0; i < _edges.length; ++i) {
            instance.searchFrom(i);

            for (int j = 0; j < _edges[i].length; ++j) {
                assertEquals(_minDists[i][j], instance.costTo(j), 1e-4);
            }
            assertEquals(Double.POSITIVE_INFINITY, instance.costTo(-1), 1e-4);
        }
    }

    /**
     * Test of search method, of class BellmanFord.
     */
    @Test
    public void testSearch() {
        System.out.println("search");
        
        BellmanFord instance = new BellmanFordImpl();

        for (int i = 0; i < _edges.length; ++i) {
            for (int j = 0; j < _edges[i].length; ++j) {
                List<Integer> path = instance.search(i, j);
                assertTrue(path.size() > 0);

                assertEquals(i, (int)path.get(0));
                assertEquals(j, (int)path.get(path.size() - 1));

                double cost = 0.0;
                Integer prev = null;

                for (Integer node : path) {
                    if (prev != null) {
                        assertTrue(instance.succ(prev).contains(node));
                        cost += instance.c(prev, node);
                    }

                    prev = node;
                }

                assertEquals(_minDists[i][j], cost, 1e-4);
            }
        }
    }

    public class BellmanFordImpl extends BellmanFord<Integer> {

        @Override
        public Collection<Integer> succ(Integer i) {

            if (i < 0 || i > _edges.length)
                return Collections.EMPTY_SET;

            ArrayList<Integer> succs = new ArrayList(_edges.length);
            for (int j = 0; j < _edges[i].length; ++j) {
                if (_edges[i][j] < Double.POSITIVE_INFINITY) {
                    succs.add(j);
                }
            }
            return succs;
        }

        @Override
        public double c(Integer u, Integer v) {
            return _edges[u][v];
        }
    }

}