/*
 * Copyright (c) 2008, Prasanna Velagapudi <pkv@cs.cmu.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE PROJECT AND CONTRIBUTORS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE PROJECT AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package robotutils.filters;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * An extension of the Kalman filter that allows for the motion models to be 
 * linearized around the current state at each timestep.
 * 
 * Note that directly using the "set" accessors to the model and observation 
 * matrices will have no effect, as these matrices are now generated via the
 * provided linearization functions.
 * 
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public abstract class ExtendedKalmanFilter extends KalmanFilter {

    /**
     * Process model linearization function.
     * (Creates state transition matrix.)
     * @param x the state around which to linearize.
     * @return the linearized process model.
     */
    protected abstract RealMatrix F(RealMatrix x);

    /**
     * Process noise linearization function.
     * @param x the state around which to linearize.
     * @return the linearized process noise.
     */
    protected abstract RealMatrix Q(RealMatrix x);

    /**
     * Control model linearization function.
     * (Maps control vector to state space.)
     * @param x the state around which to linearize.
     * @return the linearized control model.
     */
    protected abstract RealMatrix B(RealMatrix x);

    /**
     * Observation model linearization function.
     * (Maps observations to state space.)
     * @param x the state around which to linearize.
     * @return the linearized observation model.
     */
    protected abstract RealMatrix H(RealMatrix x);

    /**
     * Observation noise linearization function.
     * @param x the state around which to linearize.
     * @return the linearized observation noise.
     */
    protected abstract RealMatrix R(RealMatrix x);

    /**
     * Constructs an extended Kalman filter with no default motion and 
     * observation models.
     * @param x the initial state estimate.
     * @param P the initial state covariance.
     */
    public ExtendedKalmanFilter(RealMatrix x, RealMatrix P) {
        super(x, P);
    }
    
    /**
     * @see KalmanFilter#predict(org.apache.commons.math.linear.RealMatrix)
     */
    @Override
    public void predict(RealMatrix u) {
        _F = F(_x);
        _Q = Q(_x);
        _B = B(_x);

        predict(u);
    }
    
    /**
     * @see KalmanFilter#update(org.apache.commons.math.linear.RealMatrix) 
     */
    @Override
    public void update(RealMatrix z) {
        _H = H(_x);
        _R = R(_x);
        
        update(z);
    }
    
    /**
     * @see KalmanFilter#setState(org.apache.commons.math.linear.RealMatrix) 
     */
    @Override
    public void setState(RealMatrix x) {
        super.setState(x);
    }
}
