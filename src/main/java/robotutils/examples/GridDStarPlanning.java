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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JFrame;
import javax.swing.JLabel;
import robotutils.data.Coordinate;
import robotutils.data.GridMapGenerator;
import robotutils.data.GridMapUtils;
import robotutils.data.IntCoord;
import robotutils.data.StaticMap;
import robotutils.gui.MapPanel;
import robotutils.planning.GridDStar;

/**
 * Creates a randomized 2D map and solves a path between two random locations
 * using D* search.  Since D* is incremental, the GUI is configured to toggle
 * obstacles on left clicks and change the start location on a right click.
 * 
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class GridDStarPlanning {
    public static Random rnd = new Random();
    public static Shape dot = new RoundRectangle2D.Double(-0.25, -0.25, 0.5, 0.5, 0.25, 0.25);
    public static Stroke dotStroke = new BasicStroke(0.5f);
    public static AtomicBoolean needToReplan = new AtomicBoolean(true);
    private static int oldPathSize = 0;

    public static void main(String args[]) {

        // Generate a random blocky map (using cellular automata rules)
        final StaticMap sm = GridMapGenerator.createRandomMazeMap2D(100, 100);
        final Rectangle2D mapBounds = new Rectangle2D.Double(0.0, 0.0, sm.size(0), sm.size(1));

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

        // Initialize D* search
        final GridDStar dstar = new GridDStar(sm, new IntCoord(start), new IntCoord(goal));

        // Create a display panel to draw the results
        final MapPanel mp = new MapPanel() {
            @Override
            public final void onClick(double x, double y, int button, int numClicks) {
                // Find the map cell that was clicked
                int row = (int)x;
                int col = (int)y;

                // Ignore clicks outside the map
                if (row < 0 || row >= mapBounds.getWidth()
                        || col < 0 || col >= mapBounds.getHeight())
                    return;

                // Determine if click was left (BUTTON1) or right (BUTTON3)
                if (button == MouseEvent.BUTTON1) {
                    // When clicked, toggle a map obstacle
                    synchronized(dstar) {
                        if (sm.get(row, col) == 0) {
                            dstar.setCost(new IntCoord(row, col), (byte)255);
                        } else {
                            dstar.setCost(new IntCoord(row, col), (byte)0);
                        }
                    }

                    setIcon("map", GridMapUtils.toImage(sm), mapBounds);
                } else if (button == MouseEvent.BUTTON3) {
                    // When clicked, change the start location
                    synchronized(dstar) {
                        dstar.setStart(new IntCoord(row, col));
                    }

                    setShape("Start", dot, AffineTransform.getTranslateInstance(
                        (double)row + 0.5, (double)col + 0.5), Color.GREEN, dotStroke);
                }

                needToReplan.set(true);
            }
        };
        mp.setIcon("map", GridMapUtils.toImage(sm), mapBounds);
        mp.setPreferredSize(new Dimension(600,600));

        JFrame jf = new JFrame("Map");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().setLayout(new BorderLayout());
        jf.getContentPane().add(mp, BorderLayout.CENTER);
        jf.getContentPane().add(new JLabel(
                "<html>" +
                "<font color=red>(LEFT CLICK)</font> Toggle Obstacle / " +
                "<font color=red>(RIGHT-CLICK)</font> Change start location <p>" +
                "<font color=blue>(MOUSE DRAG)</font> Pan around map / " +
                "<font color=blue>(MOUSE WHEEL)</font> Zoom in/out of map" +
                "</html>"
                ), BorderLayout.NORTH);
        jf.pack();
        jf.setVisible(true);

        mp.setView(mapBounds);

        // Print and display start and goal locations
        System.out.println("Picked endpoints: " + Arrays.toString(start) + "->" + Arrays.toString(goal));
        mp.setShape("Start", dot, AffineTransform.getTranslateInstance(
                (double)start[0] + 0.5, (double)start[1] + 0.5), Color.GREEN, dotStroke);
        mp.setShape("Goal", dot, AffineTransform.getTranslateInstance(
                (double)goal[0] + 0.5, (double)goal[1] + 0.5), Color.RED, dotStroke);

        // Execute D* search FOREVER
        while(true) {
            if (needToReplan.getAndSet(false)) {
                synchronized(dstar) {
                    List<? extends Coordinate> path = dstar.plan();
                    drawPath(mp, path);
                }
            }
            Thread.yield();
        }
    }

    public static void drawPath(MapPanel mp, List<? extends Coordinate> path) {
        // Print and display resulting lowest cost path
        if (path.isEmpty()) {
            System.out.println("No path found!");

            for (int i = 1; i < oldPathSize - 1; i++)
                mp.removeShape("p" + i);

            oldPathSize = 0;
        } else {
            System.out.println("Solution path: " + path);

            for (int i = 1; i < path.size() - 1; i++) {
                Coordinate c = path.get(i);
                mp.setShape("p" + i, dot, AffineTransform.getTranslateInstance(
                    c.get(0) + 0.5, c.get(1) + 0.5), Color.CYAN, dotStroke);
            }

            for (int i = path.size() - 1; i < oldPathSize - 1; i++)
                mp.removeShape("p" + i);

            oldPathSize = path.size();
        }
    }
}
