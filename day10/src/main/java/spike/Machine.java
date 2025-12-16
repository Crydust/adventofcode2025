package spike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record Machine(String line, BitSet lights, List<BitSet> buttons, List<Integer> joltages) {

    private static final Pattern MACHINE_LIGHTS_PATTERN = Pattern.compile("\\[([.#]+)]");
    private static final Pattern MACHINE_BUTTON_PATTERN = Pattern.compile("\\(([0-9,]+)\\)");
    private static final Pattern MACHINE_JOLTAGE_PATTERN = Pattern.compile("\\{([0-9,]+)}");

    static Machine parse(String line) {
        BitSet lights = parseLights(line);
        List<BitSet> buttons = parseButtons(line, lights.length());
        List<Integer> joltages = parseJoltages(line);
        return new Machine(line, lights, buttons, joltages);
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

    @Override
    public String toString() {
        return line;
    }
}
