package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day5Part2 {

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
        ranges.sort(null);

        int size = -1;
        while (ranges.size() != size) {
            size = ranges.size();
            ranges = mergeRangesIfOverlapping(ranges);
        }

        long freshIngredientCount = ranges.stream().mapToLong(Range::count).sum();

        System.out.printf("%d of the available ingredient IDs are fresh%n", freshIngredientCount);
        // 365804144481581
    }

    private static List<Range> mergeRangesIfOverlapping(List<Range> ranges) {
        List<Range> mergedRanges = new ArrayList<>();
        Range current = ranges.getFirst();
        for (int i = 1; i < ranges.size(); i++) {
            Range next = ranges.get(i);
            if (current.overlaps(next)) {
                current = current.merge(next);
            } else {
                mergedRanges.add(current);
                current = next;
            }
        }
        mergedRanges.add(current);
        return mergedRanges;
    }

    record Range(long min, long max) implements Comparable<Range> {
        long count() {
            return max - min + 1;
        }

        boolean overlaps(Range other) {
            return min <= other.max && max >= other.min;
        }

        Range merge(Range other) {
            return new Range(Math.min(this.min, other.min), Math.max(this.max, other.max));
        }

        @Override
        public int compareTo(Range o) {
            int result = Long.compare(min, o.min);
            if (result != 0) {
                return result;
            }
            return Long.compare(max, o.max);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day5Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
