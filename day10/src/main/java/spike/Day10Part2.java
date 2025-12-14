package spike;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.linear.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day10Part2 {

    private static final Pattern MACHINE_JOLTAGE_PATTERN = Pattern.compile("\\{([0-9,]+)}");
    private static final Pattern MACHINE_BUTTON_PATTERN = Pattern.compile("\\(([0-9,]+)\\)");

    static void main() throws Exception {
        Stopwatch stopwatch = Stopwatch.start();
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");
        // These are the difficult ones:
//        List<String> lines = List.of(
//                //  3402 ms
//                "[.##...#..#] (0,2,4,5,8) (0,2,3,7,9) (0,1,4,6,9) (0,1,2,6,9) (0,3,4,5,6,7,8,9) (0,4,6) (1,2,4,5,6,7,8,9) (2,8) (4,5,6,7) (0,2,3,4,6,7,8,9) (0,4,6,8,9) (0,1,2,4,5,6,7,9) (1,3,6,9) {66,46,51,33,71,36,73,26,47,59}",
//                //  5104 ms
//                "[#..#..#.#.] (2,3,7,8) (0,1) (0,1,2,4,5,9) (0,2,4,5,8,9) (0,1,2,5,6) (0,1,3,4,5,6,7,9) (1,3,6,7,8) (0,1,3,4,6,7,8) (0,2,3) (0,3,4,5,6,7,8,9) (2,3) (0,1,3,4,5,6,8,9) (4,5) {82,74,49,55,74,76,53,45,44,48}",
//                //  9704 ms
//                "[#..####.##] (3,4,5,6,7,9) (0,1,3,4,5,6,7,9) (0,1,2,4,5,7,8,9) (0,1,2,3,4,5,8,9) (0,1,3,7,8,9) (9) (2,3,4,5,8) (1,2,3,5,6,7,8) (1,3,4) (0,3,7,8) (0,1,3,6,7,8,9) (1,2) {81,84,57,116,82,86,50,87,84,80}",
//                // 17544 ms
//                "[##.##..##.] (1,8,9) (1,2,5) (0,8,9) (2,4,6,7,8) (1,2,6) (0,3,4,5,8) (0,2,3,4,5,6,7,9) (1,2,4,6,7,8,9) (0,2,3,4,5,6,7,8) (0,7) (1,2,3,4,5,6,7,8,9) (0,1,2,3,4,5,7,9) {67,79,100,65,95,79,73,93,103,83}",
//                // 32244 ms
//                "[##..#####] (0,2,3,4,5,7,8) (0,4) (1,2,5,6,8) (0,2,3,5,6,8) (0,1,2,3,4,5,7) (1,3,4,5,7,8) (0,1,3,4,5,6,7) (2,4,6,8) (1,2,3,4,5,6,7,8) (1,3,4,5,6,7) (1,7) {54,82,71,92,93,100,50,88,71}",
//                // 77726 ms (Solution: btn_0=7, btn_1=4, btn_2=205, btn_3=1, btn_4=13, btn_5=0, btn_6=16, btn_7=5, btn_8=9, btn_9=13, btn_10=25, totalButtonPresses=298)
//                "[#...#.##.] (0,1,2,3,5,7,8) (4,5,6,7) (0,1,5,6,7,8) (0,3,5,6,7,8) (0,2,3,5,7) (0,2,4,6,7,8) (0,1,3,4,7,8) (0,1,2,3,6) (0,1,6) (0,1,3,4,5,6,8) (0,2,3,4,6,7,8) {294,255,50,80,58,243,262,271,267}"
//        );

        int sum = lines.stream()
                .map(Day10Part2::parseMachine)
                .mapToInt(Day10Part2::determineMinimalButtonPressesAndLogSlowOnes)
                .sum();

        System.out.println(stopwatch.stop());
        IO.println("sum = " + sum);
        // Total time:  79546 ms (parallelStream)
        // Total time: 139259 ms (stream)
        // 16757
    }

    private static int determineMinimalButtonPressesAndLogSlowOnes(Machine machine) {
        Stopwatch stopwatch = Stopwatch.start();
        int result = determineMinimalButtonPresses(machine);
        stopwatch.stop();
        if (stopwatch.hasExceeded(Duration.ofSeconds(4))) {
//            System.out.println(stopwatch + " for machine " + machine);
        }
//        try {
//            Files.writeString(
//                    Path.of("./day10/solution.txt"),
//                    stopwatch + " " + machine + " <" + result + ">\n",
//                    UTF_8,
//                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
//        } catch (IOException ignored) {
//            System.out.println("ignored = " + ignored);
//        }
        return result;
    }

    private static int determineMinimalButtonPresses(Machine machine) {
        int simpleSolution = tryToSolveWithDecompositionSolver(machine);
        int realSolution = solveWithConstrainSolver(machine);

        if (simpleSolution != -1 && simpleSolution != realSolution) {
            System.out.println("complicated machine = " + machine);
            System.out.println(" simpleSolution = " + simpleSolution);
            System.out.println(" realSolution = " + realSolution);
        }
        return realSolution;
    }

    static int tryToSolveWithDecompositionSolver(Machine machine) {

        Stopwatch stopwatch = Stopwatch.start();

        // This is fast but will only solve 17 of the machines
        int buttonCount = machine.buttons.size();
        int joltageCount = machine.joltages.size();

        // Create A
        double[][] a = createMatrixA(machine, joltageCount, buttonCount);

        // Create B
        double[] b = createMatrixB(machine, joltageCount);

        // Solve matrix
        int firstGuess = machine.joltages.stream().mapToInt(it -> it).max().orElseThrow();
        int lastGuess = machine.joltages.stream().mapToInt(it -> it).sum();

        DecompositionSolver solver;
        try {
            // using other solvers than LUDecomposition yields unreliable results
            if (joltageCount + 1 == buttonCount) {
                solver = new LUDecomposition(new Array2DRowRealMatrix(a)).getSolver();
            } else {
                solver = new SingularValueDecomposition(new Array2DRowRealMatrix(a)).getSolver();
            }
        } catch (MathIllegalArgumentException e) {
            System.out.println("Could not create solver: " + e.getMessage());
            return -1;
        }
        for (int n = firstGuess; n <= lastGuess; n++) {
            if (stopwatch.hasExceeded(Duration.ofSeconds(1))) {
                System.out.println("Timeout");
                return -1;
            }
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
                return n;
            }
        }
        return -1;
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

    private static int solveWithConstrainSolver(Machine machine) {
        // Begin solving with Choco
        Model model = new Model();
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
        model.setObjective(Model.MINIMIZE, totalButtonPresses);
        // solve
        Solver solver = model.getSolver();
        Solution solution = solver.findOptimalSolution(totalButtonPresses, Model.MINIMIZE);
//        System.out.println("solution = " + solution);
        if (solution == null) {
            throw new IllegalStateException("No solution found! for machine " + machine);
        } else {
            return solution.getIntVal(totalButtonPresses);
        }
    }

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
}
