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
 * Implementation of the D*-lite algorithm that solves the planning problem for
 * an arbitrary graph, where movement occurs along directed or undirected edges
 * and use the weights associated with the edges, if provided.  The heuristic
 * function <i>h</i> is abstract and must be defined in a subclass before use.
 * This is because the heuristic function is typically dependent on some domain
 * specific content of each vertex.
 *
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public abstract class GraphDStar<V, E> extends DStarLite<V> {

    /**
     * The map over which D*-lite searches will be performed.
     */
    final Graph<V, E> _graph;

    /**
     * Instantiates D*-lite search object over the given graph.  Because the
     * D* algorithm creates a goal-rooted search tree, it is necessary to
     * specify the start and goal locations ahead of time.   
     * 
     * The start location can be changed later using the provided update
     * function.  The goal location is fixed since changing it would require
     * rebuilding the tree anyway.
     *
     * @param graph A graph where edge weights represent traversal costs
     * @param start The starting location in the graph (easy to update).
     * @param goal The ending location in the graph (cannot be updated).
     */
    public GraphDStar(Graph<V, E> graph, V start, V goal) {
        super(start, goal);
        _graph = graph;
    }

    /**
     * Update the start vertex of the D*-lite search.  This can be used to move
     * the start location around without having to recompute the entire search
     * tree.
     *
     * @param start The new starting location in the graph.
     */
    public void setStart(V start) {
        super.updateStart(start);
    }

    /**
     * Gets the current starting location of the search.
     * 
     * @return The current starting location of the search.
     */
    public V getStart() {
        return _start;
    }

    /**
     * Gets the current ending location of the search.
     *
     * @return The current ending location of the search.
     */
    public V getGoal() {
        return _goal;
    }

    /**
     * A wrapper function that searches for a set of edges that yields the
     * optimal path of vertices returned by D*-lite.  The start and end points
     * are assumed to already be specified using the constructor and setStart.
     *
     * @see GraphDStar#GraphDStar(org.jgrapht.Graph, java.lang.Object, java.lang.Object) 
     * @see GraphDStar#setStart(java.lang.Object)
     *
     * @return a list of edges implementing an optimal path
     */
    public List<E> search() {

        // Find the optimal vertex path, if it exists
        List<V> path = super.plan();
        if (path.isEmpty()) {
            return Collections.emptyList();
        }

        // Find the cheapest edges between each neighboring vertex in solution
        List<E> edgePath = new ArrayList(path.size() - 1);

        V last = _start;
        for (V curr : path) {

            // Ignore the start vertex, we are already there
            if (curr.equals(_start)) {
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
    protected Collection<V> pred(V s) {
        Set<E> edges = _graph.edgesOf(s);
        Collection<V> preds = new ArrayList(edges.size());

        for (E edge : edges) {
            V source = _graph.getEdgeSource(edge);
            V target = _graph.getEdgeTarget(edge);

            if ( target.equals(s) ) {
                preds.add( source );
            } else if ( source.equals(s) ) {
                if (_graph.containsEdge( target, s )) {
                    preds.add( target );
                }
            }
        }

        return preds;
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
