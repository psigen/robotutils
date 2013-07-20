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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import robotutils.data.CoordUtils;
import robotutils.data.GridMap;
import robotutils.data.IntCoord;

/**
 * Implementation of the D*-lite algorithm that solves the planning problem for
 * an N-dimensional lattice, where movement can only occur in the 2N cardinal
 * directions.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class GridDStar extends DStarLite<IntCoord> {

    /**
     * The map over which searches will be performed.
     */
    GridMap _map;

    /**
     * Instantiates A* search over the given map.
     * @param map a grid map where each cell represents a cost of traversal
     * @param start the initial position
     * @param goal the final position
     */
    public GridDStar(GridMap map, IntCoord start, IntCoord goal) {

        super(start, goal);
        _map = map;
    }

    /**
     * Returns a list of neighbors to the current grid cell, excluding neighbor
     * cells that have negative cost values.
     * @param s the current cell
     * @return a list of neighboring cells
     */
    protected Collection<IntCoord> nbrs(IntCoord s) {
        List<IntCoord> nbrs = new ArrayList(2*_map.dims());

        for (int i = 0; i < _map.dims(); i++) {
            int[] up = Arrays.copyOf(s.getInts(), s.getInts().length);
            up[i] += 1;
            nbrs.add(new IntCoord(up));
            
            int[] down = Arrays.copyOf(s.getInts(), s.getInts().length);
            down[i] -= 1;
            nbrs.add(new IntCoord(down));
        }

        return nbrs;
    }

    @Override
    protected Collection<IntCoord> succ(IntCoord s) {
        return nbrs(s);
    }

    @Override
    protected Collection<IntCoord> pred(IntCoord s) {
        return nbrs(s);
    }

    @Override
    protected double h(IntCoord a, IntCoord b) {
        return CoordUtils.mdist(a, b);
    }

    @Override
    protected double c(IntCoord a, IntCoord b) {

        if (CoordUtils.mdist(a, b) != 1) {
            return Double.POSITIVE_INFINITY;
        } else {
            double cA = _map.get(a.getInts());
            if (cA < 0) return Double.POSITIVE_INFINITY;

            double cB = _map.get(b.getInts());
            if (cB < 0) return Double.POSITIVE_INFINITY;

            double cost = (cA + cB)/2.0 + 1.0;
            if (cost <= 0) {
                throw new IllegalStateException();
            }
            return cost;
        }
    }

    public void setCost(IntCoord s, byte val) {

        // If the cost hasn't changed, don't do anything
        if (val == _map.get(s.getInts()))
            return;

        // Find all affected edges (tuples of this cell and its neighbors)
        Collection<IntCoord> preds = pred(s);
        Collection<IntCoord> succs = succ(s);

        // Record old costs
        HashMap<IntCoord, Double> predVals = new HashMap();
        for (IntCoord sPrime : preds) {
            predVals.put(sPrime, c(sPrime, s));
        }

        HashMap<IntCoord, Double> succVals = new HashMap();
        for (IntCoord sPrime : succs) {
            succVals.put(sPrime, c(s, sPrime));
        }

        // Change map cost
        _map.set(val, s.getInts());

        // Flag changes to new costs
        for (IntCoord sPrime : preds) {
            flagChange(sPrime, s, predVals.get(sPrime), c(sPrime, s));
        }

        for (IntCoord sPrime : succs) {
            flagChange(s, sPrime, succVals.get(sPrime), c(s, sPrime));
        }
    }

    public void setStart(IntCoord start) {
        updateStart(start);
    }
}
