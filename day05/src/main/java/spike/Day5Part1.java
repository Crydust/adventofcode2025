package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day5Part1 {

    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");

        List<Range> ranges = new ArrayList<>();
        for (String line : lines) {
            if (line.contains("-")) {
                String[] parts = line.split("-");
                Range range = new Range(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
                ranges.add(range);
            }
        }

        int freshIngredientCount = 0;
        for (String line : lines) {
            if (!line.contains("-") && !line.isEmpty()) {
                long id = Long.parseLong(line);
                boolean fresh = ranges.stream().anyMatch(r -> r.contains(id));
                if (fresh) {
                    freshIngredientCount++;
                }
            }
        }
        System.out.printf("%d of the available ingredient IDs are fresh%n", freshIngredientCount);
    }

    record Range(long min, long max) {
        boolean contains(long id) {
            return id >= min && id <= max;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day5Part1.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
