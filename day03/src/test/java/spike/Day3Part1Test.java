package spike;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class Day3Part1Test {
    @ParameterizedTest
    @CsvSource(textBlock = """
            '987654321111111', 98
            '811111111111119', 89
            '234234234234278', 78
            '818181911112111', 92
            """)
    void shouldFindMaximumJoltageFromBank(String bank, int expectedJoltage) {
        int actualJoltage = Day3Part1.maximumJoltageFromBank(bank);
        assertThat(actualJoltage).isEqualTo(expectedJoltage);
    }
}