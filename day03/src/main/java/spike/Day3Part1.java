package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day3Part1 {
    static void main(String[] args) throws Exception {
//        List<String> banks = readInputLines("/example.txt");
        List<String> banks = readInputLines("/input.txt");
        int sum = banks.stream()
                .mapToInt(Day3Part1::maximumJoltageFromBank)
                .sum();
        System.out.printf("The total output joltage is %d\n", sum);
    }

    static int maximumJoltageFromBank(String bank) {
        for (int firstBattery = 9; firstBattery > 0; firstBattery--) {
            int firstBatteryIndex = bank.indexOf(String.valueOf(firstBattery));
            if (firstBatteryIndex == -1) {
                continue;
            }
            for (int secondBattery = 9; secondBattery > 0; secondBattery--) {
                int secondBatteryIndex = bank.indexOf(String.valueOf(secondBattery), firstBatteryIndex + 1);
                if (secondBatteryIndex == -1) {
                    continue;
                }
                return firstBattery * 10 + secondBattery;
            }
        }
        return 0;
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day3Part1.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }

}