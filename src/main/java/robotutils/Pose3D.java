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
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import java.io.Serializable;

/**
 * Immutable 6DOF pose for 3D object.
 * Math adapted from www.euclideanspace.com.
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class Pose3D implements Cloneable, Serializable {
    
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
     * 3D cartesian coordinates.
     */
    private final double x, y, z;
    
    /**
     * 4D rotation quaternion.
     */
    private final Quaternion rotation;
    
    /** 
     * Constructs a new pose.
     * @param position the 3D position of the robot.
     * @param rotation either a 4D quaternion or a 3D roll-pitch-yaw vector.
     */
    public Pose3D(double[] position, double[] rotation) {
        // Fail if position matrix does not match expected size
        if (position.length != 3) 
            throw new IllegalArgumentException("Position must be a 3D vector.");
        this.x = position[0];
        this.y = position[1];
        this.z = position[2];
        
        // If we get three rotation params, assume we got RPY format
        if (rotation.length == 3) {
            // convert from RPY to quaternion
            this.rotation = Quaternion.fromEulerAngles(rotation[0], rotation[1], 
                    rotation[2]);
        } else if (rotation.length == 4) {
            // already in quaternion format
            this.rotation = new Quaternion(rotation[0], rotation[1], 
                    rotation[2], rotation[3]);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /** 
     * Constructs a new pose from a homogeneous transformation.
     * @param transform a 4x4 homogeneous absolute transformation.
     */
    public Pose3D(RealMatrix transform) {
        // Fail if transformation matrix does not match expected size
        if ( (transform.getRowDimension() != 4) 
                || (transform.getColumnDimension() != 4) )
            throw new IllegalArgumentException();
        
        this.x = transform.getEntry(0,3);
        this.y = transform.getEntry(1,3);
        this.z = transform.getEntry(2,3);
        this.rotation = Quaternion.fromTransform(transform);
    }
    
    /** 
     * Constructs a new pose.
     * @param x the X position of the robot.
     * @param y the Y position of the robot.
     * @param z the Z position of the robot.
     * @param q the rotation of the robot in quaternion form.
     */
    public Pose3D(double x, double y, double z, Quaternion q) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = q;
    }
    
    /** 
     * Constructs a new pose.
     * @param x the X position of the robot.
     * @param y the Y position of the robot.
     * @param z the Z position of the robot.
     * @param qw the scalar component of the quaternion
     * @param qx the "i" component of the quaternion
     * @param qy the "j" component of the quaternion
     * @param qz the "k" component of the quaternion
     */
    public Pose3D(double x, double y, double z, 
            double qw, double qx, double qy, double qz) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = new Quaternion(qw, qx, qy, qz);
    }
    
    /** 
     * Constructs a new pose.
     * @param x the X position of the robot.
     * @param y the Y position of the robot.
     * @param z the Z position of the robot.
     * @param roll the positive X axis rotation of the robot.
     * @param pitch the positive Y axis rotation of the robot.
     * @param yaw the positive Z axis rotation of the robot.
     */
    public Pose3D(double x, double y, double z, 
            double roll, double pitch, double yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = Quaternion.fromEulerAngles(roll, pitch, yaw);
    }
    
    /**
     * Accessor for the X position of the robot.
     * @return the X position of the robot.
     */
    public double getX() { return x; }
    
    /**
     * Accessor for the Y position of the robot.
     * @return the Y position of the robot.
     */
    public double getY() { return y; }
    
    /**
     * Accessor for the Z position of the robot.
     * @return the Z position of the robot.
     */
    public double getZ() { return z; }
    
    /**
     * Accessor for the position of the robot.
     * @return the 3D position of the robot.
     */
    public double[] getPosition() { return new double[] { x, y, z }; }
    
    /**
     * Accessor for the position of the robot in Jama matrix form.
     * @return the 3D position of the robot as a Jama matrix.
     */
    public RealVector getPositionVector() { return new ArrayRealVector(getPosition()); }
    
    /**
     * Accessor for the quaternion of the robot orientation in [w x y z] form.
     * @return the 4D quaternion representing robot orientation.
     */
    public Quaternion getRotation() { return rotation; }
    
    /** 
     * Accessor for the robot pose as a homogenous transformation.
     * @return a 4x4 homogeneous tranformation matrix for robot pose.
     */
    public RealMatrix getTransform() {
        RealMatrix ht = rotation.toRotation();
        ht.setEntry(0, 3, x);
        ht.setEntry(1, 3, y);
        ht.setEntry(2, 3, z);
        return ht;
    }
    
    /** 
     * Computes the square of the Euclidean distance between this pose and 
     * the specified pose.  This distance is solely based on 3D position, and
     * does not compare the rotations of the two poses.  
     * 
     * Fields contaning NaN are ignored in the computation.
     * @param p the pose to which distance is calculated
     * @return the square of the Euclidean distance between positions.
     */
    public double getEuclideanDistanceSqr(Pose3D p) {
        double dist = 0.0;
        
        // Discount fields that are NaN
        if (!Double.isNaN(this.x) && !Double.isNaN(p.x)) {
            // Take the square of the distance
            double diffX = (this.x - p.x);
            dist += diffX * diffX;
        }
            
        // Discount fields that are NaN
        if (!Double.isNaN(this.y) && !Double.isNaN(p.y)) {
            // Take the square of the distance
            double diffY = (this.y - p.y);
            dist += diffY * diffY;
        }
        
        // Discount fields that are NaN
        if (!Double.isNaN(this.z) && !Double.isNaN(p.z)) {
            // Take the square of the distance
            double diffZ = (this.z - p.z);
            dist += diffZ * diffZ;
        }
        
        return dist;
    }
    
    /** 
     * Computes the Euclidean distance between this pose and the specified pose.
     * This distance is solely based on 3D position, and does not compare the 
     * rotations of the two poses.  
     * 
     * Fields contaning NaN are ignored in the computation.  If this computation
     * is being compared to a constant, consider using getEuclideanDistance,
     * as it is faster.
     * @param p the pose to which distance is calculated
     * @return the Euclidean distance between positions.
     */
    public double getEuclideanDistance(Pose3D p) {
        return Math.sqrt(getEuclideanDistanceSqr(p));
    }
    
    /**
     * Checks whether this pose matches the other given pose given rounding 
     * errors and wildcard fields (NaN).
     * 
     * Note that this comparison is reflexive and symmetric, but not transitive.
     * @param p the pose against which to compare.
     * @return true if the poses are equivalent, false otherwise.
     */
    public boolean isEquivalent(Pose3D p) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pose3D)) return false;
        Pose3D p = (Pose3D)obj;
        
        if (this.x != p.x) return false;
        if (this.y != p.y) return false;
        if (this.z != p.z) return false;
        if (this.rotation != p.rotation) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 13 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        hash = 13 * hash + (this.rotation != null ? this.rotation.hashCode() : 0);
        return hash;
    }
    
    @Override
    public Pose3D clone() {
        return new Pose3D(getPosition(), rotation.getArray());
    }
    
    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + z + ", " + rotation + "}";
    }
    
    /**
     * Converts the native 6D pose to an (x,y,theta) 3D representation.
     * @return a 3D mapper pose representation
     * @deprecated The Mapper.Pose2D is only 3D, thus its use is discouraged. 
     */
    @Deprecated
    public Pose2D convertToPose2D() {
        return new Pose2D(x, y, rotation.toYaw());
    }
    
    /**
     * Executes a series of unit tests to ensure the functionality of the 
     * Pose2D class, mostly testing conversion functions.
     * @param args all command line arguments are ignored.
     */
    public static void main(String args[]) {
        Pose3D p = new Pose3D(100.0, 10.0, 20.0, 3.45, -2.34, 1.23);
        Pose3D q = new Pose3D(p.getTransform());

        RealMatrix m = p.getTransform();
        RealMatrix t = MatrixUtils.createRealMatrix(new double[][] {
            {-0.23248, -0.51489, -0.82512, 100.0},
            {-0.71846,  0.66274, -0.21113, 10.0},
            { 0.65556,  0.54374, -0.52400, 20.0},
            {0, 0, 0, 1}
            });
        Pose3D r = new Pose3D(t);
        
        System.out.println("The following poses should be similar:");
        System.out.println(p);
        System.out.println(q);
        System.out.println(r);
        
        System.out.println("");
        System.out.println("The following matrices should be similar:");
        System.out.println(m);
        System.out.println(t);
    }
}
