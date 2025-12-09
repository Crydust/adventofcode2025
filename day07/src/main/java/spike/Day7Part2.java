package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class Day7Part2 {

    static void main() throws Exception {
        List<String> lines = readInputLines("/example.txt").stream()
//        List<String> lines = readInputLines("/input.txt").stream()
                .filter(not(String::isBlank))
                .toList();
        int rowCount = lines.size();
        int columnCount = lines.getFirst().length();

        int start = lines.getFirst().indexOf("S");
        List<Beam> currentBeams = List.of(new Beam(start, 1));

        Grid grid = new Grid(rowCount, columnCount);
        for (int y = 0; y < rowCount; y++) {
            String line = lines.get(y);
            for (int x = 0; x < columnCount; x++) {
                if (line.charAt(x) == '^') {
                    grid.addSplitter(x, y);
                }
            }
        }
        //System.out.println("grid = \n" + grid);

        for (int y = 0; y < rowCount; y++) {
            Map<Integer, Long> newBeams = new HashMap<>();
            for (Beam beam : currentBeams) {
                if (grid.isSplitter(beam.x(), y)) {
                    newBeams.merge(beam.x() - 1, beam.possibleRoutes(), Long::sum);
                    newBeams.merge(beam.x() + 1, beam.possibleRoutes(), Long::sum);
                } else {
                    newBeams.merge(beam.x(), beam.possibleRoutes(), Long::sum);
                }
            }
            currentBeams = newBeams.entrySet().stream()
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
            if (x < 0 || x >= columns || y < 0 || y >= rows) {
                throw new IllegalArgumentException("Invalid coordinates: (" + x + ", " + y + ")");
            }
            return (y * columns) + x;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < columns; x++) {
                    sb.append(isSplitter(x, y) ? '^' : '.');
                }
                sb.append('\n');
            }
            return sb.toString();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day7Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
