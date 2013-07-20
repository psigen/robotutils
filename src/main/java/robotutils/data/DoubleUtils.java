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
 * This class determines the parameters of the floating point representation on
 * the current machine and uses this to construct appropriate relative error
 * comparison functions.
 *
 * Source: Besset, D. H., Object-Oriented Implementation of Numerical Methods:
 * an introduction with Java \& Smalltalk, 2000
 *
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public final class DoubleUtils {

    /** Radix used by floating-point numbers. */
    private final static int _radix = computeRadix();

    /** Largest positive value which, when added to 1.0, yields 0 */
    private final static double _machinePrecision = computeMachinePrecision();
    
    /** Typical meaningful precision for numerical calculations. */
    private final static double _defaultNumericalPrecision = Math.sqrt(_machinePrecision);

    private static int computeRadix() {
        int radix = 0;
        double a = 1.0d;
        double tmp1, tmp2;
        do {
            a += a;
            tmp1 = a + 1.0d;
            tmp2 = tmp1 - a;
        } while (tmp2 - 1.0d != 0.0d);
        double b = 1.0d;
        while (radix == 0) {
            b += b;
            tmp1 = a + b;
            radix = (int) (tmp1 - a);
        }
        return radix;
    }

    private static double computeMachinePrecision() {
        double floatingRadix = getRadix();
        double inverseRadix = 1.0d / floatingRadix;
        double machinePrecision = 1.0d;
        double tmp = 1.0d + machinePrecision;
        while (tmp - 1.0d != 0.0d) {
            machinePrecision *= inverseRadix;
            tmp = 1.0d + machinePrecision;
        }
        return machinePrecision;
    }

    public static int getRadix() {
        return _radix;
    }

    public static double getMachinePrecision() {
        return _machinePrecision;
    }

    public static double defaultNumericalPrecision() {
        return _defaultNumericalPrecision;
    }

    /**
     * @return true if the difference between a and b is less than
     * the default numerical precision
     */
    public static boolean equals(double a, double b) {
        return equals(a, b, defaultNumericalPrecision());
    }

    /**
     * @return true if the relative difference between a and b is
     * less than precision
     */
    public static boolean equals(double a, double b, double precision) {
        // This should handle positive and negative infinities with grace
        if (Double.compare(a, b) == 0) return true;

        // If we have real numbers, use a linearly-scaled numerical precision
        double norm = Math.max(Math.abs(a), Math.abs(b));
        return norm < precision || Math.abs(a - b) < precision * norm;
    }
}

