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

    private static final Pattern MACHINE_JOLTAGE_PATTERN = Pattern.compile("\\{([0-9,]+)}");
    private static final Pattern MACHINE_BUTTON_PATTERN = Pattern.compile("\\(([0-9,]+)\\)");
    private static final double NEARLY_ZERO = 1e-6;

    static void main() throws Exception {
//        List<String> lines = List.of("(3) (1,3) (2,3) (0,2) (0,1) {3,5,4,7}");
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");
        // should be solved in 10
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
        // too low: 11909
        // too low: 15375
        // too low: 16609
    }

    private static int determineMinimalButtonPresses(Machine machine) {
        double[][] a = createA(machine);
        double[] b = createB(machine);

        int i = tryToSolve(machine, a, b);
        if (i == 0) {
            // what is the lowest requested joltage
            int lowestJoltage = machine.joltages.stream().mapToInt(it -> it).min().orElseThrow();
            // what is the index of that joltage
            int lowestJoltageIndex = -1;
            List<Integer> joltages = machine.joltages;
            for (int j = 0; j < joltages.size(); j++) {
                Integer joltage = joltages.get(j);
                if (joltage == lowestJoltage) {
                    lowestJoltageIndex = j;
                    break;
                }
            }
            // which button controls that index and has the highest cardinality
            int buttonIndex = -1;
            int highestCardinality = -1;
            for (int j = 0; j < machine.buttons.size(); j++) {
                BitSet button = machine.buttons.get(j);
                if (!button.get(lowestJoltageIndex)) {
                    continue;
                }
                int cardinality = button.cardinality();
                if (cardinality > highestCardinality) {
                    highestCardinality = cardinality;
                    buttonIndex = j;
                }
            }
            // pretend we know how many button presses for that button (start with the highest first)
            int buttonCount = machine.buttons.size();
            int joltageCount = machine.joltages.size();
            double[][] aModified = new double[joltageCount + 1 + 1][buttonCount];
            for (int j = 0; j < a.length; j++) {
                for (int k = 0; k < a[j].length; k++) {
                    aModified[j][k] = a[j][k];
                }
            }
            Arrays.fill(aModified[joltageCount + 1], 0);
            aModified[joltageCount + 1][buttonIndex] = 1;
            double[] bModified = new double[joltageCount + 1 + 1];
            for (int j = 0; j < b.length; j++) {
                bModified[j] = b[j];
            }
            bModified[joltageCount + 1] = -1; // we'll guess this

            for (int j = lowestJoltage; j >= 0; j--) {
                bModified[joltageCount + 1] = j;
                i = tryToSolve(machine, aModified, bModified);
                if (i != 0) {
                    return i;
                }
            }
        }
        if (i == 0) {
            IO.println("error: solution impossible for machine " + machine);
//            System.out.println("*** buttonCount = " + buttonCount);
//            System.out.println("*** joltageCount = " + joltageCount);
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
        RealMatrix coefficients = new Array2DRowRealMatrix(a);
        int result = tryToSolve(machine, b, new QRDecomposition(coefficients, NEARLY_ZERO).getSolver());
        if (result != 0) {
            return result;
        }
        return tryToSolve(machine, b, new SingularValueDecomposition(coefficients).getSolver());
    }

    private static int tryToSolve(Machine machine, double[] b, DecompositionSolver solver) {
        int firstGuess = machine.joltages.stream().mapToInt(it -> it).max().orElseThrow();
        int lastGuess = machine.joltages.stream().mapToInt(it -> it).sum();
        int joltageCount = machine.joltages.size();
        String lastRejectionReason = null;
        for (int n = firstGuess; n <= lastGuess; n++) {
            b[joltageCount] = n;
            RealVector constants = new ArrayRealVector(b);

            double[] solution;
            try {
                solution = solver.solve(constants).toArray();
            } catch (MathIllegalArgumentException e) {
                lastRejectionReason = ("error: '" + e.getMessage() + "' for machine " + machine);
                continue;
            }
            // the solution should contain only positive numbers (double might be very close to zero)
            if (!Arrays.stream(solution).allMatch(it -> it >= -1 * NEARLY_ZERO)) {
                lastRejectionReason = ("rejected for negative numbers");
                continue;
            }
            // the solution should contain only integers (or almost integers)
            if (!Arrays.stream(solution).allMatch(it -> Math.abs(it - Math.round(it)) < NEARLY_ZERO)) {
                lastRejectionReason = ("rejected for non-integers");
                continue;
            }
            return n;
        }
//        System.out.println("*** lastRejectionReason = " + lastRejectionReason);
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
