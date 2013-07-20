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
import java.util.List;
import robotutils.data.CoordUtils;
import robotutils.data.GridMap;
import robotutils.data.IntCoord;

/**
 * Implementation of the A* algorithm that solves the planning problem for
 * an N-dimensional lattice, where movement can only occur in the 2N cardinal
 * directions.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class GridAStar extends AStar<IntCoord> {

    /**
     * The map over which A* searches will be performed.
     */
    final GridMap _map;

    /**
     * Instantiates A* search over the given map.
     * @param map A grid map where each cell represents a cost of traversal
     */
    public GridAStar(GridMap map) {
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
            if (_map.get(up) >= 0) {
                nbrs.add(new IntCoord(up));
            }

            int[] down = Arrays.copyOf(s.getInts(), s.getInts().length);
            down[i] -= 1;
            if (_map.get(down) >= 0) {
                nbrs.add(new IntCoord(down));
            }
        }

        return nbrs;
    }

    @Override
    protected Collection<IntCoord> succ(IntCoord s) {
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
            double cB = _map.get(b.getInts());

            double cost = (cA + cB)/2.0 + 1.0;
            if (cost <= 0) {
                throw new IllegalStateException();
            }
            return cost;
        }
    }

}
