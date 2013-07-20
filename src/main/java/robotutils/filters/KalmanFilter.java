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

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;

/**
 * A lightweight Kalman filter implementation based on the Wikipedia article.
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class KalmanFilter {
    
    /**
     * Predicted state.
     */
    protected RealMatrix _x;
    
    /**
     * Predicted state covariance.
     */
    protected RealMatrix _P;
    
    /**
     * Optimal Kalman gain.
     */
    protected RealMatrix _K;
        
    /**
     * Default process model (state transition matrix).
     */
    protected RealMatrix _F;
    
    /**
     * Default process noise.
     */
    protected RealMatrix _Q;
    
    /**
     * Default control model (maps control vector to state space).
     */
    protected RealMatrix _B;
    
    /**
     * Default observation model (maps observations to state space).
     */
    protected RealMatrix _H;
    
    /**
     * Default observation noise.
     */
    protected RealMatrix _R;
    
    /**
     * Constructs a Kalman filter with no default motion and observation
     * models.
     * @param x the initial state estimate.
     * @param P the initial state covariance.
     */
    public KalmanFilter(RealMatrix x, RealMatrix P) {
        _x = x;
        _P = P;
    }
    
    /**
     * Constructs a Kalman filter with a default motion model and no default
     * observation model.
     * @param x the initial state estimate.
     * @param P the initial state covariance.
     * @param F the default process model.
     * @param Q the default process noise.
     * @param B the default control model.
     */
    public KalmanFilter(RealMatrix x, RealMatrix P,
            RealMatrix F, RealMatrix Q, RealMatrix B) {
        this(x, P);
        
        _F = F;
        _Q = Q;
        _B = B;
    }
    
    /**
     * Constructs a Kalman filter with a default motion model and default
     * observation model.
     * @param x the initial state estimate.
     * @param P the initial state covariance.
     * @param F the default process model.
     * @param Q the default process noise.
     * @param B the default control model.
     * @param H the default observation model.
     * @param R the default observation noise.
     */
    public KalmanFilter(RealMatrix x, RealMatrix P,
            RealMatrix F, RealMatrix Q, RealMatrix B,
            RealMatrix H, RealMatrix R) {
        this(x, P, F, Q, B);
        
        _H = H;
        _R = R;
    }
    
    /**
     * Uses the previous state estimate and the default motion model to produce 
     * an estimate of the current state.
     * @param u the current control input.
     */
    public void predict(RealMatrix u) {
        predict(_F, _Q, _B, u);
    }
    
    /**
     * Uses the previous state estimate and the provided motion model to produce
     * an estimate of the current state.
     * @param F the process model.
     * @param Q the process noise.
     * @param B the control model.
     * @param u the current control input.
     */
    public void predict(RealMatrix F, RealMatrix Q, RealMatrix B, RealMatrix u) {
        _x = F.multiply(_x).add(B.multiply(u));
        _P = F.multiply(_P).multiply(F.transpose()).add(Q);
    }

    /**
     * Current measurement information is used to refine the state estimate 
     * using the default observation model.
     * @param z the current measurement.
     */
    public void update(RealMatrix z) {
        update(_H, _R, z);
    }
    
    /**
     * Current measurement information is used to refine the state estimate 
     * using the provided observation model.
     * @param H the observation model.
     * @param R the observation noise.
     * @param z the current measurement.
     */
    public void update(RealMatrix H, RealMatrix R, RealMatrix z) {
        // Create a non-square identity matrix
        RealMatrix I = MatrixUtils.createRealMatrix(_K.getRowDimension(), H.getColumnDimension());
        int dim = Math.min(_K.getRowDimension(), H.getColumnDimension());
        I = I.add(MatrixUtils.createRealIdentityMatrix(dim));

        // Apply the rest of the Kalman update
        RealMatrix y = z.subtract(H.multiply(_x));
        RealMatrix S = H.multiply(_P).multiply(H.transpose()).add(R);
        RealMatrix invS = new LUDecomposition(S).getSolver().getInverse();
        _K = _P.multiply(H.transpose()).multiply(invS);
        _x = _x.add(_K.multiply(y));
        _P = I.subtract(_K.multiply(H)).multiply(_P);
    }
    
    /**
     * Sets the current state estimate.
     * @param x the new state estimate.
     */
    public void setState(RealMatrix x) {
        _x = x;
    }
    
    /**
     * Gets the current state estimate.
     * @return the current state estimate.
     */
    public RealMatrix getState() {
        return _x.copy();
    }
    
    /**
     * Sets the current state covariance.
     * @param P the new state covariance.
     */
    public void setStateCov(RealMatrix P) {
        _P = P;
    }
    
    /**
     * Gets the current state covariance.
     * @return the current state covariance.
     */
    public RealMatrix getStateCov() {
        return _P.copy();
    }
    
    /**
     * Gets the most recently computed Kalman gain.
     * @return the current Kalman gain.
     */
    public RealMatrix getKalmanGain() {
        return _K.copy();
    }
    
    /**
     * Sets the default process model.
     * @param F the new process model. 
     */
    public void setProcessModel(RealMatrix F) {
        _F = F;
    }
    
    /**
     * Gets the default process model.
     * @return the default process model.
     */
    public RealMatrix getProcessModel() {
        return _F.copy();
    }
    
    /**
     * Sets the default process noise.
     * @param Q the new process noise.
     */
    public void setProcessNoise(RealMatrix Q) {
        _Q = Q;
    }
    
    /**
     * Gets the default process noise.
     * @return the default process noise.
     */
    public RealMatrix getProcessNoise() {
        return _Q.copy();
    }
    
    /**
     * Sets the default control model.
     * @param B the new control model.
     */
    public void setControlModel(RealMatrix B) {
        _B = B;
    }
    
    /**
     * Gets the default control model.
     * @return the default control model.
     */
    public RealMatrix getControlModel() {
        return _B.copy();
    }
    
    /**
     * Sets the default observation model.
     * @param H the new observation model.
     */
    public void setObsModel(RealMatrix H) {
        _H = H;
    }
    
    /**
     * Gets the default observation model.
     * @return the default observation model.
     */
    public RealMatrix getObsModel() {
        return _H.copy();
    }
    
    /**
     * Sets the default observation noise.
     * @param R the new observation noise.
     */
    public void setObsNoise(RealMatrix R) {
        _R = R;
    }
    
    /**
     * Gets the default observation noise.
     * @return the default observation noise.
     */
    public RealMatrix getObsNoise() {
        return _R.copy();
    }
    
}
