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
            for (int j = 0; j < n; j++) {
                A[i + m][j] = -origA[i][j];
            }
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

    /**
     * Solves the Integer Linear Programming (ILP) problem using the Branch and Bound algorithm.
     * <p>
     * This method finds integer values for variables 'x' that minimize the objective function,
     * subject to the constraints defined by A and b.
     * <p>
     * It works by:
     * 1. Solving the "relaxed" Linear Programming (LP) problem (allowing fractional values).
     * 2. If the solution is fractional, it "branches" into two sub-problems by adding constraints
     *    to force the fractional variable to the nearest integers (floor and ceiling).
     * 3. It explores these branches using a priority queue (best-first search) to find the optimal integer solution.
     *
     * @param A   The constraint matrix (LHS).
     * @param b   The constraint vector (RHS).
     * @param obj The objective function coefficients.
     * @return The minimal objective value where all variables are integers, or 0 if infeasible.
     */
    private static double branchAndBound(double[][] A, double[] b, double[] obj) {
        // Priority queue stores active nodes, ordered by their lower bound (LP solution value).
        // We explore nodes with smaller lower bounds first (Best-First Search).
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.lowerBound));

        // Step 1: Solve the root relaxation (ignore integer constraints).
        SimplexSolver.Result res = SimplexSolver.solve(A, b, obj);
        if (!res.feasible()) {
            // If the relaxed problem is infeasible, the integer problem is too.
            throw new IllegalStateException("Unsolvable: If the relaxed problem is infeasible, the integer problem is too.");
            //return 0;
        }

        // Add the root node to the queue.
        queue.add(new Node(A, b, res.objValue(), res.x()));

        // Track the best (minimum) objective value found so far for a valid INTEGER solution.
        double bestObj = Double.POSITIVE_INFINITY;

        while (!queue.isEmpty()) {
            Node node = queue.poll();

            // Pruning: If the best possible value for this branch (lowerBound) is already worse
            // (or equal within epsilon) than the best integer solution found so far, discard this branch.
            if (node.lowerBound >= bestObj - EPSILON) {
                continue;
            }

            // Check if the current solution is integral (all variables are integers).
            int branchIdx = -1;
            for (int i = 0; i < node.x.length; i++) {
                // If x[i] is not an integer (e.g., 3.5), we need to branch on it.
                if (Math.abs(node.x[i] - Math.rint(node.x[i])) > EPSILON) {
                    branchIdx = i;
                    break;
                }
            }

            // If no fractional variables were found, this is a valid integer solution.
            if (branchIdx == -1) {
                bestObj = Math.min(bestObj, node.lowerBound);
                continue;
            }

            // Branching: Create two sub-problems to exclude the fractional value.
            // Example: If x[i] = 3.5, we branch into:
            // 1. x[i] <= 3.0 (Floor)
            // 2. x[i] >= 4.0 (Ceiling)
            addBranch(queue, node, branchIdx, Math.floor(node.x[branchIdx]), true, obj, bestObj);
            addBranch(queue, node, branchIdx, Math.ceil(node.x[branchIdx]), false, obj, bestObj);
        }
        if (bestObj == Double.POSITIVE_INFINITY) {
            throw new IllegalStateException("Unsolvable: No integer solution found.");
            //return 0;
        }
        return bestObj;
    }

    /**
     * Helper method to create a new branch (sub-problem) with an added constraint.
     *
     * @param q      The priority queue to add the new node to.
     * @param parent The parent node containing current constraints.
     * @param idx    The index of the variable to constrain.
     * @param val    The value for the constraint boundary.
     * @param leq    If true, adds constraint x[idx] <= val. If false, adds x[idx] >= val.
     * @param obj    The objective function.
     * @param best   The current best integer solution (used for pruning).
     */
    private static void addBranch(PriorityQueue<Node> q, Node parent, int idx, double val, boolean leq, double[] obj, double best) {
        int m = parent.A.length;
        int n = parent.A[0].length;

        // Create new constraint system with one extra row.
        double[][] nextA = new double[m + 1][n];
        double[] nextB = new double[m + 1];

        // Copy existing constraints.
        for (int i = 0; i < m; i++) {
            System.arraycopy(parent.A[i], 0, nextA[i], 0, n);
            nextB[i] = parent.b[i];
        }

        // Add the new constraint.
        // Simplex expects Ax <= b.
        // If leq (<=), we add:  1.0 * x[idx] <= val
        // If !leq (>=), we add: -1.0 * x[idx] <= -val  (equivalent to x[idx] >= val)
        nextA[m][idx] = leq ? 1.0 : -1.0;
        nextB[m] = leq ? val : -val;

        // Solve the new sub-problem.
        SimplexSolver.Result res = SimplexSolver.solve(nextA, nextB, obj);

        // Only add to queue if feasible and potentially better than the current best integer solution.
        if (res.feasible() && res.objValue() < best - EPSILON) {
            q.add(new Node(nextA, nextB, res.objValue(), res.x()));
        }
    }

    /**
     * Represents a node in the Branch and Bound search tree.
     *
     * @param A          The constraint matrix for this node.
     * @param b          The constraint vector for this node.
     * @param lowerBound The optimal objective value for the relaxed problem at this node.
     * @param x          The solution vector (values of variables) for the relaxed problem.
     */
    private record Node(double[][] A, double[] b, double lowerBound, double[] x) {
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day10Part2Simplex.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }

}