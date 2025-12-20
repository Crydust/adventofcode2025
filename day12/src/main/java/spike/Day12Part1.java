package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day12Part1 {

    private static final Pattern REGION_PATTERN = Pattern.compile("^([0-9]+)x([0-9]+): ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+)$");

    static void main() throws Exception {
        List<String> lines = readInputLines("/example.txt").stream()
//        List<String> lines = readInputLines("/input.txt").stream()
                .toList();

        Grid[] shapes = readShapes(lines);
        printShapes(shapes);
        List<Region> regions = readRegions(lines);
        long count = regions.stream()
                .filter(it -> it.canFitPresents(shapes))
                .count();
        System.out.println("count = " + count);
    }

    private static Grid[] readShapes(List<String> lines) {
        Grid[] shapes = new Grid[6];
        int index = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.endsWith(":")) {
                i++;
                Grid shape = new Grid(3, 3);
                for (int y = 0; y < 3; y++) {
                    String row = lines.get(i + y);
                    for (int x = 0; x < 3; x++) {
                        if (row.charAt(x) == '#') {
                            shape.occupy(x, y);
                        }
                    }
                }
                shapes[index] = shape;
                index++;
            }
        }
        return shapes;
    }

    private static void printShapes(Grid[] shapes) {
        for (int i = 0; i < shapes.length; i++) {
            Grid shape = shapes[i];
            System.out.println(i + ":");
            System.out.println(shape);
            System.out.println();
        }
    }

    private static List<Region> readRegions(List<String> lines) {
        List<Region> regions = new ArrayList<>();
        for (String line : lines) {
            Matcher m = REGION_PATTERN.matcher(line);
            if (m.matches()) {
                int rows = parseInt(m.group(1));
                int cols = parseInt(m.group(2));
                int[] shapeCounts = {
                        parseInt(m.group(3)),
                        parseInt(m.group(4)),
                        parseInt(m.group(5)),
                        parseInt(m.group(6)),
                        parseInt(m.group(7)),
                        parseInt(m.group(8))
                };
                Region region = new Region(new Grid(rows, cols), shapeCounts);
                regions.add(region);
            }
        }
        return regions;
    }

    private record Region(Grid grid, int[] shapeCounts) {
        public boolean canFitPresents(Grid[] shapes) {
            for (int i = 0; i < shapeCounts.length; i++) {
                int shapeCount = shapeCounts[i];
                Grid shape = shapes[i];

            }
            return false;
        }
    }

    private static final class Grid {
        private final int rows;
        private final int columns;
        private final BitSet square;

        Grid(int rows, int columns) {
            this.rows = rows;
            this.columns = columns;
            square = new BitSet(rows * columns);
        }

        boolean isOccupied(int x, int y) {
            return square.get(index(x, y));
        }

        void occupy(int x, int y) {
            square.set(index(x, y));
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
                    sb.append(isOccupied(x, y) ? '#' : '.');
                }
                sb.append('\n');
            }
            return sb.toString();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day12Part1.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
