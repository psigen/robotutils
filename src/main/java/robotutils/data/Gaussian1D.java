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
package robotutils.data;

/**
 * A 1D Gaussian distribution built from a number of samples of some variable.
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class Gaussian1D {

    private int n = 0;
    private double M = Double.NaN;
    private double S = Double.NaN;

    public Gaussian1D(double... samples) {
        update(samples);
    }

    public void update(double x) {
        n++;
        if (n == 1) {
            M = x;
            S = 0;
        } else {
            double M_old = M;
            double S_old = S;

            // M(1) = x(1), M(k) = M(k-1) + (x(k) - M(k-1)) / k
            M = M_old + (x - M_old) / n;

            //S(1) = 0, S(k) = S(k-1) + (x(k) - M(k-1)) * (x(k) - M(k))
            S = S_old + (x - M_old) * (x - M);
        }
    }

    public void update(double... samples) {
        for (double x : samples) {
            update(x);
        }
    }

    public void update(Gaussian1D that) {
        if (this.n == 0) {
            this.M = that.M;
            this.S = that.S;
            this.n = that.n;
        } else {
            int new_n = this.n + that.n;
            double new_m = (this.M * this.n + that.M * that.n) / new_n;
            double new_s = (this.n * (this.S + (this.M - new_m) * (this.M - new_m)) + that.n * (that.S + (that.M - new_m) * (that.M - new_m))) / new_n;

            this.M = new_m;
            this.S = new_s;
            this.n = new_n;
        }
    }

    public double mu() {
        if (n == 0) {
            throw new IllegalStateException("Cannot compute mean with no samples.");
        } else {
            return M;
        }
    }

    public double sigma() {
        if (n == 0) {
            throw new IllegalStateException("Cannot compute standard deviation with no samples.");
        } else if (n == 1) {
            return 0.0;
        } else {
            return Math.sqrt(S / (n - 1));
        }
    }

    public double stderr() {
        return (sigma() / Math.sqrt(n));
    }

    public int n() {
        return n;
    }

    public double total() {
        return M * n;
    }
}
