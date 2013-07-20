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

/**
 * Contains helper methods for operations on coordinates.
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class CoordUtils {

    /**
     * Returns Manhattan distance from this point to another.
     * 
     * @param a the first point
     * @param b the second point
     * @return the Manhattan distance between the two points
     */
    public static final double mdist(Coordinate a, Coordinate b) {

        if (a == null || b == null) {
            throw new IllegalArgumentException("No distance to null array.");
        } else if (a.dims() != a.dims()) {
            throw new IllegalArgumentException("Coordinate lengths don't match.");
        }

        int dist = 0;
        for (int i = 0; i < a.dims(); i++) {
            dist += Math.abs(a.get(i) - b.get(i));
        }

        return dist;
    }

    /**
     * Returns Euclidean distance from this point to another.
     *
     * @param a the first point
     * @param b the second point
     * @return the Euclidean distance between the two points
     */
    public static final double edist(Coordinate a, Coordinate b) {
        
        if (a == null || b == null) {
            throw new IllegalArgumentException("No distance to null array.");
        } else if (a.dims() != a.dims()) {
            throw new IllegalArgumentException("Coordinate lengths don't match.");
        }

        double dist = 0;
        for (int i = 0; i < a.dims(); i++) {
            dist += (a.get(i) - b.get(i))*(a.get(i) - b.get(i));
        }

        return Math.sqrt(dist);
    }
}
