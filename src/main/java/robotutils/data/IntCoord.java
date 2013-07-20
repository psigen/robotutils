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

import java.util.Arrays;

/**
 * A simple tuple class that correctly represents an integer coordinate
 * in arbitrary dimensions.  Equality, hashcode and comparisons are all
 * implemented as a lexical ordering over the integer array elements.
 */
public class IntCoord implements Coordinate {

    final int[] _coords;

    public IntCoord(int... values) {
        _coords = Arrays.copyOf(values, values.length);
    }

    public int[] getInts() {
        return _coords;
    }

    public double[] get() {
        double[] d = new double[_coords.length];

        for (int i = 0; i < d.length; i++) {
            d[i] = (double)_coords[i];
        }

        return d;
    }

    public double get(int dim) {
        return (double)_coords[dim];
    }

    public int dims() {
        return _coords.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(_coords);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntCoord) {
            
            // Use int array compare if other object is also an integer array
            IntCoord that = (IntCoord)obj;
            return Arrays.equals(this._coords, that._coords);

        } else if (obj instanceof Coordinate) {

            // Use slower double array compare if other object in unknown
            Coordinate that = (Coordinate)obj;
            return Arrays.equals(this.get(), that.get());

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Arrays.hashCode(this._coords);
        return hash;
    }
}
