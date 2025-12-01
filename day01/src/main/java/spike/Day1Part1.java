package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day1Part1 {
    static void main() throws Exception {
        List<String> lines = readInputLines("/input.txt");
        int zero = 0;
        int minimum = 0;
        int maximum = 99;
        int current = 50;
        int timesPointingAtZero = 0;
        for (String line : lines) {
            int rotation = Integer.parseInt(line.substring(1));
            if (line.startsWith("L")) {
                rotation *= -1;
            }
            current = (current + rotation);
            if (current > maximum) {
                current %= (maximum + 1);
            }
            while (current < minimum) {
                current += maximum + 1;
            }
            if (current == zero) {
                timesPointingAtZero++;
            }
            System.out.printf("The dial is rotated %s to point at %d.%n", line, current);
        }
        System.out.printf("The dial points at %d a total of %d times.%n", zero, timesPointingAtZero);
        // 1043
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day1Part1.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }

}
