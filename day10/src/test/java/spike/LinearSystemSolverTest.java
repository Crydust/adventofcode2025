package spike;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static spike.Day10Part2bis.*;

/**
 * Unit tests for the linear system solver helper methods.
 * Tests individual components of the Gaussian elimination and solution finding process.
 */
@DisplayName("Linear System Solver Tests")
class LinearSystemSolverTest {

    @Test
    @DisplayName("Create augmented matrix from A and b")
    void testCreateAugmentedMatrix() {
        double[][] a = {{1, 2}, {3, 4}};
        double[][] b = {{5}, {6}};

        double[][] result = createAugmentedMatrix(a, b);

        assertEquals(2, result.length, "Should have 2 rows");
        assertEquals(3, result[0].length, "Should have 3 columns (2 from A + 1 from b)");
        assertEquals(1, result[0][0], "A[0][0] should be preserved");
        assertEquals(2, result[0][1], "A[0][1] should be preserved");
        assertEquals(5, result[0][2], "b[0][0] should be in augmented column");
        assertEquals(3, result[1][0], "A[1][0] should be preserved");
        assertEquals(4, result[1][1], "A[1][1] should be preserved");
        assertEquals(6, result[1][2], "b[1][0] should be in augmented column");
    }

    @Test
    @DisplayName("Swap rows in matrix")
    void testSwapRows() {
        double[][] matrix = {{1, 2}, {3, 4}};

        swapRows(matrix, 0, 1);

        assertEquals(3, matrix[0][0], "Row 0 should now have values from original row 1");
        assertEquals(4, matrix[0][1]);
        assertEquals(1, matrix[1][0], "Row 1 should now have values from original row 0");
        assertEquals(2, matrix[1][1]);
    }

    @Test
    @DisplayName("Scale pivot row to have 1 at pivot position")
    void testScalePivotRow() {
        double[][] matrix = {{2, 4}, {6, 8}};

        scalePivotRow(matrix, 0, 0);

        assertEquals(1.0, matrix[0][0], 1e-9, "Pivot element should be 1");
        assertEquals(2.0, matrix[0][1], 1e-9, "Other elements should be scaled proportionally");
    }

    @Test
    @DisplayName("Eliminate lead column for all rows except pivot")
    void testEliminateLeadColumn() {
        double[][] matrix = {
                {1, 2, 3},
                {2, 4, 6},
                {1, 1, 1}
        };

        eliminateLeadColumn(matrix, 0, 0, 3);

        // The pivot row (row 0) should remain unchanged
        assertEquals(1.0, matrix[0][0], 1e-9, "Pivot row column 0 should remain 1");
        assertEquals(2.0, matrix[0][1], 1e-9, "Pivot row should not change");

        // Elements below pivot in column 0 should be eliminated
        assertEquals(0.0, matrix[1][0], 1e-9, "Element in pivot column should be eliminated from row 1");
        assertEquals(0.0, matrix[2][0], 1e-9, "Element in pivot column should be eliminated from row 2");

        // But the rest of the row should be modified accordingly
        // Row 1 (originally [2,4,6]): after elimination should be [0, 0, 0] (row 1 - 2*row 0)
        assertEquals(0.0, matrix[1][1], 1e-9, "Row 1 column 1 should be 0");
        assertEquals(0.0, matrix[1][2], 1e-9, "Row 1 column 2 should be 0");
    }

    @Test
    @DisplayName("Find pivot row returns correct index")
    void testFindPivotRow() {
        double[][] matrix = {
                {0, 0, 1},
                {0, 2, 3},
                {0, 0, 4}
        };

        int pivotRow = findPivotRow(matrix, 0, 1, 3);

        assertEquals(1, pivotRow, "Should find row with non-zero at column 1");
    }

    @Test
    @DisplayName("Find pivot row returns -1 when no pivot found")
    void testFindPivotRowNotFound() {
        double[][] matrix = {
                {0, 0, 1},
                {0, 0, 3},
                {0, 0, 4}
        };

        int pivotRow = findPivotRow(matrix, 0, 1, 3);

        assertEquals(-1, pivotRow, "Should return -1 when no pivot found");
    }

    @Test
    @DisplayName("Identify free variables correctly")
    void testIdentifyFreeVariables() {
        // Matrix in RREF with 6 variables, where columns 2, 4, 5 are free
        // Rows correspond to pivots at columns: 0, 1, 3 (columns with leading 1s in their rows)
        double[][] aug = {
                {1, 0, 0, 0, 0, 0, 5},  // pivot at column 0
                {0, 1, 0, 0, 0, 0, 3},  // pivot at column 1
                {0, 0, 0, 1, 0, 0, 4},  // pivot at column 3
                {0, 0, 0, 0, 0, 0, 0}   // zero row
        };

        int[] freeVars = identifyFreeVariables(aug, 6);

        assertEquals(3, freeVars.length, "Should have 3 free variables (columns 2, 4, 5)");
        assertTrue(Arrays.stream(freeVars).anyMatch(i -> i == 2), "Column 2 should be free");
        assertTrue(Arrays.stream(freeVars).anyMatch(i -> i == 4), "Column 4 should be free");
        assertTrue(Arrays.stream(freeVars).anyMatch(i -> i == 5), "Column 5 should be free");
    }

    @Test
    @DisplayName("Check valid integer solution")
    void testIsValidIntegerSolution() {
        double[] validSolution = {1.0, 2.0, 3.0};
        double[] invalidNegative = {-1.0, 2.0, 3.0};
        double[] invalidNonInteger = {1.5, 2.0, 3.0};

        assertTrue(isValidIntegerSolution(validSolution), "Should accept positive integers");
        assertFalse(isValidIntegerSolution(invalidNegative), "Should reject negative values");
        assertFalse(isValidIntegerSolution(invalidNonInteger), "Should reject non-integers");
    }

    @Test
    @DisplayName("Evaluate solution metrics (sum and sum of squares)")
    void testEvaluateSolution() {
        double[] x = {1.0, 2.0, 3.0};

        Solution sol = evaluateSolution(x);

        assertEquals(6, sol.sum, "Sum should be 1+2+3=6");
        assertEquals(14, sol.sumSq, "Sum of squares should be 1+4+9=14");
    }

    @Test
    @DisplayName("Increment free values with wrap-around")
    void testIncrementFreeValues() {
        int[] values = {0, 0};
        // With maxValue=3, we have 3 values per variable (0, 1, 2)
        // Total combinations: 3*3 = 9
        // Expected sequence: [0,0]?[1,0]?[2,0]?[0,1]?[1,1]?[2,1]?[0,2]?[1,2]?[2,2]?reset to [0,0]

        // Increment 1: [0,0] -> [1,0]
        assertTrue(incrementFreeValues(values, 3), "Should return true on increment 1");
        assertEquals(1, values[0]);
        assertEquals(0, values[1]);

        // Increment 2: [1,0] -> [2,0]
        assertTrue(incrementFreeValues(values, 3), "Should return true on increment 2");
        assertEquals(2, values[0]);
        assertEquals(0, values[1]);

        // Increment 3: [2,0] -> [0,1] (wrap around on i=0)
        assertTrue(incrementFreeValues(values, 3), "Should return true on increment 3");
        assertEquals(0, values[0]);
        assertEquals(1, values[1]);

        // Continue through remaining combinations
        for (int i = 0; i < 5; i++) {
            assertTrue(incrementFreeValues(values, 3), "Should continue returning true");
        }

        // After 9 total increments from [0,0], we should be at [2,2]
        // Now increment should wrap back to [0,0] and return false
        boolean result = incrementFreeValues(values, 3);
        assertFalse(result, "Should return false when wrapping around all values");
        assertEquals(0, values[0], "Should reset to 0");
        assertEquals(0, values[1], "Should reset to 0");
    }

    @Test
    @DisplayName("Find pivot in row")
    void testFindPivotInRow() {
        double[][] aug = {
                {0, 1, 0, 5},
                {1, 0, 0, 3}
        };

        int pivot0 = findPivotInRow(aug, 0, 3);
        int pivot1 = findPivotInRow(aug, 1, 3);

        assertEquals(1, pivot0, "Should find pivot at column 1 in row 0");
        assertEquals(0, pivot1, "Should find pivot at column 0 in row 1");
    }

    @Test
    @DisplayName("Try free variable assignment with back-substitution")
    void testTryFreeVariableAssignment() {
        // Simple 2x2 system in RREF: x0 + x1 = 3
        double[][] aug = {
                {1, 1, 3}
        };
        int[] freeVars = {1}; // x1 is free
        int[] freeVals = {2};  // Set x1 = 2

        double[] result = tryFreeVariableAssignment(aug, freeVars, freeVals, 2, 1);

        assertEquals(1.0, result[0], 1e-9, "x0 should be 1 (3 - 2)");
        assertEquals(2.0, result[1], 1e-9, "x1 should be 2 (free variable)");
    }

    @Test
    @DisplayName("RREF reduction")
    void testReduceToRREF() {
        // Use a proper 2x2 non-singular matrix for reliable testing
        double[][] aug = {
                {1, 2, 3},
                {0, 1, 2}
        };

        reduceToRREF(aug);

        // After RREF:
        // Row 0: should have leading 1 at column 0
        // Row 1: should have leading 1 at column 1
        // Column 0 should have [1, 0] pattern
        assertEquals(1.0, aug[0][0], 1e-9, "First pivot should be 1");
        assertEquals(0.0, aug[1][0], 1e-9, "Should have 0 below pivot");
        assertEquals(1.0, aug[1][1], 1e-9, "Second pivot should be 1");
    }

    @Test
    @DisplayName("Full solve for X with expected result")
    void testSolveForXComplete() {
        // System of equations:
        // x4 + x5 = 3
        // x1 + x5 = 5
        // x2 + x3 + x4 = 4
        // x0 + x1 + x3 = 7
        double[][] a = {
                {0, 0, 0, 0, 1, 1},
                {0, 1, 0, 0, 0, 1},
                {0, 0, 1, 1, 1, 0},
                {1, 1, 0, 1, 0, 0},
        };
        double[][] b = {
                {3},
                {5},
                {4},
                {7},
        };

        double[] result = solveForX(a, b);

        // Verify we got a valid solution
        assertNotNull(result, "Should find a solution");
        assertEquals(6, result.length, "Should have 6 variables");

        // All values should be non-negative
        assertTrue(Arrays.stream(result).allMatch(v -> v >= -1e-9),
                   "All values should be non-negative");

        // All values should be integers
        assertTrue(Arrays.stream(result).allMatch(v -> Math.abs(v - Math.rint(v)) < 1e-5),
                   "All values should be integers");

        // Verify the solution satisfies the equations
        double[] x = result;
        assertEquals(3.0, x[4] + x[5], 1e-9, "Equation 1: x4 + x5 = 3");
        assertEquals(5.0, x[1] + x[5], 1e-9, "Equation 2: x1 + x5 = 5");
        assertEquals(4.0, x[2] + x[3] + x[4], 1e-9, "Equation 3: x2 + x3 + x4 = 4");
        assertEquals(7.0, x[0] + x[1] + x[3], 1e-9, "Equation 4: x0 + x1 + x3 = 7");
    }
}

