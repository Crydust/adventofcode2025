package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class Day10Part2bis {

    static void main() throws Exception {
        Stopwatch stopwatch = Stopwatch.start();
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");

        ProgressBar pb = ProgressBar.startProgressBar();

        int sum = lines.parallelStream()
                .map(Machine::parse)
                .peek(_ -> pb.incrementMax())
                .mapToInt(Day10Part2bis::determineMinimalButtonPresses)
                .peek(_ -> pb.incrementDone())
                .sum();

        pb.stop();

        System.out.println("sum = " + sum);
        System.out.println("totalTime = " + stopwatch.stop());
        // 16757
    }

    static int determineMinimalButtonPresses(Machine machine) {
        // Create matrix A
        double[][] a = createMatrixA(machine);
//        System.out.println("a = " + matrixToString(a));

        // Create matrix B
        double[][] b = createMatrixB(machine);
//        System.out.println("b = " + matrixToString(b));

        // Matrix X contains the solution
        double[] x = solveForX(a, b);
//        System.out.println("x = " + matrixToString(x));

        // the solution should contain only positive numbers (double might be very close to zero)
        // the solution should contain only integers (or almost integers)
        // the solution (converted to ints) should be correct (A*X=B)
        // rint is faster than round
        if (Arrays.stream(x).allMatch(it -> it >= -1 * 1e-11
                && Math.abs(it - Math.rint(it)) < 1e-11)
                && aTimesXIsB(a, x, b)) {
            return Arrays.stream(x).mapToInt(it -> (int) Math.rint(it)).sum();
        }

        System.out.println("ERROR: Could not solve machine " + machine);
        return 0;
    }

    private static String matrixToString(double[][] matrix) {
        return Arrays.stream(matrix)
                .map(row -> "  " + matrixToString(row))
                .collect(joining(",\n", "{\n", "\n}"));
    }

    private static String matrixToString(double[] matrix) {
        return Arrays.stream(matrix)
                .mapToObj(Double::toString)
                .collect(joining(", ", "{ ", " }"));
    }

    /**
     * Solves the linear system Ax = b for x, where x must be positive integers.
     * Uses Gaussian elimination with free variable enumeration.
     */
    static double[] solveForX(double[][] a, double[][] b) {
        int m = a.length;
        int n = a[0].length;
        double[][] augmented = createAugmentedMatrix(a, b);
        reduceToRREF(augmented);
        int[] freeVariables = identifyFreeVariables(augmented, n);
        return findOptimalIntegerSolution(augmented, freeVariables, n, m);
    }

    /**
     * Creates an augmented matrix [A | b] from matrices A and b.
     */
    static double[][] createAugmentedMatrix(double[][] a, double[][] b) {
        int m = a.length;
        int n = a[0].length;
        double[][] aug = new double[m][n + 1];
        for (int i = 0; i < m; i++) {
            System.arraycopy(a[i], 0, aug[i], 0, n);
            aug[i][n] = b[i][0];
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
            if (Math.abs(aug[i][column]) > 1e-11) {
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
                if (Math.abs(aug[i][j]) > 1e-11) {
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
            if (Math.abs(aug[row][j]) > 1e-11) {
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
            if (v < -1e-11 || Math.abs(v - Math.rint(v)) > 1e-11) {
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
    static class Solution {
        final long sum;
        final long sumSq;

        Solution(long sum, long sumSq) {
            this.sum = sum;
            this.sumSq = sumSq;
        }
    }

    static double[][] createMatrixA(Machine machine) {
        int buttonCount = machine.buttons().size();
        int joltageCount = machine.joltages().size();
        double[][] a = new double[joltageCount][buttonCount];
        // for each joltage
        for (int i = 0; i < joltageCount; i++) {
            a[i] = new double[buttonCount];
            Arrays.fill(a[i], 0);
            // for each button
            for (int j = 0; j < buttonCount; j++) {
                if (machine.buttons().get(j).get(i)) {
                    a[i][j] = 1;
                }
            }
        }
        return a;
    }

    static double[][] createMatrixB(Machine machine) {
        int joltageCount = machine.joltages().size();
        double[][] b = new double[joltageCount][1];
        for (int i = 0; i < joltageCount; i++) {
            b[i] = new double[]{machine.joltages().get(i)};
        }
        return b;
    }

    static boolean aTimesXIsB(double[][] a, double[] x, double[][] b) {
        for (int i = 0; i < a.length; i++) {
            int sum = 0;
            for (int j = 0; j < a[i].length; j++) {
                sum += ((int) Math.rint(x[j]) * (int) a[i][j]);
            }
            if (sum != b[i][0]) {
                return false;
            }
        }
        return true;
    }


    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day10Part2bis.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }

}
