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

package robotutils.filters;

import robotutils.data.StaticMap;
import robotutils.data.GridMap;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class OccupancyMap {
    double ctr[];
    double res;

    GridMap map = new StaticMap();

    public OccupancyMap(int x, int y, int z,
            double xctr, double yctr, double zctr,
            double r) {
        map.resize(new int[] {x, y, z});
        ctr = new double[] {xctr, yctr, zctr};
        res = r;
    }

    public void addScan(double[] pos, double[] ray) {
        int[] pt = new int[3];

        pt[0] = (int)((pos[0] + ray[0]) / res - ctr[0]);
        pt[1] = (int)((pos[1] + ray[1]) / res - ctr[1]);
        pt[2] = (int)((pos[2] + ray[2]) / res - ctr[2]);

        //System.out.println("Point at " + Arrays.toString(pt) + ", " + map.get(pt));

        map.set((byte)255, pt);
    }

    public void save(String filename) {
        try {
            System.out.println("START: Serializing map.");
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));

            out.writeShort(map.size(0));
            out.writeShort(map.size(1));
            out.writeShort(map.size(2));

            int[] idx = new int[3];
            for (idx[2] = 0; idx[2] < map.size(2); idx[2]++) {
                for (idx[1] = 0; idx[1] < map.size(1); idx[1]++) {
                    for (idx[0] = 0; idx[0] < map.size(0); idx[0]++) {
                        out.write((map.get(idx) == 0) ? 0 : 255);
                    }
                }
            }

            out.flush();
            out.close();
            System.out.println("FINISH: Serializing map.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
