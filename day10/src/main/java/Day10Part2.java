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
        // LinearSolve[a,b]
//        List<String> lines = List.of(
//                "[...####.#.] (0,1,4,5,8) (4,6,7) (1,3,4,5,6,8,9) (0,5,9) (1,2,4,5,9) (0,2,3,4,5,6,7,8) (0,5,6,9) (1,3,8,9) (1,3,5,7,8,9) (4,5,7) (1,3,5,6,7,8,9) (1,2,4,5,6,7,8) (3,5,6,7) {145,40,16,28,49,200,23,46,32,152}",
//                "[.###.....#] (2,3,7) (0,1,3,4,5,6,7,9) (2,5,6) (0,1,4,6,8) (4,7,9) (1,8) (1,2,3,5,7,8,9) (0,5,7,9) (0,1,3,4,5,7,8) (5) (2,3,5,8,9) (4,9) (0,4,5,6,7,8,9) {29,26,41,30,30,73,35,45,53,47}",
//                "[.##...#..#] (0,2,4,5,8) (0,2,3,7,9) (0,1,4,6,9) (0,1,2,6,9) (0,3,4,5,6,7,8,9) (0,4,6) (1,2,4,5,6,7,8,9) (2,8) (4,5,6,7) (0,2,3,4,6,7,8,9) (0,4,6,8,9) (0,1,2,4,5,6,7,9) (1,3,6,9) {66,46,51,33,71,36,73,26,47,59}",
//                "[..##.] (1,2,4) (0,3) (2) (0) (0,2,3) (3) (0,1,4) {215,20,45,40,20}"
//        );
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");
        // should be solved in 10
        List<Machine> machines = lines.parallelStream()
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
    }

    private static int determineMinimalButtonPresses(Machine machine) {
        double[][] a = createA(machine);
        double[] b = createB(machine);

        int i = tryToSolve(machine, a, b);
        if (i == 0) {
            // much slower, but solves a few edge cases
            i = tryToSolveByFaffingIt(machine, a, b);
        }
        if (i == 0) {
            // even slower, but solves more edge cases
            i = tryToSolveByFaffingItMore(machine, a, b);
        }
        if (i == 0) {
            // ridiculously slow, but solves even more edge cases
            i = tryToSolveByFaffingItEvenMore(machine, a, b);
        }

        if (i == 0) {
            IO.println("error: solution impossible for machine " + machine);
//            System.out.println("*** buttonCount = " + buttonCount);
//            System.out.println("*** joltageCount = " + joltageCount);
        } else {
//            IO.println("solved");
        }
        return i;
    }

    private static int tryToSolveByFaffingIt(Machine machine, double[][] a, double[] b) {
        // what is the lowest requested joltage
        // int lowestJoltage = machine.joltages.stream().mapToInt(it -> it).min().orElseThrow();
        // what are the lowest requested joltage
        List<Integer> lowestJoltages = machine.joltages.stream().distinct().sorted().toList();
        for (int lowestJoltage : lowestJoltages) {
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
                int result = tryToSolve(machine, aModified, bModified);
                if (result != 0) {
                    return result;
                }
            }
        }
        return 0;
    }

    private static int tryToSolveByFaffingItMore(Machine machine, double[][] a, double[] b) {
        // what is the lowest requested joltage
        // int lowestJoltage = machine.joltages.stream().mapToInt(it -> it).min().orElseThrow();
        // what are the lowest requested joltage
        List<Integer> lowestJoltages = machine.joltages.stream().distinct().sorted().toList();
        int lowestJoltage0 = lowestJoltages.get(0);
        int lowestJoltage1 = lowestJoltages.get(1);
        // what is the index of that joltage
        int lowestJoltage0Index = -1;
        List<Integer> joltages = machine.joltages;
        for (int j = 0; j < joltages.size(); j++) {
            Integer joltage = joltages.get(j);
            if (joltage == lowestJoltage0) {
                lowestJoltage0Index = j;
                break;
            }
        }
        int lowestJoltage1Index = -1;
        for (int j = 0; j < joltages.size(); j++) {
            Integer joltage = joltages.get(j);
            if (joltage == lowestJoltage1) {
                lowestJoltage1Index = j;
                break;
            }
        }
        // which button controls that index and has the highest cardinality
        int button0Index = -1;
        int highestCardinality0 = -1;
        for (int j = 0; j < machine.buttons.size(); j++) {
            BitSet button = machine.buttons.get(j);
            if (!button.get(lowestJoltage0Index)) {
                continue;
            }
            int cardinality = button.cardinality();
            if (cardinality > highestCardinality0) {
                highestCardinality0 = cardinality;
                button0Index = j;
            }
        }
        int button1Index = -1;
        int highestCardinality1 = -1;
        for (int j = 0; j < machine.buttons.size(); j++) {
            if (j == button0Index) continue;
            BitSet button = machine.buttons.get(j);
            if (!button.get(lowestJoltage1Index)) {
                continue;
            }
            int cardinality = button.cardinality();
            if (cardinality > highestCardinality1) {
                highestCardinality1 = cardinality;
                button1Index = j;
            }
        }
        // pretend we know how many button presses for that button (start with the highest first)
        int buttonCount = machine.buttons.size();
        int joltageCount = machine.joltages.size();
        double[][] aModified = new double[joltageCount + 1 + 1 + 1][buttonCount];
        for (int j = 0; j < a.length; j++) {
            for (int k = 0; k < a[j].length; k++) {
                aModified[j][k] = a[j][k];
            }
        }
        Arrays.fill(aModified[joltageCount + 1], 0);
        Arrays.fill(aModified[joltageCount + 1 + 1], 0);
        aModified[joltageCount + 1][button0Index] = 1;
        aModified[joltageCount + 1 + 1][button1Index] = 1;
        double[] bModified = new double[joltageCount + 1 + 1 + 1];
        for (int j = 0; j < b.length; j++) {
            bModified[j] = b[j];
        }
        bModified[joltageCount + 1] = -1; // we'll guess this
        bModified[joltageCount + 1 + 1] = -1; // we'll guess this too

        for (int j = lowestJoltage0; j >= 0; j--) {
            for (int k = lowestJoltage1; k >= 0; k--) {
                bModified[joltageCount + 1] = j;
                bModified[joltageCount + 1 + 1] = k;
                int result = tryToSolve(machine, aModified, bModified);
                if (result != 0) {
                    return result;
                }
            }
        }

        return 0;
    }

    private static int tryToSolveByFaffingItEvenMore(Machine machine, double[][] a, double[] b) {
        // what is the lowest requested joltage
        // int lowestJoltage = machine.joltages.stream().mapToInt(it -> it).min().orElseThrow();
        // what are the lowest requested joltage
        List<Integer> lowestJoltages = machine.joltages.stream().distinct().sorted().toList();
        int lowestJoltage0 = lowestJoltages.get(0);
        int lowestJoltage1 = lowestJoltages.get(1);
        int lowestJoltage2 = lowestJoltages.get(2);
        // what is the index of that joltage
        int lowestJoltage0Index = -1;
        List<Integer> joltages = machine.joltages;
        for (int j = 0; j < joltages.size(); j++) {
            Integer joltage = joltages.get(j);
            if (joltage == lowestJoltage0) {
                lowestJoltage0Index = j;
                break;
            }
        }
        int lowestJoltage1Index = -1;
        for (int j = 0; j < joltages.size(); j++) {
            Integer joltage = joltages.get(j);
            if (joltage == lowestJoltage1) {
                lowestJoltage1Index = j;
                break;
            }
        }
        int lowestJoltage2Index = -1;
        for (int j = 0; j < joltages.size(); j++) {
            Integer joltage = joltages.get(j);
            if (joltage == lowestJoltage2) {
                lowestJoltage2Index = j;
                break;
            }
        }
        // which button controls that index and has the highest cardinality
        int button0Index = -1;
        int highestCardinality0 = -1;
        for (int j = 0; j < machine.buttons.size(); j++) {
            BitSet button = machine.buttons.get(j);
            if (!button.get(lowestJoltage0Index)) {
                continue;
            }
            int cardinality = button.cardinality();
            if (cardinality > highestCardinality0) {
                highestCardinality0 = cardinality;
                button0Index = j;
            }
        }
        int button1Index = -1;
        int highestCardinality1 = -1;
        for (int j = 0; j < machine.buttons.size(); j++) {
            if (j == button0Index) continue;
            BitSet button = machine.buttons.get(j);
            if (!button.get(lowestJoltage1Index)) {
                continue;
            }
            int cardinality = button.cardinality();
            if (cardinality > highestCardinality1) {
                highestCardinality1 = cardinality;
                button1Index = j;
            }
        }
        if (button1Index == -1) {
            throw new IllegalStateException("button1Index == -1");
        }
        int button2Index = -1;
        int highestCardinality2 = -1;
        for (int j = 0; j < machine.buttons.size(); j++) {
            if (j == button0Index) continue;
            if (j == button1Index) continue;
            BitSet button = machine.buttons.get(j);
            if (!button.get(lowestJoltage2Index)) {
                continue;
            }
            int cardinality = button.cardinality();
            if (cardinality > highestCardinality2) {
                highestCardinality2 = cardinality;
                button2Index = j;
            }
        }
        if (button2Index == -1) {
            throw new IllegalStateException("button2Index == -1");
        }
        // pretend we know how many button presses for that button (start with the highest first)
        int buttonCount = machine.buttons.size();
        int joltageCount = machine.joltages.size();
        double[][] aModified = new double[joltageCount + 1 + 1 + 1 + 1][buttonCount];
        for (int j = 0; j < a.length; j++) {
            for (int k = 0; k < a[j].length; k++) {
                aModified[j][k] = a[j][k];
            }
        }
        Arrays.fill(aModified[joltageCount + 1], 0);
        Arrays.fill(aModified[joltageCount + 1 + 1], 0);
        Arrays.fill(aModified[joltageCount + 1 + 1 + 1], 0);
        aModified[joltageCount + 1][button0Index] = 1;
        aModified[joltageCount + 1 + 1][button1Index] = 1;
        aModified[joltageCount + 1 + 1 + 1][button2Index] = 1;
        double[] bModified = new double[joltageCount + 1 + 1 + 1 + 1];
        for (int j = 0; j < b.length; j++) {
            bModified[j] = b[j];
        }
        bModified[joltageCount + 1] = -1; // we'll guess this
        bModified[joltageCount + 1 + 1] = -1; // we'll guess this too
        bModified[joltageCount + 1 + 1 + 1] = -1; // we'll guess this too

        for (int j = lowestJoltage0; j >= 0; j--) {
            for (int k = lowestJoltage1; k >= 0; k--) {
                for (int l = lowestJoltage2; l >= 0; l--) {
                    bModified[joltageCount + 1] = j;
                    bModified[joltageCount + 1 + 1] = k;
                    bModified[joltageCount + 1 + 1 + 1] = l;
                    int result = tryToSolve(machine, aModified, bModified);
                    if (result != 0) {
                        return result;
                    }
                }
            }
        }

        return 0;
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
        result = tryToSolveWithHipparchus(machine, a, b);
        if (result != 0) {
            return result;
        }
//        result = tryToSolveWithOjalgo(machine, a, b);
//        if (result != 0) {
//            return result;
//        }
        return result;
    }

//    private static int tryToSolveWithOjalgo(Machine machine, double[][] a, double[] b) {
//        RawStore coefficients = RawStore.wrap(a);
//
//        SingularValue<Double> svd = SingularValue.R064.make(coefficients);
//        svd.decompose(coefficients);
//        return tryToSolveWithOjalgo(machine, b, svd);
//
//    }
//
//    private static int tryToSolveWithOjalgo(Machine machine, double[] b, MatrixDecomposition.Solver<Double> solver) {
//        int firstGuess = machine.joltages.stream().mapToInt(it -> it).max().orElseThrow();
//        int lastGuess = machine.joltages.stream().mapToInt(it -> it).sum();
//        int joltageCount = machine.joltages.size();
//        for (int n = firstGuess; n <= lastGuess; n++) {
//            b[joltageCount] = n;
//            MatrixStore<Double> constants = RawStore.wrap(new double[][]{b}).transpose();
//
//            MatrixStore<Double> solution;
//            try {
//                if (!solver.isSolvable()) {
//                    continue;
//                }
//                solution = solver.getSolution(constants);
//            } catch (Exception e) {
//                continue;
//            }
//            double[] sol = solution.toRawCopy1D();
//            if (Arrays.stream(sol).anyMatch(it -> it < -1 * NEARLY_ZERO)) {
//                continue;
//            }
//            if (Arrays.stream(sol).anyMatch(it -> Math.abs(it - Math.round(it)) >= NEARLY_ZERO)) {
//                continue;
//            }
//            return n;
//        }
//        return 0;
//    }

    private static int tryToSolveWithHipparchus(Machine machine, double[][] a, double[] b) {
        RealMatrix coefficients = new Array2DRowRealMatrix(a);
//        int result = tryToSolveWithHipparchus(machine, b, new QRDecomposition(coefficients, NEARLY_ZERO).getSolver());
//        if (result != 0) {
//            return result;
//        }
        return tryToSolveWithHipparchus(machine, b, new SingularValueDecomposition(coefficients).getSolver());
    }

    private static int tryToSolveWithHipparchus(Machine machine, double[] b, DecompositionSolver solver) {
        int firstGuess = machine.joltages.stream().mapToInt(it -> it).max().orElseThrow();
        int lastGuess = machine.joltages.stream().mapToInt(it -> it).sum();
        int joltageCount = machine.joltages.size();
//        String lastRejectionReason = null;
        for (int n = firstGuess; n <= lastGuess; n++) {
            b[joltageCount] = n;
            RealVector constants = new ArrayRealVector(b);

            double[] solution;
            try {
                solution = solver.solve(constants).toArray();
            } catch (MathIllegalArgumentException e) {
//                lastRejectionReason = ("error: '" + e.getMessage() + "' for machine " + machine);
                continue;
            }
            // the solution should contain only positive numbers (double might be very close to zero)
            if (!Arrays.stream(solution).allMatch(it -> it >= -1 * NEARLY_ZERO)) {
//                lastRejectionReason = ("rejected for negative numbers");
                continue;
            }
            // the solution should contain only integers (or almost integers)
            if (!Arrays.stream(solution).allMatch(it -> Math.abs(it - Math.round(it)) < NEARLY_ZERO)) {
//                lastRejectionReason = ("rejected for non-integers");
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
