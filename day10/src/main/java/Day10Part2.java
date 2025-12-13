import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
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

public class Day10Part2 {

    private static final boolean USE_HIPPARCHUS = false;
//    private static final boolean USE_HIPPARCHUS = true;
//    private static final boolean USE_CHOCO = false;
    private static final boolean USE_CHOCO = true;

    private static final Pattern MACHINE_JOLTAGE_PATTERN = Pattern.compile("\\{([0-9,]+)}");
    private static final Pattern MACHINE_BUTTON_PATTERN = Pattern.compile("\\(([0-9,]+)\\)");
    private static final double NEARLY_ZERO = 1e-6;

    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");

        List<Machine> machines = lines.stream()
                .map(line -> {
                    List<Integer> joltages = parseJoltages(line);
                    List<BitSet> buttons = parseButtons(line, joltages.size());
                    return new Machine(line, joltages, buttons);
                })
                .toList();

        int sum = machines.parallelStream()
                .mapToInt(Day10Part2::determineMinimalButtonPresses)
                .sum();

        IO.println("sum = " + sum);
        // wrong: 11909
        // wrong: 15375
        // wrong: 16206
        // wrong: 16609
        // wrong: 16767
        // wrong: 16839
        // wrong: 16808
        // maybe: 16757 only choco
    }

    private static int determineMinimalButtonPresses(Machine machine) {
        double[][] a = createA(machine);
        double[] b = createB(machine);

        int i = tryToSolve(machine, a, b);

        if (i == 0) {
            IO.println("error: solution impossible for machine " + machine);
//            System.out.println("*** buttonCount = " + buttonCount);
//            System.out.println("*** joltageCount = " + joltageCount);
        } else {
//            IO.println("solved");
        }
        return i;
    }

    private static double[][] createA(Machine machine) {
        int buttonCount = machine.buttons.size();
        int joltageCount = machine.joltages.size();
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

    private static double[] createB(Machine machine) {
        int joltageCount = machine.joltages.size();
        double[] b = new double[joltageCount + 1];
        for (int i = 0; i < joltageCount; i++) {
            b[i] = machine.joltages.get(i);
        }
        b[joltageCount] = -1; // we'll guess this
        return b;
    }

    private static int tryToSolve(Machine machine, double[][] a, double[] b) {
        int result = 0;
        if (USE_HIPPARCHUS) {
            result = tryToSolveWithHipparchus(machine, a, b);
            if (result != 0) return result;
        }
        if (USE_CHOCO) {
            result = tryToSolveWithChoco(machine);
            if (result != 0) return result;
        }
        return result;
    }

    private static int tryToSolveWithHipparchus(Machine machine, double[][] a, double[] b) {
        RealMatrix coefficients = new Array2DRowRealMatrix(a);
        DecompositionSolver solver = new SingularValueDecomposition(coefficients).getSolver();
        int firstGuess = machine.joltages.stream().mapToInt(it -> it).max().orElseThrow();
        int lastGuess = machine.joltages.stream().mapToInt(it -> it).sum();
        int joltageCount = machine.joltages.size();
        for (int n = firstGuess; n <= lastGuess; n++) {
            b[joltageCount] = n;
            RealVector constants = new ArrayRealVector(b);
            double[] solution;
            try {
                solution = solver.solve(constants).toArray();
            } catch (MathIllegalArgumentException e) {
                continue;
            }
            // the solution should contain only positive numbers (double might be very close to zero)
            if (!Arrays.stream(solution).allMatch(it -> it >= -1 * NEARLY_ZERO)) {
                continue;
            }
            // the solution should contain only integers (or almost integers)
            if (!Arrays.stream(solution).allMatch(it -> Math.abs(it - Math.round(it)) < NEARLY_ZERO)) {
                continue;
            }
            return n;
        }
        return 0;
    }

    private static int tryToSolveWithChoco(Machine machine) {
//        System.out.println("*** machine = " + machine);
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
            as[i] = model.intVar("a" + i, miniumumValue, maximumValue, true);
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
            if (!intVars.isEmpty()) {
                model.sum(intVars.toArray(IntVar[]::new), "=", joltage).post();
            } else {
                throw new IllegalStateException("Zero buttons control this joltage?");
            }
        }
        // minimize total buttonpresses
        ArExpression totalButtonPresses = as[0];
        for (int i = 1; i < as.length; i++) {
            totalButtonPresses = totalButtonPresses.add(as[i]);
        }
        IntVar totalButtonPressesIntVar = totalButtonPresses.intVar();
        model.setObjective(Model.MINIMIZE, totalButtonPressesIntVar);
//        System.out.println("*** model = " + model);
        // solve
        Solver solver = model.getSolver();
        Solution solution = solver.findOptimalSolution(totalButtonPressesIntVar, Model.MINIMIZE);
//        Solution solution = solver.findSolution();
        if (solution == null) {
            System.out.println("ERROR no solutions for " + machine);
        } else {
            return solution.getIntVal(totalButtonPressesIntVar);
        }
        return 0;
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
