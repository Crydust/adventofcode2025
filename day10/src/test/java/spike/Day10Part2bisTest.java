package spike;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static spike.Day10Part2bis.*;

class Day10Part2bisTest {

    @Test
    void shouldCreateMatrixA() {
        Machine machine = Machine.parse("[.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}");
        double[][] matrixA = createMatrixA(machine);
        double[][] expected = {
                {0, 0, 0, 0, 1, 1},
                {0, 1, 0, 0, 0, 1},
                {0, 0, 1, 1, 1, 0},
                {1, 1, 0, 1, 0, 0},
        };
        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expected[i].length; j++) {
                assertEquals(expected[i][j], matrixA[i][j]);
            }
        }
    }

    @Test
    void shouldCreateMatrixB() {
        Machine machine = Machine.parse("[.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}");
        double[] matrixB = createMatrixB(machine);
        double[] expected = {3, 5, 4, 7};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], matrixB[i]);
        }
    }

    @Test
    void shouldCheckATimesXIsB() {
        double[][] matrixA = {
                {0, 0, 0, 0, 1, 1},
                {0, 1, 0, 0, 0, 1},
                {0, 0, 1, 1, 1, 0},
                {1, 1, 0, 1, 0, 0},
        };
        double[] matrixB = {3, 5, 4, 7};
        double[] matrixX = {
                1, 3, 0, 3, 1, 2.0
        };
        assertTrue(aTimesXIsB(matrixA, matrixX, matrixB));
    }

    @Test
    void shouldSolveForX() {
        double[][] matrixA = {
                {0, 0, 0, 0, 1, 1},
                {0, 1, 0, 0, 0, 1},
                {0, 0, 1, 1, 1, 0},
                {1, 1, 0, 1, 0, 0},
        };
        double[] matrixB = {3, 5, 4, 7};
        double[] expected = {
                1, 3, 0, 3, 1, 2.0
        };
        double[] matrixX = solveForX(matrixA, matrixB);
        assertAll(
                () -> assertEquals(expected[0], matrixX[0], "expected[" + 0 + "]"),
                () -> assertEquals(expected[1], matrixX[1], "expected[" + 1 + "]"),
                () -> assertEquals(expected[2], matrixX[2], "expected[" + 2 + "]"),
                () -> assertEquals(expected[3], matrixX[3], "expected[" + 3 + "]"),
                () -> assertEquals(expected[4], matrixX[4], "expected[" + 4 + "]"),
                () -> assertEquals(expected[5], matrixX[5], "expected[" + 5 + "]")
        );
    }
}