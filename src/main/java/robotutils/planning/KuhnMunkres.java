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

package robotutils.planning;

/**
 * The Hungarian method is a combinatorial optimization algorithm which solves
 * the assignment problem in polynomial time. It takes a matrix of non-negative
 * numbers defining either the costs or values for a set of executors to
 * complete a set of jobs.
 * 
 * It was developed and published by Harold Kuhn in 1955, who gave the name
 * "Hungarian method" because the algorithm was largely based on the earlier
 * works of two Hungarian mathematicians: Dénes Kőnig and Jenő Egerváry. James
 * Munkres reviewed the algorithm in 1957 and observed that it is (strongly)
 * polynomial. Since then the algorithm has been known also as Kuhn-Munkres
 * algorithm or Munkres assignment algorithm.
 *
 * Source: http://en.wikipedia.org/wiki/Hungarian_algorithm
 * Source: http://www.ams.jhu.edu/~castello/362/Handouts/hungarian.pdf
 *
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class KuhnMunkres {

    /**
     * Takes in a matrix of non-negative values where the row indices represent
     * possible <b>executors</b> of the task and the column indices represent
     * possible <b>tasks</b>.  The returned matrix contains a 1 for entries
     * corresponding to an assignment of executors to tasks.
     *
     * Each executor will be assigned up to one task. The algorithm attempts
     * to maximize the total value of the assignment.
     *
     * @param values a non-negative matrix of values.
     * @return an indicator matrix of assignments from executors to tasks.
     */
    public static int[][] computeValueAssignment(double[][] valuesRaw) {

        int numRows = valuesRaw.length;
        if (numRows <= 0)
            throw new IllegalArgumentException("Matrix has zero rows.");

        int numColumns = valuesRaw[0].length;
        if (numColumns <= 0)
            throw new IllegalArgumentException("Matrix has zero columns");

        // Pad until the matrix is square
        int numPairs = Math.max(numRows, numColumns);
        double[][] values = new double[numPairs][numPairs];
        for (int row = 0; row < numRows; ++row)
            System.arraycopy(valuesRaw[row], 0, values[row], 0, numColumns);
        for (int row = numRows; row < numPairs; ++row)
            values[row] = new double[numPairs];

        // Step 1: From each row subtract oﬀ the row min.
        for (int row = 0; row < numPairs; ++row) {
            double rowMin = Double.MIN_VALUE;
            for (int col = 0; col < numPairs; ++col)
                rowMin = Math.min(rowMin, values[row][col]);
            for (int col = 0; col < numPairs; ++col)
                values[row][col] -= rowMin;
        }

        // Step 2: From each column subtract off the column min.
        for (int col = 0; col < numPairs; ++col) {
            double colMin = Double.MIN_VALUE;
            for (int row = 0; row < numPairs; ++row)
                colMin = Math.min(colMin, values[row][col]);
            for (int row = 0; row < numPairs; ++row)
                values[row][col] -= colMin;
        }

        // Step 3: Use as few lines as possible to cover all the zeros in the 
        // matrix. There is no easy rule to do this – basically trial and error.
        // Suppose you use k lines.
        // • If k < n, let m be the minimum uncovered number. Subtract m from every
        // uncovered number. Add m to every number covered with two lines. Go back
        // to the start of step 3.
        // • If k = n, goto step 4.

        // Step 4: Starting with the top row, work your way downwards as you make assignments.
        // An assignment can be (uniquely) made when there is exactly one zero in a row. Once
        // an assignment it made, delete that row and column from the matrix.
        // If you cannot make all n assignments and all the remaining rows contain more than
        // one zero, switch to columns. Starting with the left column, work your way rightwards
        // as you make assignments.
        // Iterate between row assignments and column assignments until you’ve made as many
        // unique assignments as possible. If still haven’t made n assignments and you cannot
        // make a unique assignment either with rows or columns, make one arbitrarily by
        // selecting a cell with a zero in it. Then try to make unique row and/or column
        // assignments. (See the examples below).

        // TODO; finish the Kuhn-Munkres algorithm
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Takes in a matrix of non-negative costs where the row indices represent
     * possible <b>executors</b> of the task and the column indices represent
     * possible <b>tasks</b>.  The returned matrix contains a 1 for entries
     * corresponding to an assignment of executors to tasks.
     *
     * Each executor will be assigned up to one task.  The algorithm attempts
     * to minimize the total cost of the assignment.
     *
     * @param costs a non-negative matrix of costs.
     * @return an indicator matrix of assignments from executors to tasks.
     */
    public static int[][] computeCostAssignment(double[][] costs) {

        // Verify the matrix has reasonable size
        int numRows = costs.length;
        if (numRows <= 0)
            throw new IllegalArgumentException("Matrix has zero rows.");

        int numColumns = costs[0].length;
        if (numColumns <= 0)
            throw new IllegalArgumentException("Matrix has zero columns");
        
        // Find the largest number in the entire matrix
        double maxValue = Double.MIN_VALUE;
        for (double[] row : costs) {
            for (double val : row) {
                if (maxValue < val)
                    maxValue = val;
            }
        }

        // Subtract the value of the largest element from each cost, to get
        // positive "value" (which is what will be maximized)
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numColumns; ++col) {
                costs[row][col] = maxValue - costs[row][col];
            }
        }

        // Find the solution for the value-maximization problem
        return computeValueAssignment(costs);
    }

    /**
     * Takes in a matrix of non-negative values where the row indices represent
     * possible <b>executors</b> of the task and the column indices represent
     * possible <b>tasks</b>.  The returned matrix contains a 1 for entries
     * corresponding to an assignment of executors to tasks.
     *
     * Each executor will be assigned at least one task, but possibly more.
     * Every task will be assigned to exactly one executor. The algorithm
     * attempts to maximize the total value of the assignment by augmenting
     * the optimal matching greedily to compute a maximal edge cover.
     *
     * @param values a non-negative matrix of values.
     * @return an indicator matrix of assignments from executors to tasks.
     */
    public static int[][] computeValueMultiAssignment(double[][] values) {

        // Verify the matrix has reasonable size
        int numRows = values.length;
        if (numRows <= 0)
            throw new IllegalArgumentException("Matrix has zero rows.");

        int numColumns = values[0].length;
        if (numColumns <= numRows)
            throw new IllegalArgumentException("Matrix has zero columns");

        // Find initial matching using Hungarian algorithm
        int[][] assignment = computeValueAssignment(values);

        // Greedily allocate remaining tasks
        for (int col = 0; col < numColumns; ++col) {

            // Find the highest value executor for each task
            double maxValue = Double.MIN_VALUE;
            int maxRow = -1;
            boolean isAssigned = false;

            for (int row = 0; row < numRows && !isAssigned; ++row) {

                if (assignment[row][col] > 0)
                    isAssigned = true;

                if (values[row][col] >= maxValue) {
                    maxValue = values[row][col];
                    maxRow = row;
                }
            }

            // If a task is unallocated, assign it here
            if (!isAssigned)
                assignment[maxRow][col] = 1;
        }

        return assignment;
    }

    /**
     * Takes in a matrix of non-negative costs where the row indices represent
     * possible <b>executors</b> of the task and the column indices represent
     * possible <b>tasks</b>.  The returned matrix contains a 1 for entries
     * corresponding to an assignment of executors to tasks.
     *
     * Each executor will be assigned at least one task, but possibly more.
     * Every task will be assigned to exactly one executor. The algorithm
     * attempts to maximize the total value of the assignment by augmenting
     * the optimal matching greedily to compute a maximal edge cover.
     *
     * @param costs a non-negative matrix of costs.
     * @return an indicator matrix of assignments from executors to tasks.
     */
    public static int[][] computeCostMultiAssignment(double[][] costs) {

        // Verify the matrix has reasonable size
        int numRows = costs.length;
        if (numRows <= 0)
            throw new IllegalArgumentException("Matrix has zero rows.");

        int numColumns = costs[0].length;
        if (numColumns <= 0)
            throw new IllegalArgumentException("Matrix has zero columns");

        // Find the largest number in the entire matrix
        double maxValue = Double.MIN_VALUE;
        for (double[] row : costs) {
            for (double val : row) {
                if (maxValue < val)
                    maxValue = val;
            }
        }

        // Subtract the value of the largest element from each cost, to get
        // positive "value" (which is what will be maximized)
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numColumns; ++col) {
                costs[row][col] = maxValue - costs[row][col];
            }
        }

        // Find the solution for the value-maximization problem
        return computeValueMultiAssignment(costs);
    }
}