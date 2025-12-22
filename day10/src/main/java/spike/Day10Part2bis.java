package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class Day10Part2bis {

    public static final double EPSILON = 1e-11;

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
        double[][] a = createMatrixA(machine);
        double[] b = createMatrixB(machine);
        double[] x = solveForX(a, b);

        if (isIntegerSolution(x)
                && aTimesXIsB(a, x, b)) {
            return Arrays.stream(x).mapToInt(it -> (int) Math.rint(it)).sum();
        }

        System.out.println("ERROR: Could not solve machine " + machine);
        return 0;
    }

    private static boolean isIntegerSolution(double[] x) {
        return Arrays.stream(x).allMatch(it -> it >= -1 * EPSILON
                && Math.abs(it - Math.rint(it)) < EPSILON);
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
    static double[] solveForX(double[][] a, double[] b) {
        int m = a.length;
        int n = a[0].length;
        double[][] augmented = LinearSolver.createAugmentedMatrix(a, b);
        LinearSolver.reduceToRREF(augmented);
        int[] freeVariables = LinearSolver.identifyFreeVariables(augmented, n);
        return LinearSolver.findOptimalIntegerSolution(augmented, freeVariables, n, m);
    }

    static double[][] createMatrixA(Machine machine) {
        int buttonCount = machine.buttons().size();
        int joltageCount = machine.joltages().size();
        double[][] a = new double[joltageCount][buttonCount];
        for (int i = 0; i < joltageCount; i++) {
            for (int j = 0; j < buttonCount; j++) {
                a[i][j] = machine.buttons().get(j).get(i) ? 1.0 : 0.0;
            }
        }
        return a;
    }

    static double[] createMatrixB(Machine machine) {
        int joltageCount = machine.joltages().size();
        double[] b = new double[joltageCount];
        for (int i = 0; i < joltageCount; i++) {
            b[i] = machine.joltages().get(i);
        }
        return b;
    }

    static boolean aTimesXIsB(double[][] a, double[] x, double[] b) {
        for (int i = 0; i < a.length; i++) {
            int sum = 0;
            for (int j = 0; j < a[i].length; j++) {
                sum += ((int) Math.rint(x[j]) * (int) a[i][j]);
            }
            if (sum != b[i]) {
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
