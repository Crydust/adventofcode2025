package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

public class Day7Part2 {

    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt").stream()
        List<String> lines = readInputLines("/input.txt").stream()
                .filter(not(String::isBlank))
                .toList();
        int rowCount = lines.size();
        int columnCount = lines.getFirst().length();

        List<Beam> currentBeams = List.of(new Beam(lines.getFirst().indexOf("S"), 1));

        // the "+1" should not be necessary, but it fixes an off by one error
        Grid grid = new Grid(rowCount + 1, columnCount + 1);
        for (int y = 0; y < rowCount; y++) {
            String line = lines.get(y);
            for (int x = 0; x < columnCount; x++) {
                if (line.charAt(x) == '^') {
                    grid.addSplitter(x, y);
                }
            }
        }

        for (int y = 0; y < rowCount; y++) {
            List<Beam> newBeams = new ArrayList<>();
            for (Beam beam : currentBeams) {
                if (grid.isSplitter(beam.x(), y)) {
                    newBeams.add(new Beam(beam.x() - 1, beam.possibleRoutes()));
                    newBeams.add(new Beam(beam.x() + 1, beam.possibleRoutes()));
                } else {
                    newBeams.add(beam);
                }
            }
            currentBeams = newBeams.stream()
                    .collect(toMap(Beam::x, Beam::possibleRoutes, Long::sum))
                    .entrySet().stream()
                    .map(entry -> new Beam(entry.getKey(), entry.getValue()))
                    .toList();
        }

        long sum = currentBeams.stream().mapToLong(Beam::possibleRoutes).sum();
        System.out.println("sum = " + sum);
        // too low: 40941112789504
    }

    private record Beam(int x, long possibleRoutes) {
    }

    private static final class Grid {
        private final int rows;
        private final int columns;
        private final BitSet items;

        Grid(int rows, int columns) {
            this.rows = rows;
            this.columns = columns;
            items = new BitSet(rows * columns);
        }

        boolean isSplitter(int x, int y) {
            return items.get(index(x, y));
        }

        void addSplitter(int x, int y) {
            items.set(index(x, y));
        }

        private int index(int x, int y) {
            if (x < 0 || x >= rows || y < 0 || y >= columns) {
                return -1;
            }
            return (y * columns) + x;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day7Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
