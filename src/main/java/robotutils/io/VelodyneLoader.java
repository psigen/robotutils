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

package robotutils.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Pattern;
import robotutils.Pose3D;
import robotutils.filters.OccupancyMap;

/**
 * Contains a script to load velodyne data from some random format.
 * 
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class VelodyneLoader {
    private static Pattern linePat = Pattern.compile(" *, *");

    BufferedReader state;
    BufferedReader laser;

    Vehicle curState = new Vehicle();

    static class Scan {
        public double time;
        public Pose3D ray;
    }

    static class Vehicle {
        public double time;
        public Pose3D pose;
    }

    public static class Ray {
        double time;
        double pos[];
        double ray[];
    }

    public void load(String stateFile, String laserFile) throws FileNotFoundException {
        state = new BufferedReader(new FileReader(stateFile));
        laser = new BufferedReader(new FileReader(laserFile));

        curState.time = Double.NEGATIVE_INFINITY;
    }

    public Ray step() {
        try {
            // Get next laser scan
            String scanLine = laser.readLine();
            if (scanLine == null) return null;
            Scan curScan = parseScan(scanLine);

            while (curState.time < curScan.time) {
                String stateLine = state.readLine();
                if (stateLine == null) return null;
                curState = parseState(stateLine);
            }

            // Get vehicle state at time of next laser scan
            Ray ray = new Ray();
            ray.time = curScan.time;
            ray.pos = curState.pose.getPosition();
            ray.ray = curState.pose.getRotation().toRotation().operate(curScan.ray.getPosition());

            // Return next laser scan and state
            return ray;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    Scan parseScan(String line) {
        String[] s = linePat.split(line);
        
        Scan sc = new Scan();
        sc.time = Double.parseDouble(s[0]);
        
        double x = Double.parseDouble(s[5]);
        double y = Double.parseDouble(s[6]);
        double z = Double.parseDouble(s[7]);
        
        sc.ray = new Pose3D(x, y, z, 0.0, 0.0, 0.0);
        return sc;
    }

    Vehicle parseState(String line) {
        String[] s = linePat.split(line);
        
        Vehicle veh = new Vehicle();
        veh.time = Double.parseDouble(s[0]);

        double x = Double.parseDouble(s[1]);
        double y = Double.parseDouble(s[2]);
        double z = Double.parseDouble(s[3]);

        double yaw = Double.parseDouble(s[4]);
        double pitch = Double.parseDouble(s[5]);
        double roll = Double.parseDouble(s[6]);

        veh.pose = new Pose3D(x, y, z, roll, pitch, yaw);
        return veh;
    }

    public static void main(String[] args) throws IOException {
        String vehFile = "/Users/pkv/Desktop/velodyne/VehicleState.csv";
        String veloDir = "/Users/pkv/Desktop/velodyne";

        File dir = new File(veloDir);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("velodyne");
            }
        };
        String[] veloFiles = dir.list(filter);

        VelodyneLoader vl = new VelodyneLoader();
        OccupancyMap omap = new OccupancyMap(400, 400, 400,
                //4473923.999042602, 589438.581795943, -201.3608791894574,
                4473723.999042602, 589238.581795943, -241.3608791894574,
                1);

        for (String veloFile : veloFiles) {
            System.out.println(veloFile);
            vl.load(vehFile, dir.getPath() + File.separator + veloFile);
            loadData(omap, vl);
        }
       
        omap.save("/Users/pkv/Desktop/velodyne/out.df3");
    }

    public static void loadData(OccupancyMap omap, VelodyneLoader vl) {
        Ray r = new Ray();

        for (int i = 1; i < 10000000; i++) {
            // Get next scan
            for (int j = 1; j < 100; j++) {
                r = vl.step();
                if (r == null) return;
            }

            // Add scan to occupancy grid
            if (r.ray[0] == 0) {
                continue;
            }
            if (r.ray[1] == 0) {
                continue;
            }
            if (r.ray[2] == 0) {
                continue;
            }
            omap.addScan(r.pos, r.ray);

            // On certain intervals output iteration number
            if (i % 10000 == 0) {
                System.out.println("i = " + i);
            }
        }
    }
}
