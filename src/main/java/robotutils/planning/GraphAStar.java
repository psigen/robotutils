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

package robotutils.planning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jgrapht.Graph;

/**
 * Implementation of the A* algorithm that solves the planning problem for
 * an arbitrary graph, where movement occurs along directed or undirected edges
 * and use the weights associated with the edges, if provided.  The heuristic
 * function <i>h</i> is abstract and must be defined in a subclass before use.
 * This is because the heuristic function is typically dependent on some domain
 * specific content of each vertex.
 *
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public abstract class GraphAStar<V, E> extends AStar<V> {

    /**
     * The map over which A* searches will be performed.
     */
    final Graph<V, E> _graph;

    /**
     * Instantiates A* search object over the given graph.
     *
     * @param graph A graph where edge weights represent traversal costs
     */
    public GraphAStar(Graph<V, E> graph) {
        _graph = graph;
    }

    /**
     * A wrapper function that searches for a set of edges that yields the
     * optimal path of vertices returned by A*.
     * 
     * @param start the starting vertex
     * @param goal the ending vertex
     * @return a list of edges implementing an optimal path
     */
    public List<E> searchEdges(V start, V goal) {

        // Find the optimal vertex path, if it exists
        List<V> path = super.search(start, goal);
        if (path.isEmpty()) {
            return Collections.emptyList();
        }

        // Find the cheapest edges between each neighboring vertex in solution
        List<E> edgePath = new ArrayList(path.size() - 1);

        V last = start;
        for (V curr : path) {

            // Ignore the start vertex, we are already there
            if (curr.equals(start)) {
                continue;
            }

            // Get all of the edges between this vertex and the next
            Set<E> edges = _graph.getAllEdges(last, curr);

            // Find the lowest cost edge to the next vertex
            double minWeight = Double.POSITIVE_INFINITY;
            E minEdge = null;

            for (E edge : edges) {
                double weight = _graph.getEdgeWeight(edge);

                if (weight < minWeight) {
                    minEdge = edge;
                    minWeight = weight;
                }
            }

            // Add this edge to the solution path
            edgePath.add(minEdge);
            last = curr;
        }

        return edgePath;
    }

    @Override
    protected Collection<V> succ(V s) {
        Set<E> edges = _graph.edgesOf(s);
        Collection<V> succs = new ArrayList(edges.size());

        for (E edge : edges) {
            V source = _graph.getEdgeSource(edge);
            V target = _graph.getEdgeTarget(edge);

            if ( source.equals(s) ) {
                succs.add( target );
            } else if ( target.equals(s) ) {
                if (_graph.containsEdge( s, source )) {
                    succs.add( source );
                }
            }
        }

        return succs;
    }

    @Override
    protected double c(V a, V b) {
        E edge = _graph.getEdge(a, b);

        if (edge != null) {
            return _graph.getEdgeWeight(edge);
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

}
