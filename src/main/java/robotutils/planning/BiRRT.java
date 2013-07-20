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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements an unoptimized BiRRT algorithm (bidirectional rapidly
 * exploring random tree) as described in [LaValle 2001]. RRTs attempt to
 * rapidly and uniformly explore state space.  They are well-suited to high
 * dimensional planning problems and problems with differential constraints.
 *
 * The BiRRT expands two trees, one from the start state and one from the goal
 * state, terminating when the two trees meet at any intermediate location.  It
 * alternates between growing the trees toward each other and expanding the
 * trees to cover the state space.
 * 
 * Source: S.M. LaValle and J.J. Kuffner. Randomized kinodynamic planning.
 * International Journal of Robotics Research, 20(5):378-400, May 2001.
 *
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public abstract class BiRRT<State, Action> {

    /**
     * The default number of tree expansions before a search returns failure.
     */
    public static int DEFAULT_ITERATION_LIMIT = 2000;

    /**
     * Indicates that a tree was expanded toward some state.
     */
    public static int ADVANCED = -1;

    /**
     * Indicates that a tree was able to reach some state.
     */
    public static int REACHED = 0;

    /**
     * Indicates that a tree was unable to progress toward some state.
     */
    public static int TRAPPED = -2;

    /**
     * Storage class for state/action pairs.
     *
     * @param <S> the state representation
     * @param <A> the action representation
     */
    public static final class Tuple<S, A> {

        public final S _state;
        public final A _action;

        public Tuple(S state, A action) {
            _state = state;
            _action = action;
        }

        public Node<S,A> toNode(int parent) {
            return new Node<S, A>(_state, _action, parent);
        }
    }

    /**
     * Storage class for nodes in the search trees.
     * 
     * @param <S> the state representation
     * @param <A> the action representation
     */
    protected static final class Node<S, A> {

        public final S _state;
        public final A _action;
        public final int _parent;

        public Node(S state, A action, int parent) {
            _state = state;
            _action = action;
            _parent = parent;
        }

        public Tuple<S,A> toTuple() {
            return new Tuple<S, A>(_state, _action);
        }
    }

    /**
     * Computes the distance between two states according to some metric.
     * The metric must be <b>transitive</b> and <b>commutative</b>.
     *
     * @param a some state
     * @param b some other state
     * @return the distance between the two states
     */
    protected abstract double distance(State a, State b);

    /**
     * Generates a state/action pair that moves from the "near" state toward the 
     * sampled state, and returns the <i>sampled</i> state in the state/action
     * pair if the action gets <i>sufficiently</i> close to the state.
     *
     * @param x a randomly sampled state
     * @param xNear a nearby reachable state (already in the search tree)
     * @return a state/action pair where executing the returned action will
     * result in moving from the nearby state to the returned state
     */
    protected abstract Tuple<State, Action> newState(State x, State xNear);

    /**
     * Randomly generate a state to try and expand toward.
     * 
     * @return a randomly generated state
     */
    protected abstract State randomState();

    /**
     * A simple linear search for the closest existing node in the tree to
     * a specified state, using the specified distance function.
     *
     * @see BiRRT#distance(java.lang.Object, java.lang.Object) 
     *
     * @param x the state to try to approach
     * @param t a tree of existing nodes
     * @return the tree index of the closest existing node
     */
    protected int nearestNeighbor(State x, List<Node<State, Action> > t) {

        // Initialize to worst case values
        double minDist = Double.MAX_VALUE;
        int minIdx = -1;

        // Initialize some temp variable to store current node
        Node<State, Action> curNode = null;
        double curDist;

        // Iterate through tree, looking for closest node
        for (int idx = 0; idx < t.size(); idx++) {
            curNode = t.get(idx);
            curDist = distance(curNode._state, x);

            // If this is the closest node, record index and distance
            if (curDist < minDist) {
                minIdx = idx;
                minDist = curDist;
            }
        }

        // Return index of the closest node in the tree
        return minIdx;
    }

    /**
     * Extends a given search tree towards the specified states, adding a new
     * node that either reached the given state, or tries to provide a nearer
     * state, using the newState() function.
     *
     * @see BiRRT#newState(java.lang.Object, java.lang.Object)
     * @see BiRRT#ADVANCED
     * @see BiRRT#REACHED
     * @see BiRRT#TRAPPED
     *
     * @param t the tree to be expanded
     * @param x the state toward which to expand the tree
     * @return a code indicating if the state was reached, if a new nearby state
     * was reached, or if no closer state could be found.
     */
    protected int extend(List<Node<State, Action> > t, State x) {

        // Find the closest existing state in the tree
        int iNear = nearestNeighbor(x, t);
        State xNear = t.get(iNear)._state;

        // Use the newState function to construct a new edge toward the state
        Tuple<State, Action> xuNew = newState(x, xNear);

        // If we got a state/action, add it to the tree
        if (xuNew != null) {
            t.add(xuNew.toNode(iNear));

            // Did we reach the target state or just expand toward it?
            if (xuNew._state.equals(x)) {
                return REACHED;
            } else {
                return ADVANCED;
            }
        }

        // If we didn't get a state/action, report failure
        return TRAPPED;
    }

    /**
     * Generates a path from two tree lists in a BiRRT.  Assumes that the last
     * node in the trees are the ones that matched.
     * 
     * @param tStart the start tree (rooted at the starting node)
     * @param tGoal the goal tree (rooted at the goal node)
     * @return a list of states/actions that connect the two trees.
     */
    private List<Tuple<State, Action> > path( List<Node<State, Action> > tStart, List<Node<State, Action> > tGoal) {

        // Create a linked list to assemble from both ends
        LinkedList<Tuple<State, Action> > path = new LinkedList();

        // Iteratively build path from starting node
        Node nA = tStart.get(tStart.size() - 1);
        path.add(nA.toTuple());
        while (nA._parent > 0) {
            nA = tGoal.get(nA._parent);
            path.addFirst(nA.toTuple());
        }

        // Iteratively build path to ending node
        Node nB = tGoal.get(tGoal.size() - 1);
        while (nB._parent > 0) {
            nB = tGoal.get(nB._parent);
            path.addLast(nB.toTuple());
        }

        return path;
    }

    /**
     * Executes a BiRRT search from the provide start to the provided goal.
     * This version uses the default number of iterations.
     *
     * @see BiRRT#DEFAULT_ITERATION_LIMIT
     * @see BiRRT#plan(java.lang.Object, java.lang.Object, int)
     *
     * @param start the desired initial state
     * @param goal the desired final state
     * @return a list of state/action tuples, or an empty list on failure.
     */
    public List<Tuple<State,Action> > plan(State start, State goal) {
        return plan(start, goal, DEFAULT_ITERATION_LIMIT);
    }

    /**
     * Executes a BiRRT search from the provide start to the provided goal.
     * 
     * @param start the desired initial state
     * @param goal the desired final state
     * @param iterations the number of iterations until terminating with failure.
     * @return a list of state/action tuples, or an empty list on failure.
     */
    public List<Tuple<State,Action> > plan(State start, State goal, int iterations) {

        // Create the start tree and add the starting state
        List<Node<State,Action> > tStart = new ArrayList();
        tStart.add(new Node(start, null, -1));

        // Create the goal tree and add the goal state
        List<Node<State,Action> > tGoal = new ArrayList();
        tGoal.add(new Node(goal, null, -1));

        // Assign the start and goal trees to "A" and "B"
        List<Node<State,Action> > tA = tStart;
        List<Node<State,Action> > tB = tGoal;

        // Iterate until limit is reached
        for (int k = 0; k < iterations; k++) {

            // Pick a random state using the generating function
            State xRand = randomState();

            // Attempt to extend the "A" tree toward the random state
            if (!(extend(tA, xRand) == TRAPPED)) {

                // Get the new state that was added to the "A" tree
                State xNew = tA.get(tA.size() - 1)._state;

                // Attempt to extend the "B" tree to reach this new node
                if (extend(tB, xNew) == REACHED) {

                    // If we succeeded, reconstruct this path
                    return path(tStart, tGoal);
                }
            }

            // Switch the "A" and "B" trees to expand the other one
            // SWAP(Ta, Tb);
            List<Node<State, Action> > tmp = tA;
            tA = tB;
            tB = tmp;
        }

        return Collections.emptyList();
    }
}
