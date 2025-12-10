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

public class Day10Part1 {

    public static final Pattern MACHINE_LIGHTS_PATTERN = Pattern.compile("\\[([.#]+)]");
    public static final Pattern MACHINE_BUTTON_PATTERN = Pattern.compile("\\(([0-9,]+)\\)");

    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");
        List<Machine> machines = lines.stream()
                .map(line -> {
                    BitSet lights = parseLights(line);
                    List<BitSet> buttons = parseButtons(line, lights.length());
                    return new Machine(lights, buttons);
                })
                .toList();
        int sum = 0;
        for (Machine machine : machines) {
            if (canBeSolvedWithOneButtonPress(machine)) {
                sum += 1;
            } else if (canBeSolvedWithTwoButtonPresses(machine)) {
                sum += 2;
            } else if (canBeSolvedWithThreeButtonPresses(machine)) {
                sum += 3;
            } else if (canBeSolvedWithFourButtonPresses(machine)) {
                sum += 4;
            } else if (canBeSolvedWithFiveButtonPresses(machine)) {
                sum += 5;
            } else if (canBeSolvedWithSixButtonPresses(machine)) {
                sum += 6;
            } else if (canBeSolvedWithSevenButtonPresses(machine)) {
                sum += 7;
            } else {
                System.out.println("no solution for " + machine);
            }
        }
        System.out.println("sum = " + sum);
    }

    private static boolean canBeSolvedWithOneButtonPress(Machine machine) {
        for (BitSet button : machine.buttons()) {
            BitSet lights = new BitSet(machine.lights().length());
            lights.xor(button);
            if (lights.equals(machine.lights())) {
                return true;
            }
        }
        return false;
    }

    private static boolean canBeSolvedWithTwoButtonPresses(Machine machine) {
        for (BitSet a : machine.buttons()) {
            for (BitSet b : machine.buttons()) {
                BitSet lights = new BitSet(machine.lights().length());
                lights.xor(a);
                lights.xor(b);
                if (lights.equals(machine.lights())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean canBeSolvedWithThreeButtonPresses(Machine machine) {
        for (BitSet a : machine.buttons()) {
            for (BitSet b : machine.buttons()) {
                for (BitSet c : machine.buttons()) {
                    BitSet lights = new BitSet(machine.lights().length());
                    lights.xor(a);
                    lights.xor(b);
                    lights.xor(c);
                    if (lights.equals(machine.lights())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean canBeSolvedWithFourButtonPresses(Machine machine) {
        for (BitSet a : machine.buttons()) {
            for (BitSet b : machine.buttons()) {
                for (BitSet c : machine.buttons()) {
                    for (BitSet d : machine.buttons()) {
                        BitSet lights = new BitSet(machine.lights().length());
                        lights.xor(a);
                        lights.xor(b);
                        lights.xor(c);
                        lights.xor(d);
                        if (lights.equals(machine.lights())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean canBeSolvedWithFiveButtonPresses(Machine machine) {
        for (BitSet a : machine.buttons()) {
            for (BitSet b : machine.buttons()) {
                for (BitSet c : machine.buttons()) {
                    for (BitSet d : machine.buttons()) {
                        for (BitSet e : machine.buttons()) {
                            BitSet lights = new BitSet(machine.lights().length());
                            lights.xor(a);
                            lights.xor(b);
                            lights.xor(c);
                            lights.xor(d);
                            lights.xor(e);
                            if (lights.equals(machine.lights())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean canBeSolvedWithSixButtonPresses(Machine machine) {
        for (BitSet a : machine.buttons()) {
            for (BitSet b : machine.buttons()) {
                for (BitSet c : machine.buttons()) {
                    for (BitSet d : machine.buttons()) {
                        for (BitSet e : machine.buttons()) {
                            for (BitSet f : machine.buttons()) {
                                BitSet lights = new BitSet(machine.lights().length());
                                lights.xor(a);
                                lights.xor(b);
                                lights.xor(c);
                                lights.xor(d);
                                lights.xor(e);
                                lights.xor(f);
                                if (lights.equals(machine.lights())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean canBeSolvedWithSevenButtonPresses(Machine machine) {
        for (BitSet a : machine.buttons()) {
            for (BitSet b : machine.buttons()) {
                for (BitSet c : machine.buttons()) {
                    for (BitSet d : machine.buttons()) {
                        for (BitSet e : machine.buttons()) {
                            for (BitSet f : machine.buttons()) {
                                for (BitSet g : machine.buttons()) {
                                    BitSet lights = new BitSet(machine.lights().length());
                                    lights.xor(a);
                                    lights.xor(b);
                                    lights.xor(c);
                                    lights.xor(d);
                                    lights.xor(e);
                                    lights.xor(f);
                                    lights.xor(g);
                                    if (lights.equals(machine.lights())) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static BitSet parseLights(String line) {
        BitSet lights;
        Matcher m = MACHINE_LIGHTS_PATTERN.matcher(line);
        if (!m.find()) {
            throw new IllegalArgumentException("No lights found in " + line);
        }
        String group = m.group(1);
        lights = new BitSet(group.length());
        char[] charArray = group.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == '#') {
                lights.set(i);
            }
        }
        return lights;
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

    record Machine(BitSet lights, List<BitSet> buttons) {
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day10Part1.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
