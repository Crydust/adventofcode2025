package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day9Part1 {
    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");
        List<Point2dLong> points = lines.stream()
                .map(it -> it.split(","))
                .map(it -> new Point2dLong(Long.parseLong(it[0]), Long.parseLong(it[1])))
                .toList();
        long maxArea = 0;
        for (Point2dLong a : points) {
            for (Point2dLong b : points) {
                // Add "+ 1" because ... that is what the exercise needs
                long dX = Math.abs(a.x - b.x) + 1;
                long dY = Math.abs(a.y - b.y) + 1;
                long area = dX * dY;
                maxArea = Math.max(maxArea, area);
            }
        }
        System.out.println("maxArea = " + maxArea);
    }

    private record Point2dLong(long x, long y) {
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day9Part1.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
