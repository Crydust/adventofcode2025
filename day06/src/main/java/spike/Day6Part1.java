package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class Day6Part1 {

    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt").stream()
        List<String> lines = readInputLines("/input.txt").stream()
                .map(String::trim)
                .filter(not(String::isBlank))
                .toList();
        int rowCount = lines.size() - 1;
        int columnCount = lines.getFirst().split("\\s+").length;
        long total = 0;
        for (int column = 0; column < columnCount; column++) {
            List<String> columnValues = new ArrayList<>();
            for (String line : lines) {
                columnValues.add(line.split("\\s+")[column]);
            }
            long columnResult = switch (columnValues.getLast()) {
                case "+" -> columnValues.stream()
                        .limit(rowCount)
                        .mapToLong(Long::parseLong)
                        .sum();
                case "*" -> columnValues.stream()
                        .limit(rowCount)
                        .mapToLong(Long::parseLong)
                        .reduce(1, (a, b) -> a * b);
                default -> 0;
            };
            total += columnResult;
        }
        System.out.println(total);
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day6Part1.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
