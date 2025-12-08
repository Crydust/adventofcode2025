package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class Day6Part2 {

    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt").stream()
        List<String> lines = readInputLines("/input.txt").stream()
                .filter(not(String::isBlank))
                .toList();
        int columnCount = lines.stream().mapToInt(String::length).max().orElse(0);
        int rowCount = lines.size() - 1;
        List<char[]> lineChars = lines.stream()
                .limit(rowCount)
                .map(String::toCharArray)
                .toList();
        char[] charsOfLastLine = lines.getLast().toCharArray();
        int currentColumn = 0;
        long total = 0;
        while (true) {
            int nextColumn = 0;
            for (int column = currentColumn + 1; column < charsOfLastLine.length; column++) {
                if (charsOfLastLine[column] != ' ') {
                    nextColumn = column;
                    break;
                }
            }
            char operator = charsOfLastLine[currentColumn];
            long subTotal = operator == '+' ? 0 : 1;
            for (int column = currentColumn; column < (nextColumn == 0 ? columnCount : nextColumn - 1); column++) {
                StringBuilder sb = new StringBuilder();
                for (int row = 0; row < rowCount; row++) {
                    char[] chars = lineChars.get(row);
                    if (column >= chars.length) {
                        continue;
                    }
                    char c = chars[column];
                    if (c != ' ') {
                        sb.append(c);
                    }
                }
                long columnValue = Long.parseLong(sb.toString());
                switch (operator) {
                    case '+' -> subTotal += columnValue;
                    case '*' -> subTotal *= columnValue;
                }
            }
            total += subTotal;
            if (nextColumn == 0) {
                break;
            }
            currentColumn = nextColumn;
        }

        System.out.println(total);
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day6Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
