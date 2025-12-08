package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class Day7Part1 {

    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt").stream()
        List<String> lines = readInputLines("/input.txt").stream()
                .filter(not(String::isBlank))
                .toList();
        int rowCount = lines.size();
        int columnCount = lines.getFirst().length();

        Set<Integer> beams = new TreeSet<>();
        beams.add(lines.getFirst().indexOf("S"));

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

        int splitCount = 0;
        for (int y = 0; y < rowCount; y++) {
            Set<Integer> copyOfBeams = Set.copyOf(beams);
            for (int beam : copyOfBeams) {
                if (grid.isSplitter(beam, y)) {
                    splitCount++;
                    beams.remove(beam);
                    beams.add(beam - 1);
                    beams.add(beam + 1);
                }
            }
        }

        System.out.printf("the beam is split %d times%n", splitCount);
        // 1662
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

        boolean isEmpty(int x, int y) {
            return !items.get(index(x, y));
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
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day7Part1.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
