package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day1Part2 {
    static void main() throws Exception {
        List<String> lines = readInputLines("/input.txt");
//        List<String> lines = readInputLines("/input.txt");
        int zero = 0;
        int minimum = 0;
        int maximum = 99;
        int current = 50;
        int timesPointingAtZero = 0;
        for (String line : lines) {
            int rotation = Integer.parseInt(line.substring(1));
            int timesPointingAtZeroSubTotal = 0;
            if (line.startsWith("L")) {
                for (int i = 0; i < rotation; i++) {
                    current--;
                    if (current < minimum) {
                        current = maximum;
                    }
                    if (current == zero) {
                        timesPointingAtZeroSubTotal++;
                    }
                }
            } else {
                for (int i = 0; i < rotation; i++) {
                    current++;
                    if (current > maximum) {
                        current = minimum;
                    }
                    if (current == zero) {
                        timesPointingAtZeroSubTotal++;
                    }
                }
            }
            System.out.printf("The dial is rotated %s to point at %d; during this rotation, it points at %d %d times.%n", line, current, zero, timesPointingAtZeroSubTotal);
            timesPointingAtZero += timesPointingAtZeroSubTotal;
        }
        System.out.printf("The dial points at %d a total of %d times.%n", zero, timesPointingAtZero);
        // 5963
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day1Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }

}
