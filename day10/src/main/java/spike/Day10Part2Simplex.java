package spike;

import org.hipparchus.exception.MathIllegalStateException;
import org.hipparchus.optim.PointValuePair;
import org.hipparchus.optim.linear.*;
import org.hipparchus.optim.nonlinear.scalar.GoalType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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

        List<LinearConstraint> constraints = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            constraints.add(new LinearConstraint(origA[i], Relationship.EQ, origB[i]));
        }

        double[] objCoeffs = new double[n];
        Arrays.fill(objCoeffs, 1.0);
        LinearObjectiveFunction objective = new LinearObjectiveFunction(objCoeffs, 0);

        return (int) Math.round(branchAndBound(constraints, objective, n));
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
     * to force the fractional variable to the nearest integers (floor and ceiling).
     * 3. It explores these branches using a priority queue (best-first search) to find the optimal integer solution.
     *
     * @param initialConstraints The constraints we'll use for the relaxed problem.
     * @param objective          The objective function.
     * @return The minimal objective value where all variables are integers, or 0 if infeasible.
     */
    private static double branchAndBound(List<LinearConstraint> initialConstraints, LinearObjectiveFunction objective, int numVars) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.lowerBound));

        // Step 1: Solve the root relaxation (ignore integer constraints).
        PointValuePair res = solve(initialConstraints, objective);
        if (res == null) {
            // If the relaxed problem is infeasible, the integer problem is too.
            throw new IllegalStateException("Unsolvable: If the relaxed problem is infeasible, the integer problem is too.");
        }

        // Add the root node to the queue.
        queue.add(new Node(initialConstraints, res.getValue(), res.getPoint()));

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
            addBranch(queue, node, branchIdx, Math.floor(node.x[branchIdx]), true, objective, bestObj, numVars);
            addBranch(queue, node, branchIdx, Math.ceil(node.x[branchIdx]), false, objective, bestObj, numVars);
        }
        if (bestObj == Double.POSITIVE_INFINITY) {
            throw new IllegalStateException("Unsolvable: No integer solution found.");
        }
        return bestObj;
    }

    private static PointValuePair solve(Collection<LinearConstraint> constraints, LinearObjectiveFunction objective) {
        try {
            SimplexSolver solver = new SimplexSolver();
            return solver.optimize(
                    objective,
                    new LinearConstraintSet(constraints),
                    GoalType.MINIMIZE,
                    new NonNegativeConstraint(true) // x >= 0
            );
        } catch (MathIllegalStateException e) {
            return null;
        }
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
    private static void addBranch(PriorityQueue<Node> q, Node parent, int idx, double val, boolean leq,
                                  LinearObjectiveFunction obj, double best, int numVars) {

        // Create new constraint system with one extra row.
        // Copy existing constraints.
        List<LinearConstraint> nextConstraints = new ArrayList<>(parent.constraints);

        // Add the new constraint: x[idx] <= val OR x[idx] >= val
        double[] coeffs = new double[numVars];
        coeffs[idx] = 1.0;
        Relationship relationship = leq ? Relationship.LEQ : Relationship.GEQ;
        nextConstraints.add(new LinearConstraint(coeffs, relationship, val));

        // Solve the new sub-problem
        PointValuePair res = solve(nextConstraints, obj);

        // Only add to queue if feasible and potentially better than the current best integer solution.
        if (res != null && res.getValue() < best - EPSILON) {
            q.add(new Node(nextConstraints, res.getValue(), res.getPoint()));
        }
    }

    /**
     * Represents a node in the Branch and Bound search tree.
     *
     * @param constraints The constraints for this node.
     * @param lowerBound  The optimal objective value for the relaxed problem at this node.
     * @param x           The solution vector (values of variables) for the relaxed problem.
     */
    private record Node(List<LinearConstraint> constraints, double lowerBound, double[] x) {
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day10Part2Simplex.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
