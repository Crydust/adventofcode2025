package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class Day3Part2 {
    static void main(String[] args) throws Exception {
//        List<String> banks = readInputLines("/example.txt");
        List<String> banks = readInputLines("/input.txt");
        BigInteger sum = banks.stream()
                .map(Day3Part2::maximumJoltageFromBank)
                .map(BigInteger::new)
                .reduce(BigInteger.ZERO, BigInteger::add);
        System.out.printf("The total output joltage is %s\n", sum);
        // 171846613143331
    }

    static String maximumJoltageFromBank(String bank) {
        // discard 3 batteries
        int[] batteries = Arrays.stream(bank.split("")).mapToInt(Integer::parseInt).toArray();
        for (int i = 0; i < 100; i++) {
            if (batteries.length == 12) {
                break;
            }
            removePrecedingLargeJolt(batteries, batteries.length - 11);
            batteries = Arrays.stream(batteries).filter(it -> it >= 0).toArray();
        }
        return Arrays.stream(batteries)
                .limit(12)
                .mapToObj(Integer::toString)
                .collect(joining(""));
    }

    private static void removePrecedingLargeJolt(int[] batteries, int groupSize) {
        for (int i = 0; i < batteries.length - groupSize + 1; i++) {
            int highestValue = -1;
            int highestIndex = -1;
            for (int j = i; j < i + groupSize; j++) {
                int value = batteries[j];
                if (value > highestValue) {
                    highestValue = value;
                    highestIndex = j;
                }
            }
            if (highestIndex != i && highestIndex != -1) {
                for (int j = i; j < highestIndex; j++) {
                    batteries[j] = -1;
                }
                break;
            }
        }
    }


    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day3Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }

}