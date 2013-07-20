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

package robotutils.filters;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * An extension of the Kalman filter that allows for the motion models to be
 * approximated linearized around the current state using sigma points.
 *
 * Note that directly using the "set" accessors to the model and observation
 * matrices will have no effect, as these matrices are now generated via the
 * provided linearization functions.
 * 
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class UnscentedKalmanFilter extends ExtendedKalmanFilter {

    /**
     * Constructs an extended Kalman filter with no default motion and
     * observation models.
     * @param x the initial state estimate.
     * @param P the initial state covariance.
     */
    public UnscentedKalmanFilter(RealMatrix x, RealMatrix P) {
        super(x, P);
    }

    @Override
    protected RealMatrix F(RealMatrix x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected RealMatrix Q(RealMatrix x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected RealMatrix B(RealMatrix x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected RealMatrix H(RealMatrix x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected RealMatrix R(RealMatrix x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
