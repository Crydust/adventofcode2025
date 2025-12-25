package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day10Part2Simplex {
    static final double EPSILON = 1e-11;

    static void main() throws Exception {
        Stopwatch stopwatch = Stopwatch.start();
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");

        ProgressBar pb = ProgressBar.startProgressBar();

        int sum = lines.parallelStream()
                .map(Machine::parse)
                .peek(_ -> pb.incrementMax())
                .mapToInt(Day10Part2Simplex::determineMinimalButtonPresses)
                .peek(_ -> pb.incrementDone())
                .sum();

        pb.stop();

        System.out.println("sum = " + sum);
        System.out.println("totalTime = " + stopwatch.stop());
        // 16757
    }

    static int determineMinimalButtonPresses(Machine machine) {
        double[][] origA = createMatrixA(machine);
        double[] origB = createMatrixB(machine);

        int n = origA[0].length;
        int m = origA.length;

        // Convert Ax = b to Ax <= b AND -Ax <= -b
        double[][] A = new double[2 * m][n];
        double[] b = new double[2 * m];
        for (int i = 0; i < m; i++) {
            System.arraycopy(origA[i], 0, A[i], 0, n);
            b[i] = origB[i];
            for (int j = 0; j < n; j++) A[i + m][j] = -origA[i][j];
            b[i + m] = -origB[i];
        }

        double[] obj = new double[n];
        Arrays.fill(obj, 1.0);

        return (int) Math.round(branchAndBound(A, b, obj));
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

    private static double branchAndBound(double[][] A, double[] b, double[] obj) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.lowerBound));
        SimplexSolver.Result res = SimplexSolver.solve(A, b, obj);
        if (!res.feasible()) return 0;

        queue.add(new Node(A, b, res.objValue(), res.x()));
        double bestObj = Double.POSITIVE_INFINITY;

        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (node.lowerBound >= bestObj - EPSILON) continue;

            int branchIdx = -1;
            for (int i = 0; i < node.x.length; i++) {
                if (Math.abs(node.x[i] - Math.rint(node.x[i])) > EPSILON) {
                    branchIdx = i;
                    break;
                }
            }

            if (branchIdx == -1) {
                bestObj = Math.min(bestObj, node.lowerBound);
                continue;
            }

            // Branch: x[i] <= floor(x[i]) and x[i] >= ceil(x[i])
            addBranch(queue, node, branchIdx, Math.floor(node.x[branchIdx]), true, obj, bestObj);
            addBranch(queue, node, branchIdx, Math.ceil(node.x[branchIdx]), false, obj, bestObj);
        }
        return bestObj == Double.POSITIVE_INFINITY ? 0 : bestObj;
    }

    private static void addBranch(PriorityQueue<Node> q, Node parent, int idx, double val, boolean leq, double[] obj, double best) {
        int m = parent.A.length;
        int n = parent.A[0].length;
        double[][] nextA = new double[m + 1][n];
        double[] nextB = new double[m + 1];
        for (int i = 0; i < m; i++) {
            System.arraycopy(parent.A[i], 0, nextA[i], 0, n);
            nextB[i] = parent.b[i];
        }
        nextA[m][idx] = leq ? 1.0 : -1.0;
        nextB[m] = leq ? val : -val;

        SimplexSolver.Result res = SimplexSolver.solve(nextA, nextB, obj);
        if (res.feasible() && res.objValue() < best - EPSILON) {
            q.add(new Node(nextA, nextB, res.objValue(), res.x()));
        }
    }

    private record Node(double[][] A, double[] b, double lowerBound, double[] x) {
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day10Part2Simplex.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }

}