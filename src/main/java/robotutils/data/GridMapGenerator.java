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
 * Contains generation functions for various simple grid maps.
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class GridMapGenerator {

    public static StaticMap createRandomMazeMap2D(int width, int height) {
        
        StaticMap map = new StaticMap();
        map.resize(width, height);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (i == 0 || i == width - 1 || j == 0 || j == height - 1) {
                    map.set((byte)255, i, j); // Map borders are untraversable
                } else {
                    int a = (i < 1) ? 0 : map.get(i - 1, j); // left
                    int b = (j < 1) ? 0 : map.get(i, j - 1); // up
                    int c = ((i < 1) || (j < 1)) ? 0 : map.get(i - 1, j - 1); //up-left

                    double prob;
                    if (a != 0) {
                        if (b != 0) {
                            if (c != 0) {
                                prob = 0.6; // all sides blocked
                            } else {
                                prob = 0.4; // left and up but no corner
                            }
                        } else {
                            if (c != 0) {
                                prob = 0.3; // left and corner
                            } else {
                                prob = 0.2; // just left
                            }
                        }
                    } else {
                        if (b != 0) {
                            if (c != 0) {
                                prob = 0.3; // up and corner
                            } else {
                                prob = 0.2; // just up
                            }
                        } else {
                            if (c != 0) {
                                prob = 0.0; // just corner
                            } else {
                                prob = 0.1; // nothing
                            }
                        }
                    }

                    if (Math.random() < prob) {
                        map.set((byte)255, i, j);
                    } else {
                        map.set((byte)0, i, j);
                    }
                }
            }
        }

        return map;
    }
}
