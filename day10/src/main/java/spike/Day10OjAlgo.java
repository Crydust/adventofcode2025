package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;

public class Day10OjAlgo {

    static void main() throws Exception {
        Stopwatch stopwatch = Stopwatch.start();
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");

        ProgressBar pb = ProgressBar.startProgressBar();

        int sum = lines.parallelStream()
                .map(Machine::parse)
                .peek(_ -> pb.incrementMax())
                .mapToInt(Day10OjAlgo::determineMinimalButtonPresses)
                .peek(_ -> pb.incrementDone())
                .sum();

        pb.stop();

        System.out.println("sum = " + sum);
        System.out.println("totalTime = " + stopwatch.stop());
    }

    static int determineMinimalButtonPresses(Machine machine) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();

        int buttonCount = machine.buttons().size();
        int joltageCount = machine.joltages().size();

        // 1. Define Variables
        // We create a variable for each button (x0, x1, ...).
        // They must be Integer and Non-Negative.
        // We set the weight to 1.0 because we want to minimize the sum of all button presses.
        for (int j = 0; j < buttonCount; j++) {
            model.addVariable("x" + j)
                    .integer(true)
                    .lower(0)
                    .weight(1);
        }

        // 2. Define Constraints
        // For each joltage component (row), the linear combination of buttons must equal the target.
        for (int i = 0; i < joltageCount; i++) {
            Expression expression = model.addExpression("JoltageConstraint" + i);
            for (int j = 0; j < buttonCount; j++) {
                // If button j affects joltage i (value is true/1.0)
                if (machine.buttons().get(j).get(i)) {
                    expression.set(j, 1);
                }
            }
            // The expression must equal the target joltage
            expression.level(machine.joltages().get(i));
        }

        // 3. Solve
        Optimisation.Result result = model.minimise();

        if (result.getState().isFeasible()) {
            return (int) Math.round(result.getValue());
        }
        return 0;
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day10OjAlgo.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
