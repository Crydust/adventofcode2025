package spike;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class Day3Part2Test {
    @ParameterizedTest
    @CsvSource(textBlock = """
            '987654321111111', '987654321111'
            '811111111111119', '811111111119'
            '234234234234278', '434234234278'
            '818181911112111', '888911112111'
            """)
    void shouldFindMaximumJoltageFromBank(String bank, String expectedJoltage) {
        String actualJoltage = Day3Part2.maximumJoltageFromBank(bank);
        assertThat(actualJoltage).isEqualTo(expectedJoltage);
    }
}