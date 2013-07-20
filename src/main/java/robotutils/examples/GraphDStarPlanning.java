/*
 *  The MIT License
 * 
 *  Copyright 2010 Prasanna Velagapudi <psigen@gmail.com>.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package robotutils.examples;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import robotutils.data.CoordUtils;
import robotutils.data.Coordinate;
import robotutils.data.GridMapGenerator;
import robotutils.data.GridMapUtils;
import robotutils.data.StaticMap;
import robotutils.planning.GraphDStar;

/**
 * Creates a randomized small worlds graph and solves a path between two random
 * locations using D-Star search.  Note that a <i>significant</i> portion of
 * this code is boilerplate code for creating and displaying the graph.  The
 * actual use of the D* algorithm consists of only a handful of lines in the
 * middle.
 *
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class GraphDStarPlanning {
    public static Random rnd = new Random();
    public static final int GRAPH_SIZE = 30;

    public static void main(String args[]) {

        // Generate a random blocky map (using cellular automata rules)
        StaticMap sm = GridMapGenerator.createRandomMazeMap2D(GRAPH_SIZE, GRAPH_SIZE);
        Graph<Coordinate, DefaultWeightedEdge> g = GridMapUtils.toGraph(sm);
        Coordinate[] vertices = g.vertexSet().toArray(new Coordinate[0]);

        // Set up vertex and edge display properties for jgraph
        AttributeMap attrVertex = JGraphModelAdapter.createDefaultVertexAttributes();
        AttributeMap attrEdge = JGraphModelAdapter.createDefaultEdgeAttributes( g );
        GraphConstants.setLabelEnabled(attrEdge, false);
        GraphConstants.setLineColor(attrEdge, Color.BLUE);

        // Use jgraph to create a view of the graph
        JGraphModelAdapter jgAdapter = new JGraphModelAdapter( g, attrVertex, attrEdge );
        JGraph jgraph = new JGraph( jgAdapter );

        // Position the nodes at their (x,y) locations
        Map cellAttrs = new Hashtable();
        for (Coordinate c : vertices) {
            DefaultGraphCell cell = jgAdapter.getVertexCell( c );
            Map cellAttr = cell.getAttributes();
            GraphConstants.setBounds( cellAttr, new Rectangle2D.Double(c.get(0) * 60.0, c.get(1) * 60.0, 50.0, 50.0) );
            cellAttrs.put( cell, cellAttr );
        }
        jgAdapter.edit( cellAttrs, null, null, null );
        
        // Create a display panel to draw the results
        JFrame jf = new JFrame("Graph");
        jf.setBounds(10, 10, 810, 610);
        jf.getContentPane().add(new JScrollPane(jgraph));
        jf.setVisible(true);

        // Find a random goal location
        Coordinate goal;
        do {
            int startIdx = rnd.nextInt(vertices.length);
            goal = vertices[startIdx];
        } while (!g.containsVertex(goal));

        // Find a random start location (that isn't the same as the start)
        Coordinate start;
        do {
            int goalIdx = rnd.nextInt(vertices.length);
            start = vertices[goalIdx];
        } while (!g.containsVertex(start) || (goal == start));

        // Print and display start and goal locations
        System.out.println("Picked endpoints: " + goal + "->" + start);

        Map endPtAttrs = new Hashtable();
        {
            DefaultGraphCell startCell = jgAdapter.getVertexCell( goal );
            Map attrStart = startCell.getAttributes();
            GraphConstants.setBackground(attrStart, Color.GREEN);
            endPtAttrs.put( startCell, attrStart);

            DefaultGraphCell goalCell = jgAdapter.getVertexCell( start );
            Map attrGoal = goalCell.getAttributes();
            GraphConstants.setBackground(attrGoal, Color.RED);
            endPtAttrs.put( goalCell, attrGoal);
        }
        jgAdapter.edit( endPtAttrs, null, null, null );
        
        // Create an D* instantiation that uses the coordinates stored in each
        // vertex to compute Manhattan distance.
        GraphDStar<Coordinate, DefaultWeightedEdge> dstar =
                new GraphDStar<Coordinate, DefaultWeightedEdge>(g, goal, start){

            @Override
            protected double h(Coordinate a, Coordinate b) {
                return CoordUtils.mdist(a, b);
            }
        };

        // Perform D* search
        List<DefaultWeightedEdge> path = dstar.search();

        // Print and display resulting lowest cost path
        if (path.isEmpty()) {
            System.out.println("No path found!");
        } else {
            System.out.println("Solution path: " + path);

            // Display resulting path by marking the edges that were used
            Map pathAttrs = new Hashtable();
            for (DefaultWeightedEdge e : path) {
                DefaultGraphCell cell = jgAdapter.getEdgeCell( e );
                Map attr = cell.getAttributes();
                GraphConstants.setLineColor( attr, Color.MAGENTA );
                GraphConstants.setLineWidth( attr, 5.0f );
                cellAttrs.put( cell, attr );
            }
            jgAdapter.edit( pathAttrs, null, null, null );
        }
    }
}
