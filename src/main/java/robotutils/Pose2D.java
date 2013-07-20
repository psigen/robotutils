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

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Immutable 3DOF pose for 2D object.
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class Pose2D implements Cloneable, Serializable {
    
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
     * 2D cartesian coordinates.
     */
    private final double x,  y;
    
    /**
     * Planar rotation, where the positive direction is defined for a 
     * right-handed coordinate system.
     */
    private final double theta;

    /**
     * Construct a new 2D pose.
     * @param x the x-coordinate of the object
     * @param y the y-coordinate of the object
     * @param theta the planar rotation of the object
     */
    public Pose2D(double x, double y, double theta) {
        this.x = x;
        this.y = y;
        this.theta = theta;
    }

    /**
     * Accessor for the X position of the object.
     * @return the X position of the object.
     */
    public double getX() { return x; }
    
    /**
     * Accessor for the Y position of the object.
     * @return the Y position of the object.
     */
    public double getY() { return y; }
    
    /**
     * Accessor for the rotation of the object.
     * @return the rotation of the robot.
     */
    public double getTheta() {
        return theta;
    }
    
    /**
     * Returns the pose as a 2D point without a rotation.
     * @return the 2D point with the coordinates of this pose.
     */
    public Point2D getPoint() {
        return new Point2D.Double(x, y);
    }

    @Override
    public String toString() {
        return "{" + x + ", " + y + ", " + theta + "}";
    }

    @Override
    public Pose2D clone() {
        return new Pose2D(x, y, theta);
    }

    @Deprecated
    public Pose3D convertToPose3D() {
        return new Pose3D(x, y, 0.0, 0.0, 0.0, theta);
    }
}
