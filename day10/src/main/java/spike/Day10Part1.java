package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day10Part1 {

    public static final Pattern MACHINE_LIGHTS_PATTERN = Pattern.compile("\\[([.#]+)]");
    public static final Pattern MACHINE_BUTTON_PATTERN = Pattern.compile("\\(([0-9,]+)\\)");

    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");
        List<Machine> machines = lines.stream()
                .map(Machine::parse)
                .toList();
        int sum = 0;
        for (Machine machine : machines) {
            int solvedIn = -1;
            for (int i = 1; i < 10; i++) {
                if (canBeSolved(machine, i)) {
                    solvedIn = i;
                    break;
                }
            }
            if (solvedIn != -1) {
                sum += solvedIn;
            } else {
                System.out.println("no solution for " + machine);
            }
        }
        System.out.println("sum = " + sum);
        // 385
    }

    private static boolean canBeSolved(Machine machine, int presses) {
        return canBeSolved(machine, presses, new BitSet(machine.lights().length()));
    }

    private static boolean canBeSolved(Machine machine, int presses, BitSet currentLights) {
        if (presses == 0) {
            return currentLights.equals(machine.lights());
        }
        for (BitSet button : machine.buttons()) {
            currentLights.xor(button);
            if (canBeSolved(machine, presses - 1, currentLights)) {
                return true;
            }
            currentLights.xor(button); // Backtrack
        }
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day10Part1.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
