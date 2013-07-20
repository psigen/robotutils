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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of the Bellman–Ford algorithm, a graph search algorithm that
 * computes single-source shortest paths in a weighted directed graph.
 *
 * If a graph contains a "negative cycle", i.e., a cycle whose edges sum to a
 * negative value, then walks of arbitrarily low weight can be constructed and
 * there can be no shortest path. Bellman-Ford can detect negative cycles and
 * report their existence, but it cannot produce a correct answer.
 *
 * This implementation implements a single-query variant, in which the shortest
 * path between two nodes is computed, and no information is retained for
 * subsequent queries.
 *
 * Source: http://en.wikipedia.org/wiki/Bellman-Ford_algorithm
 *
 * @author pkv
 */
public abstract class BellmanFord<State> {

    /**
     * Contains the distance and path information from the last search performed
     * by the algorithm.  Should be accessible through the pathTo() function.
     *
     * @see BellmanFord#pathTo(java.lang.Object) 
     * @see BellmanFord#costTo(java.lang.Object)
     */
    private LinkedHashMap<State, Score<State>> _scores = new LinkedHashMap(INITIAL_CAPACITY);

    /**
     * Contains the source node for the currently cached search.  Accessible
     * from the source() function.
     *
     * @see BellmanFord#source() 
     */
    private State _source = null;

    /**
     * Defines a suitably large initial capacity for internal data structures
     * to avoid Java's heuristics reallocating up from scratch.
     */
    public static final int INITIAL_CAPACITY = 1024;

    /**
     * An internal storage class that contains scores associated with a state.
     *
     * @param <State> the class representing a state
     */
    static final class Score<State> {
        State prev = null;
        double dist = Double.POSITIVE_INFINITY;
    }

    /**
     * Returns the set of successor states to the specified state.
     * 
     * @param s the specified state.
     * @return A set of successor states.
     */
    protected abstract Collection<State> succ(State s);

    /**
     * An exact cost function for the distance between two <i>neighboring</i>
     * states.  This function is undefined for non-neighboring states.  The
     * neighbor connectivity is determined by the pred() and succ() functions.
     * 
     * @param a some initial state
     * @param b some final state
     * @return the actual distance between the states.
     */
    protected abstract double c(State a, State b);

    /**
     * Performs a search for the shortest (lowest cost) path from the specified
     * start node to every other node that is reachable from the start node.
     *
     * @param start The node from which to find the shortest path.
     */
    public void searchFrom(State start) {

        // Can't deal with empty start and goal
        if (start == null) {
            _source = null;
            throw new IllegalArgumentException("Start cannot be null.");
        } else {
            _source = start;
        }
            

        // Step 1: initialize graph (source node has zero distance)
        
        // Add the source node (has zero distance, and no previous node)
        Score<State> startScore = new Score();
        startScore.dist = 0.0;
        _scores.clear();
        _scores.put(_source, startScore);
        

        // Create a set of unexplored, reachable nodes
        HashSet<State> reachable = new HashSet(INITIAL_CAPACITY);
        reachable.addAll(succ(_source));

        // Find all other reachable nodes by iterating through neighbors
        while(!reachable.isEmpty()) {

            // Add a blank score for this reachable node
            State u = reachable.iterator().next();
            _scores.put(u, new Score<State>());
            reachable.remove(u);

            // Add any new neighbors to the list of reachable nodes
            for (State v : succ(u)) {
                if (!_scores.containsKey(v))
                    reachable.add(v);
            }
        }

        // Step 2: relax edges repeatedly
        for (int i = 0; i < _scores.size() - 1; ++i) {
            for (State u : _scores.keySet()) {
                for (State v : succ(u)) {
                    Score<State> uScore = _scores.get(u);
                    Score<State> vScore = _scores.get(v);
                    double uvCost = c(u, v);

                    if (uScore.dist + uvCost < vScore.dist) {
                        vScore.dist = uScore.dist + uvCost;
                        vScore.prev = u;
                    }
                }
            }
        }

        // Step 3: check for negative-weight cycles
        for (State u : _scores.keySet()) {
            for (State v : succ(u)) {
                Score<State> uScore = _scores.get(u);
                Score<State> vScore = _scores.get(v);
                double uvCost = c(u, v);

                if (uScore.dist + uvCost < vScore.dist) {
                    _source = null;
                    throw new IllegalStateException("Detected a negative-weight cycle:" + u + " -> " + v);
                }
            }
        }
    }

    /**
     * Returns the source node used for the most recent searchFrom() call.
     *
     * @see BellmanFord#searchFrom(java.lang.Object)
     *
     * @return The source node of the most recent search.
     */
    public State source() {
        return _source;
    }

    /**
     * Returns the shortest path from the source node most recently used in a
     * searchFrom() call to the specified goal node.
     *
     * @param goal The node to which a shortest path should be computed.
     * @return A list of nodes corresponding to a valid path between the two nodes, or an empty list if no path was found.
     */
    public List<State> pathTo(State goal) {

        // Can't find a path if no valid search occurred
        if (_source == null)
            return Collections.EMPTY_LIST;

        // Start at the goal
        State curr = goal;
        
        // Create a trail of states to follow
        LinkedList<State> path = new LinkedList();
        path.addFirst(curr);

        // Iterate backwards to reach the start
        while (!curr.equals(_source)) {
            Score<State> currScore = _scores.get(curr);
            path.addFirst(currScore.prev);
            curr = currScore.prev;

            if (curr == null)
                return Collections.EMPTY_LIST;
        }
        return path;
    }

    /**
     * Returns the cost of the shortest path from the source node most recently
     * used in a searchFrom() call to the specified goal node.
     *
     * @param goal The node to which a shortest path cost will be queried.
     * @return The cost of the shortest valid path between the two nodes, or positive infinity if no path exists.
     */
    public double costTo(State goal) {

        // Can't find a path if no valid search occurred
        if (_source == null)
            return Double.POSITIVE_INFINITY;
        else if (_scores.containsKey(goal)) {
            return _scores.get(goal).dist;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Implements a single-query search by simply calling the searchFrom() and
     * pathTo() operations in sequence.
     *
     * @see BellmanFord#searchFrom(java.lang.Object)
     * @see BellmanFord#pathTo(java.lang.Object)
     *
     * @param start The state from which to find a shortest path.
     * @param goal The start to which to find a shortest path.
     * @return A path in the form of a list of states from the start to the goal, or an empty list if no path could be found.
     */
    public List<State> search(State start, State goal) {
        searchFrom(start);
        return pathTo(goal);
    }
}
