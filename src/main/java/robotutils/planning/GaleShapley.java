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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * This class implements the Gale-Shapley algorithm for stable marriages.  It
 * requires that cost functions be defined over the sets of both "suitors" and
 * "reviewer".
 *
 * Note that the algorithm is not symmetric in its optimality: as implemented,
 * it is optimal for the "suitors", but the stable, suitor-optimal solution
 * may or may not be optimal for the "reviewers".
 *
 * This algorithm should be <i>reasonably</i> efficient, but is not optimized.
 *
 * Source: http://en.wikipedia.org/wiki/Stable_marriage_problem
 *
 * @param <M> This is the first set type, the "suitors".
 * @param <W> This is the second set type, the "reviewers".
 *
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public abstract class GaleShapley<M,W> {

    /**
     * Creates a stable matching between the suitors and reviewers, using
     * implementation-specific cost functions.  
     * 
     * This implementation follows the 1962 Gale-Shapley algorithm, as described
     * on the referenced wikipedia page.
     *
     * Source: http://en.wikipedia.org/wiki/Stable_marriage_problem#Algorithm
     *
     * @param suitors a list of the suitor objects.
     * @param reviewers a list of the reviewer objects.
     * @return a stable, bijective, suitor-optimal map from suitors to reviewers.
     */
    public Map<M, W> match(final List<M> suitors, final List<W> reviewers) {

        // Create a free list of suitors (and use it to store their proposals)
        Queue<MTuple> freeSuitors = new LinkedList();
        for (M suitor : suitors) {
            LinkedList<W> prefs = new LinkedList(reviewers);
            Collections.sort(prefs, suitorPreference(suitor));
            freeSuitors.add(new MTuple(suitor, prefs));
        }

        // Create an initially empty map of engagements
        Map<W, MTuple> engagements = new HashMap();

        // As per wikipedia algorithm
        while (!freeSuitors.isEmpty()) {

            // The next free suitor who has a reviewer to propose to
            MTuple m = freeSuitors.peek();

            // m's highest ranked such woman who he has not proposed to yet
            W w = freeSuitors.peek().prefs.poll();

            // If that was the last proposal, remove it from the list
            if (freeSuitors.peek().prefs.isEmpty())
                freeSuitors.poll();

            // Tf w is free
            if (!engagements.containsKey(w)) {
                // (m, w) become engaged
                engagements.put(w, m);
            } else {
                // Some pair (m', w) already exists
                MTuple mPrime = engagements.get(w);
                if (reviewerPreference(w).compare(mPrime.suitor, m.suitor) < 0) {

                    // (m, w) become engaged
                    engagements.put(w, m);

                    // m' becomes free
                    freeSuitors.add(mPrime);

                } else {
                    // (m', w) remain engaged
                }
            }

        }

        // Convert internal data structure to mapping
        HashMap<M,W> matches = new HashMap();
        for (Map.Entry<W, MTuple> entry : engagements.entrySet())
            matches.put(entry.getValue().suitor, entry.getKey());
        return matches;
    }

    private class MTuple {
        final M suitor;
        final Queue<W> prefs;

        public MTuple(M s, Queue<W> p) {
            suitor = s;
            prefs = p;
        }
    }

    public abstract Comparator<W> suitorPreference(M suitor);
    public abstract Comparator<M> reviewerPreference(W reviewer);
}
