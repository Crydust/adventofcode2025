package spike;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.BlackBoxConfigurator;
import org.chocosolver.solver.variables.IntVar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day10Part2 {

    static void main() throws Exception {
        Stopwatch stopwatch = Stopwatch.start();
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");

        ProgressBar pb = ProgressBar.startProgressBar();

        int sum = lines.parallelStream()
                .map(Machine::parse)
                .peek(_ -> pb.incrementMax())
                .mapToInt(Day10Part2::determineMinimalButtonPresses)
                .peek(_ -> pb.incrementDone())
                .sum();

        pb.stop();

        System.out.println("sum = " + sum);
        System.out.println("totalTime = " + stopwatch.stop());
        // 16757
    }

    static int determineMinimalButtonPresses(Machine machine) {
        var model = createModel(machine);
        Solver solver = model.getSolver();
        while (solver.solve()) {
            // NOOP
        }
        return solver.getBestSolutionValue().intValue();
    }

    private static Model createModel(Machine machine) {
        Settings settings = Settings.prod();
        Model model = new Model(machine.line(), settings);
        int buttonCount = machine.buttons().size();
        int joltageCount = machine.joltages().size();
        // create a variable for each button press count
        IntVar[] buttonPresses = new IntVar[buttonCount];
        for (int b = 0; b < buttonCount; b++) {
            int upperBound = Integer.MAX_VALUE;
            for (int j = 0; j < joltageCount; j++) {
                if (machine.buttons().get(b).get(j)) {
                    upperBound = Math.min(upperBound, machine.joltages().get(j));
                }
            }
            buttonPresses[b] = model.intVar("b" + b, 0, upperBound, true);
        }
        // button presses should sum to the joltage
        for (int j = 0; j < joltageCount; j++) {
            List<IntVar> intVars = new ArrayList<>();
            for (int b = 0; b < machine.buttons().size(); b++) {
                if (machine.buttons().get(b).get(j)) {
                    intVars.add(buttonPresses[b]);
                }
            }
            model.sum(intVars.toArray(IntVar[]::new), "=", machine.joltages().get(j)).post();
        }
        // minimize total button presses
        IntVar total = model.sum("total", buttonPresses);
        model.arithm(total, ">=", machine.joltages().stream().mapToInt(it -> it).max().orElseThrow()).post();
        model.arithm(total, "<=", machine.joltages().stream().mapToInt(it -> it).sum()).post();
        model.setObjective(Model.MINIMIZE, total);
        // configure the solver to improve performance
        // `forCSP()` gives a 50% speed boost over `forCOP()` which is the default for `MINIMIZE`
        BlackBoxConfigurator bb = BlackBoxConfigurator.forCSP();
        // 5% faster
        bb.setRefinedPartialAssignmentGeneration(true);
        bb.make(model);
        return model;
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day10Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }

}
