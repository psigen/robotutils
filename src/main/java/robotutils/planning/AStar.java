/*
 *  Copyright (c) 2009, Prasanna Velagapudi <pkv@cs.cmu.edu>
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the project nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ''AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE PROJECT AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package robotutils.planning;

import robotutils.util.PriorityQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class implements the basic A* graph search algorithm.  A* is an optimal
 * best-first search strategy.
 *
 * Source: http://en.wikipedia.org/wiki/A*_search_algorithm
 *
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public abstract class AStar<State> {

    /**
     * Defines a suitably large initial capacity for internal data structures
     * to avoid Java's heuristics reallocating up from scratch.
     */
    public static final int INITIAL_CAPACITY = 1024;

    /**
     * An internal storage class that contains scores associated with a state.
     * @param <State> the class representing a state
     */
    static final class Score<State> {
        State prev = null;
        double g = Double.POSITIVE_INFINITY;
        double h = Double.POSITIVE_INFINITY;
        double f = Double.POSITIVE_INFINITY;
    }

    /**
     * An internal comparison class that the A* priority queue uses to order
     * states from lowest to highest priority, for best-first exploration.
     * @param <State> the class representing a state
     */
    static final class ScoreComparator<State> implements Comparator<State> {
        Map<State, ? extends Score> _scores;

        public ScoreComparator(Map<State, ? extends Score> scores) {
            _scores = scores;
        }

        public int compare(State o1, State o2) {
            Score s1 = _scores.get(o1);
            Score s2 = _scores.get(o2);

            return (int)(s1.f - s2.f);
        }
    }

    /**
     * Returns the set of successor states to the specified state.
     * @param s the specified state.
     * @return A set of successor states.
     */
    protected abstract Collection<State> succ(State s);

    /**
     * An admissible heuristic function for the distance between two states.
     * In actual use, the second vertex will always be the goal state.
     *
     * The heuristic must follow these rules:
     * 1) h(a, a) = 0
     * 2) h(a, b) &lt;= c(a, c) + h(c, b) (where a and c are neighbors)
     *
     * @param a some initial state
     * @param b some final state
     * @return the estimated distance between the states.
     */
    protected abstract double h(State a, State b);

    /**
     * An exact cost function for the distance between two <i>neighboring</i>
     * states.  This function is undefined for non-neighboring states.  The
     * neighbor connectivity is determined by the pred() and succ() functions.
     * @param a some initial state
     * @param b some final state
     * @return the actual distance between the states.
     */
    protected abstract double c(State a, State b);

    /**
     * Adapted from wikipedia entry
     */
    public List<State> search(State start, State goal) {

        // Can't deal with empty start and goal
        if (start == null || goal == null) {
            return Collections.emptyList();
        }

        // Create open and closed sets, and a map to store node meta-info
        HashMap<State, Score<State>> scores = new HashMap(INITIAL_CAPACITY);
        HashSet<State> closed = new HashSet();
        PriorityQueue<State> open = new PriorityQueue(INITIAL_CAPACITY, new ScoreComparator(scores));

        // Insert the start node into our search tree
        Score<State> startScore = new Score();
        startScore.g = 0;
        startScore.h = h(start, goal);
        startScore.f = startScore.h;
        scores.put(start, startScore);
        open.add(start);

        // Search until we find a result or run out of nodes to explore
        while (!open.isEmpty()) {

            // Get the node at the top of the priority queue
            State x = open.poll();

            // If we reach the goal, traverse backwards to build a path
            if (x.equals(goal)) {
                LinkedList<State> path = new LinkedList();

                // Start at the goal
                State curr = goal;
                path.addFirst(curr);

                // Iterate backwards to reach the start
                while (!curr.equals(start)) {
                    Score<State> currScore = scores.get(curr);
                    path.addFirst(currScore.prev);
                    curr = currScore.prev;
                }
                return path;
            }

            // The node is now closed -- no more searching it!
            closed.add(x);

            // Search each of this node's neighbors
            for (State y : succ(x)) {

                // Find the neighbor and make sure it has metadata
                if (!scores.containsKey(y)) scores.put(y, new Score());

                // If the neighbor was already searched, ignore it
                if (closed.contains(y)) continue;

                // Get the current estimate of the distance to goal
                double tentativeGScore = scores.get(x).g + c(x, y);
                boolean tentativeIsBetter;
                boolean isInOpenSet;
                
                // If the node is unopened, or we have a better score, update
                if (!open.contains(y)) {
                    tentativeIsBetter = true;
                    isInOpenSet = false;
                } else if (tentativeGScore < scores.get(y).g) {
                    tentativeIsBetter = true;
                    isInOpenSet = true;
                } else {
                    tentativeIsBetter = false;
                    isInOpenSet = true;
                }

                // Update the node with the new score
                if (tentativeIsBetter) {
                    Score yScore = scores.get(y);
                    yScore.prev = x;
                    yScore.g = tentativeGScore;
                    yScore.h = h(y, goal);
                    yScore.f = yScore.g + yScore.h;

                    // If this node was already in the open set from a previous
                    // expansion, update the existing entry, otherwise add it.
                    if (isInOpenSet) {
                        open.update(y);
                    } else {
                        open.add(y);
                    }
                }
            }
        }

        // If we tried all nodes and still didn't find a path, there isn't one.
        return Collections.emptyList();
    }
}
