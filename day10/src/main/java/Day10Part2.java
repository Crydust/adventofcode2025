import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day10Part2 {

    public static final Pattern MACHINE_JOLTAGE_PATTERN = Pattern.compile("\\{([0-9,]+)}");
    public static final Pattern MACHINE_BUTTON_PATTERN = Pattern.compile("\\(([0-9,]+)\\)");

    static void main() throws Exception {
//        List<String> lines = List.of("(3) (1,3) (2,3) (0,2) (0,1) {3,5,4,7}");
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");
        // should be solved in 10
        List<Machine> machines = lines.stream()
                .map(line -> {
                    List<Integer> joltages = parseJoltages(line);
                    List<BitSet> buttons = parseButtons(line, joltages.size());
                    return new Machine(joltages, buttons);
                })
                .toList();

        AtomicInteger solvedMachines = new AtomicInteger(0);

        int sum = machines.parallelStream()
                .mapToInt(machine -> {
                    int solvedIn = -1;
                    for (int i = 1; i < 300; i++) {
                        if (canBeSolved(machine, i)) {
                            solvedIn = i;
                            break;
                        }
                    }
                    if (solvedIn != -1) {
                        int currentSolvedMachines = solvedMachines.incrementAndGet();
                        System.out.println("currentSolvedMachines = " + currentSolvedMachines);
                        return solvedIn;
                    } else {
                        System.out.println("no solution for " + machine);
                        return 0;
                    }
                })
                .sum();

//        int sum = 0;
//        for (int j = 0; j < machines.size(); j++) {
//            System.out.println("j = " + j + " of " + machines.size());
//            Machine machine = machines.get(j);
//            int solvedIn = -1;
//            for (int i = 1; i < 15; i++) {
//                if (canBeSolved(machine, i)) {
//                    solvedIn = i;
//                    break;
//                }
//            }
//            if (solvedIn != -1) {
//                sum += solvedIn;
//            } else {
//                System.out.println("no solution for " + machine);
//            }
//        }
        System.out.println("sum = " + sum);
        // 385
    }

    private static boolean canBeSolved(Machine machine, int presses) {
        return canBeSolved(
                machine,
                presses,
                new ArrayList<>(Collections.nCopies(machine.joltages().size(), 0)));
    }

    private static boolean canBeSolved(Machine machine, int presses, List<Integer> currentJoltages) {
        if (presses == 0) {
            return currentJoltages.equals(machine.joltages());
        }
        for (BitSet button : machine.buttons()) {
            pressButton(currentJoltages, button);
            if (canBeSolved(machine, presses - 1, currentJoltages)) {
                return true;
            }
            undoPressButton(currentJoltages, button); // Backtrack
        }
        return false;
    }

    private static void pressButton(List<Integer> currentJoltages, BitSet button) {
        for (int i = button.nextSetBit(0); i >= 0; i = button.nextSetBit(i + 1)) {
            currentJoltages.set(i, currentJoltages.get(i) + 1);
        }
    }

    private static void undoPressButton(List<Integer> currentJoltages, BitSet button) {
        for (int i = button.nextSetBit(0); i >= 0; i = button.nextSetBit(i + 1)) {
            currentJoltages.set(i, currentJoltages.get(i) - 1);
        }
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

    record Machine(List<Integer> joltages, List<BitSet> buttons) {
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day10Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
