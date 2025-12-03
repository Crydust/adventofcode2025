package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day2Part1 {

    private static final Pattern INVALID_ID_PATTERN = Pattern.compile("(.{1,9})\\1");

    static void main() throws Exception {
//        String name = "/example.txt";
        String name = "/input.txt";
        String input = readInputAsString(name);
        List<ProductIdRange> productIdRanges = parseProductIdRanges(input);
        System.out.println("productIdRanges = " + productIdRanges);
        long sum = 0;
        for (ProductIdRange productIdRange : productIdRanges) {
            List<String> subListOfInvalidIds = new ArrayList<>();
            long first = productIdRange.firstIdAsLong();
            long last = productIdRange.lastIdAsLong();
            for (long i = first; i <= last; i++) {
                String s = Long.toString(i);
                if (INVALID_ID_PATTERN.matcher(s).matches()) {
                    subListOfInvalidIds.add(s);
                    sum += i;
                }
            }
            System.out.printf("%s has %d invalid IDs, %s.%n", productIdRange, subListOfInvalidIds.size(), subListOfInvalidIds);
        }
        System.out.printf("Adding up all the invalid IDs in this example produces %d.%n", sum);
        // 23560874270
    }

    private static List<ProductIdRange> parseProductIdRanges(String s) {
        return Arrays.stream(s.trim().split(","))
                .map(it -> it.split("-"))
                .map(it -> new ProductIdRange(it[0], it[1]))
                .toList();
    }

    @SuppressWarnings("SameParameterValue")
    private static String readInputAsString(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day2Part1.class.getResourceAsStream(name)), UTF_8))) {
            return in.readAllAsString();
        }
    }

    private record ProductIdRange(String firstId, String lastId) {

        long firstIdAsLong() {
            return Long.parseLong(firstId);
        }

        long lastIdAsLong() {
            return Long.parseLong(lastId);
        }

        @Override
        public String toString() {
            return firstId + '-' + lastId;
        }
    }

}
