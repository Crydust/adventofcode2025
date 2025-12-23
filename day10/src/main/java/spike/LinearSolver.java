package spike;

import java.util.Arrays;

public class LinearSolver {

    private static final double EPSILON = 1e-11;

    /**
     * Creates an augmented matrix [A | b] from matrix A and vector b.
     */
    static double[][] createAugmentedMatrix(double[][] a, double[] b) {
        int m = a.length;
        int n = a[0].length;
        double[][] aug = new double[m][n + 1];
        for (int i = 0; i < m; i++) {
            System.arraycopy(a[i], 0, aug[i], 0, n);
            aug[i][n] = b[i];
        }
        return aug;
    }

    /**
     * Reduces the augmented matrix to Reduced Row Echelon Form (RREF).
     */
    static void reduceToRREF(double[][] aug) {
        int m = aug.length;
        int n = aug[0].length - 1; // exclude augmented column
        int lead = 0;

        for (int r = 0; r < m && lead < n; r++) {
            // Find pivot
            int pivotRow = findPivotRow(aug, r, lead, m);
            if (pivotRow == -1) {
                lead++;
                r--;
                continue;
            }

            // Swap rows
            swapRows(aug, r, pivotRow);

            // Scale pivot row to have 1 at lead position
            scalePivotRow(aug, r, lead);

            // Eliminate all other entries in the lead column
            eliminateLeadColumn(aug, r, lead, m);

            lead++;
        }
    }

    /**
     * Finds the first row from startRow onwards with non-zero value at column.
     */
    static int findPivotRow(double[][] aug, int startRow, int column, int numRows) {
        for (int i = startRow; i < numRows; i++) {
            if (Math.abs(aug[i][column]) > EPSILON) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Swaps two rows in the matrix.
     */
    static void swapRows(double[][] aug, int row1, int row2) {
        double[] temp = aug[row1];
        aug[row1] = aug[row2];
        aug[row2] = temp;
    }

    /**
     * Scales a row so that the pivot element becomes 1.
     */
    static void scalePivotRow(double[][] aug, int row, int pivotCol) {
        double pivot = aug[row][pivotCol];
        for (int j = 0; j < aug[row].length; j++) {
            aug[row][j] /= pivot;
        }
    }

    /**
     * Eliminates all non-zero entries in the pivot column.
     */
    static void eliminateLeadColumn(double[][] aug, int pivotRow, int pivotCol, int numRows) {
        for (int i = 0; i < numRows; i++) {
            if (i != pivotRow) {
                double factor = aug[i][pivotCol];
                for (int j = 0; j < aug[i].length; j++) {
                    aug[i][j] -= factor * aug[pivotRow][j];
                }
            }
        }
    }

    /**
     * Identifies which variables are free (not pivot variables).
     * In RREF, a column is a pivot column if it has a leading 1 in some row.
     * We identify pivot columns by finding the first non-zero in each row.
     */
    static int[] identifyFreeVariables(double[][] aug, int numVars) {
        boolean[] isPivot = new boolean[numVars];

        // For each row, mark the first non-zero column as a pivot column
        for (int i = 0; i < aug.length; i++) {
            for (int j = 0; j < numVars; j++) {
                if (Math.abs(aug[i][j]) > EPSILON) {
                    isPivot[j] = true;
                    break; // Only mark the first non-zero as pivot for this row
                }
            }
        }

        int[] freeIdx = new int[numVars];
        int count = 0;
        for (int j = 0; j < numVars; j++) {
            if (!isPivot[j]) {
                freeIdx[count++] = j;
            }
        }
        return Arrays.copyOf(freeIdx, count);
    }

    /**
     * Finds the optimal integer solution by enumerating free variables.
     * Returns the solution with minimum sum (L1 norm), with ties broken by minimum sum of squares.
     */
    static double[] findOptimalIntegerSolution(double[][] aug, int[] freeVariables, int numVars, int numRows) {
        int maxTry = 150;
        double[] bestX = null;
        long minSum = Long.MAX_VALUE;
        long minSumSq = Long.MAX_VALUE;

        int[] freeVals = new int[freeVariables.length];
        boolean hasMoreValues = true;

        while (hasMoreValues) {
            double[] x = tryFreeVariableAssignment(aug, freeVariables, freeVals, numVars, numRows);

            if (isValidIntegerSolution(x)) {
                Solution sol = evaluateSolution(x);
                if (bestX == null || sol.sum < minSum || (sol.sum == minSum && sol.sumSq < minSumSq)) {
                    minSum = sol.sum;
                    minSumSq = sol.sumSq;
                    bestX = x;
                }
            }

            hasMoreValues = incrementFreeValues(freeVals, maxTry);
        }

        return bestX != null ? bestX : new double[numVars];
    }

    /**
     * Creates a solution vector by assigning free variables and back-substituting.
     */
    static double[] tryFreeVariableAssignment(double[][] aug, int[] freeVariables, int[] freeVals,
                                              int numVars, int numRows) {
        double[] x = new double[numVars];

        // Assign free variables
        for (int i = 0; i < freeVariables.length; i++) {
            x[freeVariables[i]] = freeVals[i];
        }

        // Back-substitute for dependent variables
        for (int i = numRows - 1; i >= 0; i--) {
            int pivot = findPivotInRow(aug, i, numVars);
            if (pivot == -1) continue;

            double sum = aug[i][numVars]; // augmented column
            for (int j = 0; j < numVars; j++) {
                if (j != pivot) {
                    sum -= aug[i][j] * x[j];
                }
            }
            x[pivot] = sum;
        }

        return x;
    }

    /**
     * Finds the pivot (leading non-zero) in a row.
     */
    static int findPivotInRow(double[][] aug, int row, int numVars) {
        for (int j = 0; j < numVars; j++) {
            if (Math.abs(aug[row][j]) > EPSILON) {
                return j;
            }
        }
        return -1;
    }

    /**
     * Checks if all values in the solution are non-negative integers.
     */
    static boolean isValidIntegerSolution(double[] x) {
        for (double v : x) {
            if (v < -EPSILON || Math.abs(v - Math.rint(v)) > EPSILON) {
                return false;
            }
        }
        return true;
    }

    /**
     * Evaluates a solution by computing its sum (L1 norm) and sum of squares (L2 norm).
     */
    static Solution evaluateSolution(double[] x) {
        long sum = 0;
        long sumSq = 0;
        for (double v : x) {
            long val = Math.round(v);
            sum += val;
            sumSq += val * val;
        }
        return new Solution(sum, sumSq);
    }

    /**
     * Increments the free variable counter and returns true if there are more values to try.
     */
    static boolean incrementFreeValues(int[] values, int maxValue) {
        for (int i = 0; i < values.length; i++) {
            values[i]++;
            if (values[i] < maxValue) {
                return true;
            }
            values[i] = 0;
        }
        return false;
    }

    /**
     * Helper record to store solution metrics for comparison.
     */
    record Solution(long sum, long sumSq) {
    }

}
