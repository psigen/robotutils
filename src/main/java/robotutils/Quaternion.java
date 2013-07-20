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

package robotutils;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import java.io.Serializable;

/**
 * Immutable quaternion representing 3D rotation.
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class Quaternion implements Cloneable, Serializable {
    
    /**
     * Determines if a de-serialized object is compatible with this class.
     *
     * Maintainers must change this value if and only if the new version
     * of this class is not compatible with old versions. See Sun docs
     * for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
     * /serialization/spec/version.doc.html> details. </a>
     */
    public static final long serialVersionUID = 1L;
    
    /**
     * This defines the north pole singularity cutoff when converting 
     * from quaternions to Euler angles.
     */
    public static final double SINGULARITY_NORTH_POLE = 0.49999;
    
    /**
     * This defines the south pole singularity cutoff when converting 
     * from quaternions to Euler angles.
     */
    public static final double SINGULARITY_SOUTH_POLE = -0.49999;

    /**
     * 4D quaternion coordinates.
     */
    private final double w, x, y, z;

    /**
     * Construct a new quaternion.
     * 
     * @param w the w-coordinate of the object
     * @param x the x-coordinate of the object
     * @param y the y-coordinate of the object
     * @param z the z-coordinate of the object
     */
    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Wrap a quaternion in vector form.
     * @param q a quaternion vector in {w, x, y, z} form.
     */
    public Quaternion(double[] q) {
        if (q.length != 4) {
            throw new IllegalArgumentException("Quaternion vector must be 4D.");
        }
        
        this.w = q[0];
        this.x = q[1];
        this.y = q[2];
        this.z = q[3];
    }
    
    /**
     * Access the w-component (scalar) of the quaternion.
     * @return the w-component of the quaternion.
     */
    public double getW() { return w; }
    
    /**
     * Access the x-component ("i") of the quaternion.
     * @return the x-component of the quaternion.
     */
    public double getX() { return x; }
    
    /**
     * Access the y-component ("j") of the quaternion.
     * @return the y-component of the quaternion.
     */
    public double getY() { return y; }
    
    /**
     * Access the z-component ("k") of the quaternion.
     * @return the z-component of the quaternion.
     */
    public double getZ() { return z; }
    
    /**
     * Access the components of the quaternion.
     * @return the components of the quaternion in {w, x, y, z} form.
     */
    public double[] getArray() {
        return new double[] {w, x, y, z};
    }
    
    public static Quaternion fromRotation(RealMatrix m) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Converts quaternion to (3x3) rotation matrix.
     *
     * @return a 2D 3x3 rotation matrix representing the quaternion.
     */
    public RealMatrix toRotation() {
        double[][] m = new double[3][3];
        
        // Compute necessary components
        double xx = x*x;
        double xy = x*y;
        double xz = x*z;
        double xw = x*w;
        double yy = y*y;
        double yz = y*z;
        double yw = y*w;
        double zz = z*z;
        double zw = z*w;
        
        // Compute rotation tranformation
        // Compute rotation tranformation
        m[0][0] = 1 - 2 * ( yy + zz );
        m[0][1] =     2 * ( xy - zw );
        m[0][2] =     2 * ( xz + yw );
        m[1][0] =     2 * ( xy + zw );
        m[1][1] = 1 - 2 * ( xx + zz );
        m[1][2] =     2 * ( yz - xw );
        m[2][0] =     2 * ( xz - yw );
        m[2][1] =     2 * ( yz + xw );
        m[2][2] = 1 - 2 * ( xx + yy );
        
        // Put into Jama format
        return MatrixUtils.createRealMatrix(m);
    }
    
    public static Quaternion fromTransform(RealMatrix t) {
        double[] q = new double[4];
        double[][] m = t.getData();
        
        // Recover the magnitudes
        q[0] = Math.sqrt( Math.max( 0, 1 + m[0][0] + m[1][1] + m[2][2] ) ) / 2; 
        q[1] = Math.sqrt( Math.max( 0, 1 + m[0][0] - m[1][1] - m[2][2] ) ) / 2; 
        q[2] = Math.sqrt( Math.max( 0, 1 - m[0][0] + m[1][1] - m[2][2] ) ) / 2; 
        q[3] = Math.sqrt( Math.max( 0, 1 - m[0][0] - m[1][1] + m[2][2] ) ) / 2; 
        
        // Recover sign information
        q[1] *= Math.signum( m[2][1] - m[1][2] ); 
        q[2] *= Math.signum( m[0][2] - m[2][0] );
        q[3] *= Math.signum( m[1][0] - m[0][1] ); 
        
        return new Quaternion(q);
    }
    
    public RealMatrix toTransform() {
        double[][] m = new double[4][4];
        
        // Compute necessary components
        double xx = x*x;
        double xy = x*y;
        double xz = x*z;
        double xw = x*w;
        double yy = y*y;
        double yz = y*z;
        double yw = y*w;
        double zz = z*z;
        double zw = z*w;
        
        // Compute rotation tranformation
        m[0][0] = 1 - 2 * ( yy + zz );
        m[0][1] =     2 * ( xy - zw );
        m[0][2] =     2 * ( xz + yw );
        m[1][0] =     2 * ( xy + zw );
        m[1][1] = 1 - 2 * ( xx + zz );
        m[1][2] =     2 * ( yz - xw );
        m[2][0] =     2 * ( xz - yw );
        m[2][1] =     2 * ( yz + xw );
        m[2][2] = 1 - 2 * ( xx + yy );
        m[0][3] = m[1][3] = m[2][3] = m[3][0] = m[3][1] = m[3][2] = 0;
        m[3][3] = 1;
        
        // Put into Jama format
        return MatrixUtils.createRealMatrix(m);
    }
    
    public static Quaternion fromEulerAngles(double roll, double pitch, double yaw) {
        double q[] = new double[4];
        
        // Apply Euler angle transformations
        // Derivation from www.euclideanspace.com
        double c1 = Math.cos(yaw/2.0);
        double s1 = Math.sin(yaw/2.0);
        double c2 = Math.cos(pitch/2.0);
        double s2 = Math.sin(pitch/2.0);
        double c3 = Math.cos(roll/2.0);
        double s3 = Math.sin(roll/2.0);
        double c1c2 = c1*c2;
        double s1s2 = s1*s2;
        
        // Compute quaternion from components
        q[0] = c1c2*c3 - s1s2*s3;
        q[1] = c1c2*s3 + s1s2*c3;
        q[2] = s1*c2*c3 + c1*s2*s3;
        q[3] = c1*s2*c3 - s1*c2*s3;
        return new Quaternion(q);
    }
    
    /**
     * Returns the roll component of the quaternion if it is represented
     * as standard roll-pitch-yaw Euler angles.
     * @return the roll (x-axis rotation) of the robot.
     */
    public double toRoll() {
        // This is a test for singularities
        double test = x*y + z*w;
        
        // Special case for north pole
        if (test > SINGULARITY_NORTH_POLE)
            return 0;
        
        // Special case for south pole
        if (test < SINGULARITY_SOUTH_POLE)
            return 0;
            
        return Math.atan2( 
                    2*x*w - 2*y*z,
                    1 - 2*x*x - 2*z*z
                ); 
    }
    
    /**
     * Returns the pitch component of the quaternion if it is represented
     * as standard roll-pitch-yaw Euler angles.
     * @return the pitch (y-axis rotation) of the robot.
     */
    public double toPitch() {
        // This is a test for singularities
        double test = x*y + z*w;
        
        // Special case for north pole
        if (test > SINGULARITY_NORTH_POLE)
            return Math.PI/2;
        
        // Special case for south pole
        if (test < SINGULARITY_SOUTH_POLE)
            return -Math.PI/2;
        
        return Math.asin(2*test); 
    }
    
    /**
     * Returns the yaw component of the quaternion if it is represented
     * as standard roll-pitch-yaw Euler angles.
     * @return the yaw (z-axis rotation) of the robot.
     */
    public double toYaw() {
        // This is a test for singularities
        double test = x*y + z*w;
        
        // Special case for north pole
        if (test > SINGULARITY_NORTH_POLE)
            return 2 * Math.atan2(x, w);
        
        // Special case for south pole
        if (test < SINGULARITY_SOUTH_POLE)
            return -2 * Math.atan2(x, w);
        
        return Math.atan2(
                    2*y*w - 2*x*z,
                    1 - 2*y*y - 2*z*z
                ); 

    }
    
    /**
     * Returns the components of the quaternion if it is represented
     * as standard roll-pitch-yaw Euler angles.
     * @return an array of the form {roll, pitch, yaw}.
     */
    public double[] toEulerAngles() {
        return new double[] { toRoll(), toPitch(), toYaw() };
    }
    
    @Override
    public Quaternion clone() {
        return new Quaternion(w, x, y, z);
    }
    
    @Override
    public String toString() {
        return "Q[" + toRoll() + "," + toPitch() + "," + toYaw() + "]";
    }   
}
