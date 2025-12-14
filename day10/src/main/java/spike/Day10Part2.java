package spike;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

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

    static void main() throws Exception {
        Stopwatch stopwatch = Stopwatch.start();
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");

        int sum = lines.parallelStream()
                .map(Day10Part2::parseMachine)
                .mapToInt(Day10Part2::determineMinimalButtonPresses)
                .sum();

        System.out.println(stopwatch.stop());
        IO.println("sum = " + sum);
        // Total time:  79546 ms (parallelStream)
        // Total time: 139259 ms (stream)
        // 16757
    }

    private static int determineMinimalButtonPresses(Machine machine) {
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
        // solve
        Solver solver = model.getSolver();
        Solution solution = solver.findOptimalSolution(totalButtonPresses, Model.MINIMIZE);
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
        Matcher m = MACHINE_JOLTAGE_PATTERN.matcher(line);
        if (!m.find()) {
            throw new IllegalArgumentException("No joltages found in " + line);
        }
        String group = m.group(1);
        return Arrays.stream(group.split(","))
                .map(Integer::valueOf)
                .toList();
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
