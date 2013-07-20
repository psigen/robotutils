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

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.AbstractGraph;

/**
 * Creates a graph representation of an underlying grid data structure.
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class GridGraph extends AbstractGraph<int[], int[][]> {

    final GridMap _map;

    public class VertexSet extends AbstractSet<int[]> {

        @Override
        public Iterator<int[]> iterator() {
            return new Iterator() {
                public int dims = _map.dims();
                public int[] sizes = _map.sizes();
                public int[] idx = new int[dims];
                public boolean hasNext = true;


                public boolean hasNext() {
                    return hasNext;
                }

                public boolean inc() {
                    for (int i = 0; i < dims; i++) {
                        if (idx[i] < sizes[i] - 1) {
                            idx[i]++;
                            return true;
                        } else {
                            idx[i] = 0;
                        }
                    }

                    return false;
                }

                public Object next() {
                    hasNext = inc();

                    if (hasNext) {
                        return Arrays.copyOf(idx, idx.length);
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException("Not supported.");
                }
            };
        }

        @Override
        public int size() {
            int size = 1;

            for (int i = 0; i < _map.dims(); i++) {
                size *= _map.size(i);
            }

            return size;
        }

    }

    public class EdgeSet extends AbstractSet<int[][]> {

        @Override
        public Iterator<int[][]> iterator() {
            return new Iterator<int[][]>() {

                Iterator<int[]> vertexItr = vertexSet().iterator();
                Iterator<int[][]> edgeItr = Collections.EMPTY_SET.iterator();

                public boolean hasNext() {
                    if (vertexItr.hasNext() || edgeItr.hasNext()) {
                        return true;
                    } else {
                        return false;
                    }
                }

                public int[][] next() {
                    if (!edgeItr.hasNext()) {
                        if (vertexItr.hasNext()) {
                            edgeItr = edgesOf(vertexItr.next()).iterator();
                        } else {
                            throw new NoSuchElementException();
                        }
                    }

                    return edgeItr.next();
                }

                public void remove() {
                    throw new UnsupportedOperationException("Not supported.");
                }
            };
        }

        @Override
        public int size() {
            int size = 1;

            for (int i = 0; i < _map.dims(); i++) {
                size *= _map.size(i);
            }

            return size * (2 * _map.dims());
        }

    }

    public GridGraph(GridMap map) {
        _map = map;
    }

    public Set<int[][]> getAllEdges(int[] v, int[] v1) {

        int[][] edge = getEdge(v, v1);

        if (edge != null) {
            return Collections.singleton(edge);
        } else {
            return Collections.emptySet();
        }
    }

    public int[][] getEdge(int[] v, int[] v1) {
        int[][] edge = new int[][] {v, v1};

        if (containsEdge(edge)) {
            return edge;
        } else {
            return null;
        }
    }

    public EdgeFactory<int[], int[][]> getEdgeFactory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int[][] addEdge(int[] v, int[] v1) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean addEdge(int[] v, int[] v1, int[][] e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean addVertex(int[] v) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean containsEdge(int[][] e) {
        if (e.length != 2) {
            return false;
        }

        if (!containsVertex(e[0])) {
            return false;
        }

        if (!containsVertex(e[1])) {
            return false;
        }

        int idxDiff = 0;
        for (int i = 0; i < _map.dims(); i++) {
            idxDiff += Math.abs(e[0][i] - e[1][i]);
        }

        if (idxDiff == 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean containsVertex(int[] v) {
        if (v.length != _map.dims()) {
            return false;
        }

        for (int i = 0; i < _map.dims(); i++) {
            if (v[i] < 0 || v[i] >= _map.size(i)) {
                return false;
            }
        }

        return true;
    }

    public Set<int[][]> edgeSet() {
        return new EdgeSet();
    }

    public Set<int[][]> edgesOf(int[] v) {

        int numNbrs = 2 * _map.dims();
        HashSet edges = new HashSet(numNbrs);

        for (int i = 0; i < _map.dims(); i++) {
            if (v[i] < _map.size(i) - 1) {
                int[] v1 = Arrays.copyOf(v, _map.dims());
                v1[i] = v[i] + 1;
                edges.add(new int[][] {v, v1});
            }

            if (v[i] > 0) {
                int[] v1 = Arrays.copyOf(v, _map.dims());
                v1[i] = v[i] - 1;
                edges.add(new int[][] {v, v1});
            }
        }

        return edges;
    }

    public int[][] removeEdge(int[] v, int[] v1) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean removeEdge(int[][] e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean removeVertex(int[] v) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public Set<int[]> vertexSet() {
        return new VertexSet();
    }

    public int[] getEdgeSource(int[][] e) {
        return e[0];
    }

    public int[] getEdgeTarget(int[][] e) {
        return e[1];
    }

    public double getEdgeWeight(int[][] e) {
        return (double)(_map.get(e[0]) + _map.get(e[1]))/2.0;
    }
}
