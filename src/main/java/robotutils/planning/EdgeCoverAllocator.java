/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package robotutils.planning;

//import distsim.aaai.Model.*;
//import distsim.rescue.RescueMap.Door;
//import distsim.rescue.RescueMap.Room;
//import distsim.rescue.RescueScenario;
//import distsim.rescueworld.tremor.mdp.HungarianAlgorithm;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import robotutils.planning.BellmanFord;
//
///**
// * Contains multiple implementations of task allocation.
// *
// * @author pkv
// */
//public class EdgeCoverAllocator {
//    public static final class Allocation<Task> extends ArrayList<List<Task>> {}
//
//    // Copies a 2D array
//    private static double[][] arrayCopy(final double[][] src) {
//        double[][] dest = new double[src.length][];
//
//        for (int i = 0; i < src.length; i++) {
//            dest[i] = new double[src[i].length];
//            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
//        }
//
//        return dest;
//    }
//
//    // Prints a 2D array in block form
//    private static void printArray(final double[][] src) {
//        for (int i = 0; i < src.length; ++i)
//            System.out.println("\t" + Arrays.toString(src[i]));
//    }
//
//    // Prints a 2D array in block form
//    private static void printArray(final int[][] src) {
//        for (int i = 0; i < src.length; ++i)
//            System.out.println("\t" + Arrays.toString(src[i]));
//    }
//
//    // Optimal (minimal cost) allocation of tasks to robots
//    public static Allocation market(final RescueScenario problem) {
//
//        int n = problem.starts.size();
//        int m = problem.victims.size();
//
//        // Create a new allocation with empty task lists for each robot
//        Allocation allocation = new Allocation();
//        allocation.ensureCapacity(problem.starts.size());
//        for (int i = 0; i < problem.starts.size(); ++i)
//            allocation.add(new ArrayList());
//
//        // A smallest edge cover can be found in polynomial time by finding a
//        // maximum matching and extending it greedily so that all vertices are
//        // covered. So, this is how we will allocate tasks.
//
//        // Create a shortest-path (min-cost) solver for this problem
//        BellmanFord<Room> bf = new BellmanFord<Room>() {
//
//            @Override
//            protected Collection<Room> succ(Room s) {
//                List<Room> nbrs = new ArrayList(problem.map.outDegreeOf(s));
//                for (Door d : problem.map.outgoingEdgesOf(s))
//                    nbrs.add(problem.map.getEdgeTarget(d));
//                return nbrs;
//            }
//
//            @Override
//            protected double c(Room a, Room b) {
//                if (problem.map.containsEdge(a,b)) {
//                    return -problem.map.getEdge(a, b).value();
//                } else {
//                    return Double.POSITIVE_INFINITY;
//                }
//            }
//        };
//
//        // Find distances to all the victims, then use Hungarian algorithm
//        double[][] distances;
//        if (n >= m) { // more bots than tasks
//
//            // Rows: bots, Cols: tasks
//            distances = new double[n][m];
//            for (int j = 0; j < m; ++j) {
//                bf.searchFrom(problem.victims.get(j));
//                for (int i = 0; i < n; ++i) {
//                    distances[i][j] = bf.costTo(problem.starts.get(i).location);
//                }
//            }
//
//            // Compute assignments of bots -> tasks
//            int[][] matchings = HungarianAlgorithm.computeAssignments(arrayCopy(distances));
//
//            //System.out.println("MATCH: (bots -> tasks)");
//            //printArray(matchings);
//            //System.out.println("DISTS: ");
//            //printArray(distances);
//
//            // Put matches into allocation
//            for (int[] match : matchings)
//                if (match != null)
//                    allocation.get(match[0]).add(problem.victims.get(match[1]));
//
//        } else { // more tasks than bots
//
//            // Rows: tasks, Cols: bots
//            distances = new double[m][n];
//            for (int i = 0; i < n; ++i) {
//                bf.searchFrom(problem.starts.get(i).location);
//                for (int j = 0; j < m; ++j) {
//                    distances[j][i] = bf.costTo(problem.victims.get(j));
//                }
//            }
//
//            // Compute assignments of tasks -> bots
//            int[][] matchings = HungarianAlgorithm.computeAssignments(arrayCopy(distances));
//
//            //System.out.println("MATCH: (tasks -> bots)");
//            //printArray(matchings);
//            //System.out.println("DISTS: ");
//            //printArray(distances);
//
//            // Put matches into allocation
//            for (int[] match : matchings) {
//                if (match != null) {
//                    distances[match[0]] = null;
//                    allocation.get(match[1]).add(problem.victims.get(match[0]));
//                }
//            }
//
//            // Greedily allocate remaining tasks
//            for (int j = 0; j < m; ++j) {
//                if (distances[j] != null) {
//                    double minCost = Double.POSITIVE_INFINITY;
//                    int minRobot = -1;
//
//                    for (int i = 0; i < n; ++i) {
//                        if (distances[j][i] <= minCost) {
//                            minCost = distances[j][i];
//                            minRobot = i;
//                        }
//                    }
//
//                    allocation.get(minRobot).add(problem.victims.get(j));
//                }
//            }
//        }
//
//        return allocation;
//    }
//}
