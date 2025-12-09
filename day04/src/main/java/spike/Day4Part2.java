package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day4Part2 {
    static void main(String[] args) throws IOException {
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");
        int rows = lines.size();
        int columns = lines.getFirst().length();

        Grid grid = new Grid(rows, columns);
        for (int y = 0; y < rows; y++) {
            String line = lines.get(y);
            for (int x = 0; x < columns; x++) {
                if (line.charAt(x) == '@') {
                    grid.fill(x, y);
                }
            }
        }

        int count = 0;
        boolean changed;
        do {
            changed = false;
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < columns; x++) {
                    if (grid.remove(x, y)) {
                        changed = true;
                        count++;
                    }
                }
            }
        } while (changed);
        System.out.println("*** count = " + count);
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

        boolean canBeRemoved(int x, int y) {
            return isRoll(x, y) && countNeighbors(x, y) < 4;
        }

        void fill(int x, int y) {
            items.set(index(x, y));
        }

        boolean remove(int x, int y) {
            if (!canBeRemoved(x, y)) {
                return false;
            }
            items.set(index(x, y), false);
            return true;
        }

        boolean isRoll(int x, int y) {
            return items.get(index(x, y));
        }

        int countNeighbors(int x, int y) {
            return (int) IntStream.of(
                            //top
                            index(x - 1, y - 1),
                            index(x, y - 1),
                            index(x + 1, y - 1),
                            // middle
                            index(x - 1, y),
                            index(x + 1, y),
                            // bottom
                            index(x - 1, y + 1),
                            index(x, y + 1),
                            index(x + 1, y + 1))
                    .filter(it -> it != -1)
                    .filter(items::get)
                    .count();
        }

        private int index(int x, int y) {
            if (x < 0 || x >= columns || y < 0 || y >= rows) {
                return -1;
            }
            return (y * columns) + x;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day4Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}