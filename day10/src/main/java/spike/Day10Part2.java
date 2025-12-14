package spike;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ParallelPortfolio;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.linear.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/// | USE_DECOMPOSITION_SOLVER | USE_PARALLEL_PORTFOLIO | Total time |
/// | ------------------------ | ---------------------- | ---------: |
/// | false                    | false                  |    169.571 |
/// | true                     | false                  |    164.021 |
/// | false                    | true                   |    319.763 |
/// | true                     | true                   |    262.883 |
///
/// true, false, hybrid=0b00, 5.0.0-beta.1 = 169.744
/// true, false, hybrid=0b01, 5.0.0-beta.1 = 145.607
/// true, false, hybrid=0b10, 5.0.0-beta.1 = 146.567
public class Day10Part2 {

    // The DecompositionSolver finds the exact solution for 37 of the machines and it reduces the search range of 77 more.
    // This yields a dismal 1% performance improvement.
    private static final boolean USE_DECOMPOSITION_SOLVER = true;

    // it seemed like a good idea to use ParallelPortfolio, but unfortunately, it isn't faster
    private static final boolean USE_PARALLEL_PORTFOLIO = false;

    // use either of these for a 15% speedboost
    private static final boolean USE_HYBRID_01 = true;
    private static final boolean USE_HYBRID_10 = false;

    private static final Pattern MACHINE_JOLTAGE_PATTERN = Pattern.compile("\\{([0-9,]+)}");
    private static final Pattern MACHINE_BUTTON_PATTERN = Pattern.compile("\\(([0-9,]+)\\)");

    static void main() throws Exception {
        Stopwatch stopwatch = Stopwatch.start();
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");

        // using parallelStream is a no-brainer and speeds things up a lot
        int sum = lines.parallelStream()
                .map(Day10Part2::parseMachine)
                .mapToInt(Day10Part2::determineMinimalButtonPresses)
                .sum();

        System.out.println(stopwatch.stop());
        IO.println("sum = " + sum);
        // 16757
    }

    static int determineMinimalButtonPresses(Machine machine) {
        return solveWithConstraintSolver(machine, tryToSolveWithDecompositionSolver(machine));
    }

    //region Decomposition Solver
    static DecomposeResult tryToSolveWithDecompositionSolver(Machine machine) {

        // This is fast but will only solve 17 of the machines
        int buttonCount = machine.buttons.size();
        int joltageCount = machine.joltages.size();
        boolean isSquareMatrix = joltageCount + 1 == buttonCount;

        // Create A
        double[][] a = createMatrixA(machine, joltageCount, buttonCount);

        // Create B
        double[] b = createMatrixB(machine, joltageCount);

        // Solve matrix
        int lowerBound = machine.joltages.stream().mapToInt(it -> it).max().orElseThrow();
        int upperBound = machine.joltages.stream().mapToInt(it -> it).sum();
        RangeResult fallbackResult = new RangeResult(lowerBound, upperBound);

        if (!USE_DECOMPOSITION_SOLVER) {
            return fallbackResult;
        }

        // using other solvers than LUDecomposition yields unreliable results
        DecompositionSolver solver = null;
        if (isSquareMatrix) {
            solver = new LUDecomposition(new Array2DRowRealMatrix(a)).getSolver();
        }
        if (solver == null) {
            solver = new SingularValueDecomposition(new Array2DRowRealMatrix(a)).getSolver();
        }
        for (int n = lowerBound; n <= upperBound; n++) {
            b[joltageCount] = n;
            double[] solution;
            try {
                solution = solver.solve(new ArrayRealVector(b)).toArray();
            } catch (MathIllegalArgumentException ignored) {
                continue;
            }
            // the solution should contain only positive numbers (double might be very close to zero)
            // the solution should contain only integers (or almost integers)
            // the solution (converted to ints) should be correct (A*X=B)
            // rint is faster than round
            if (Arrays.stream(solution).allMatch(it -> it >= -1 * 1e-11
                    && Math.abs(it - Math.rint(it)) < 1e-11)
                    && aTimesXIsB(a, solution, b)) {
                if (isSquareMatrix || n == lowerBound) {
                    return new ExactResult(n);
                } else {
                    return new RangeResult(lowerBound, n);
                }
            }

        }
        return fallbackResult;
    }

    private static double[][] createMatrixA(Machine machine, int joltageCount, int buttonCount) {
        double[][] a = new double[joltageCount + 1][buttonCount];
        // for each joltage
        for (int i = 0; i < joltageCount; i++) {
            a[i] = new double[buttonCount];
            Arrays.fill(a[i], 0);
            // for each button
            for (int j = 0; j < buttonCount; j++) {
                if (machine.buttons.get(j).get(i)) {
                    a[i][j] = 1;
                }
            }
        }
        a[joltageCount] = new double[buttonCount];
        Arrays.fill(a[joltageCount], 1);
        return a;
    }

    private static double[] createMatrixB(Machine machine, int joltageCount) {
        double[] b = new double[joltageCount + 1];
        for (int i = 0; i < joltageCount; i++) {
            b[i] = machine.joltages.get(i);
        }
        b[joltageCount] = -1; // we'll guess this
        return b;
    }

    private static boolean aTimesXIsB(double[][] a, double[] solution, double[] b) {
        int[] x = Arrays.stream(solution)
                .mapToInt(it -> (int) Math.rint(it))
                .toArray();
        for (int i = 0; i < a.length; i++) {
            int sum = 0;
            for (int j = 0; j < a[i].length; j++) {
                sum += (x[j] * (int) a[i][j]);
            }
            if (sum != b[i]) {
                return false;
            }
        }
        return true;
    }

    private interface DecomposeResult {
    }

    private record ExactResult(int exact) implements DecomposeResult {
    }

    private record RangeResult(int lowerBound, int upperBound) implements DecomposeResult {
    }
    //endregion

    //region Constraint Solver
    private static int solveWithConstraintSolver(Machine machine, DecomposeResult decomposeResult) {
        if (decomposeResult instanceof ExactResult(int exact)) {
            return exact;
        }
        if (USE_PARALLEL_PORTFOLIO) {
            int nbModels = 8;
            ParallelPortfolio portfolio = new ParallelPortfolio(true);
            portfolio.stealNogoodsOnRestarts();
            for (int i = 0; i < nbModels; i++) {
                portfolio.addModel(createModel(machine, decomposeResult), true);
            }
            while (portfolio.solve()) {
                // NOOP
            }
            return portfolio.getBestModel().getSolver().getBestSolutionValue().intValue();
        } else {
            var model = createModel(machine, decomposeResult);
            Solver solver = model.getSolver();
            while (solver.solve()) {
                // NOOP
            }
            return solver.getBestSolutionValue().intValue();
        }
    }

    private static Model createModel(Machine machine, DecomposeResult decomposeResult) {
        Settings settings = Settings.prod();
        if (USE_HYBRID_01) {
            settings = settings.setHybridizationOfPropagationEngine((byte) 0b01);
        }
        if (USE_HYBRID_10) {
            settings = settings.setHybridizationOfPropagationEngine((byte) 0b10);
        }
        Model model = new Model("model", settings);
        int buttonCount = machine.buttons.size();
        // create a variable for each buttonpresscount
        IntVar[] as = new IntVar[buttonCount];
        for (int i = 0; i < buttonCount; i++) {
            BitSet button = machine.buttons.get(i);
            int miniumumValue = 0;
            int maximumValue = Integer.MAX_VALUE;
            for (int j = 0; j < machine.joltages.size(); j++) {
                int joltage = machine.joltages.get(j);
                if (button.get(j)) {
                    maximumValue = Math.min(maximumValue, joltage);
                }
            }
            as[i] = model.intVar("btn_" + i, miniumumValue, maximumValue, true);
        }
        // buttonpresses should sum to the joltage
        for (int i = 0; i < machine.joltages.size(); i++) {
            int joltage = machine.joltages.get(i);
            ArrayList<IntVar> intVars = new ArrayList<>();
            for (int j = 0; j < machine.buttons.size(); j++) {
                BitSet button = machine.buttons.get(j);
                if (button.get(i)) {
                    intVars.add(as[j]);
                }
            }
            model.sum(intVars.toArray(IntVar[]::new), "=", joltage).post();
        }
        // minimize total buttonpresses
        IntVar totalButtonPresses = model.sum("totalButtonPresses", as);
        if (decomposeResult instanceof RangeResult(int lowerBound, int upperBound)) {
            model.arithm(totalButtonPresses, ">=", lowerBound).post();
            model.arithm(totalButtonPresses, "<=", upperBound).post();
        }
        model.setObjective(Model.MINIMIZE, totalButtonPresses);
        return model;
    }
    //endregion

    //region Reading and parsing
    static Machine parseMachine(String line) {
        List<Integer> joltages = parseJoltages(line);
        return new Machine(line, joltages, parseButtons(line, joltages.size()));
    }

    private static List<Integer> parseJoltages(String line) {
        List<Integer> joltages;
        Matcher m = MACHINE_JOLTAGE_PATTERN.matcher(line);
        if (!m.find()) {
            throw new IllegalArgumentException("No joltages found in " + line);
        }
        String group = m.group(1);
        joltages = new ArrayList<>();
        Arrays.stream(group.split(","))
                .mapToInt(Integer::parseInt)
                .forEach(joltages::add);
        return joltages;
    }

    private static List<BitSet> parseButtons(String line, int lightCount) {
        List<BitSet> buttons = new ArrayList<>();
        Matcher m = MACHINE_BUTTON_PATTERN.matcher(line);
        while (m.find()) {
            String group = m.group(1);
            BitSet button = new BitSet(lightCount);
            Arrays.stream(group.split(","))
                    .mapToInt(Integer::parseInt)
                    .forEach(button::set);
            buttons.add(button);
        }
        return buttons;
    }

    record Machine(String line, List<Integer> joltages, List<BitSet> buttons) {
        @Override
        public String toString() {
            return line;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day10Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
    //endregion

}
