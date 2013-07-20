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

package robotutils.examples;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import robotutils.data.Coordinate;
import robotutils.data.GridMapGenerator;
import robotutils.data.GridMapUtils;
import robotutils.data.IntCoord;
import robotutils.data.StaticMap;
import robotutils.gui.MapPanel;
import robotutils.planning.GridAStar;

/**
 * Creates a randomized 2D grid map and solves a path between two random
 * locations using A-Star search.
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class GridAStarPlanning {
    public static Random rnd = new Random();
    public static Shape dot = new Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0);
    public static Stroke dotStroke = new BasicStroke(0.2f);

    public static void main(String args[]) {
        
        // Generate a random blocky map (using cellular automata rules)
        StaticMap sm = GridMapGenerator.createRandomMazeMap2D(100, 100);
        Rectangle2D mapBounds = new Rectangle2D.Double(0.0, 0.0, sm.size(0), sm.size(1));

        // Create a display panel to draw the results
        MapPanel mp = new MapPanel();
        mp.setIcon("map", GridMapUtils.toImage(sm), mapBounds);

        JFrame jf = new JFrame("Map");
        jf.setBounds(10, 10, 600, 610);
        jf.getContentPane().add(mp);
        jf.setVisible(true);

        mp.setView(mapBounds);

        // Find an unoccupied start location
        int[] start = new int[sm.dims()];
        while (sm.get(start) < 0) {
            for (int i = 0; i < sm.dims(); i++) {
                start[i] = rnd.nextInt(sm.size(i));
            }
        }

        // Find an unoccupied goal location (that isn't the same as the start)
        int[] goal = new int[sm.dims()];
        while (sm.get(goal) < 0 || Arrays.equals(start, goal)) {
            for (int i = 0; i < sm.dims(); i++) {
                goal[i] = rnd.nextInt(sm.size(i));
            }
        }

        // Print and display start and goal locations
        System.out.println("Picked endpoints: " + Arrays.toString(start) + "->" + Arrays.toString(goal));
        mp.setShape("Start", dot, AffineTransform.getTranslateInstance(
                (double)start[0] + 0.5, (double)start[1] + 0.5), Color.GREEN, dotStroke);
        mp.setShape("Goal", dot, AffineTransform.getTranslateInstance(
                (double)goal[0] + 0.5, (double)goal[1] + 0.5), Color.RED, dotStroke);

        // Perform A* search
        GridAStar astar = new GridAStar(sm);
        List<? extends Coordinate> path = astar.search(new IntCoord(start), new IntCoord(goal));

        // Print and display resulting lowest cost path
        if (path.isEmpty()) {
            System.out.println("No path found!");
        } else {
            System.out.println("Solution path: " + path);

            for (int i = 1; i < path.size() - 1; i++) {
                Coordinate c = path.get(i);
                mp.setShape("p" + i, dot, AffineTransform.getTranslateInstance(
                    c.get(0) + 0.5, c.get(1) + 0.5), Color.CYAN, dotStroke);
            }
        }
    }
}
