import org.hipparchus.linear.*;

import static java.util.stream.Collectors.joining;

/// # Day 10 part 2
///
/// ```
/// [.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
/// ```
///
/// We can interpret the buttons as:
/// ```
/// button a: 0, 0, 0, 1
/// button b: 0, 1, 0, 1
/// button c: 0, 0, 1, 0
/// button d: 0, 0, 1, 1
/// button e: 1, 0, 1, 0
/// button f: 1, 1, 0, 0
/// ```
///
/// That means the number of button presses can be expressed as linear algebra equations:
///
/// ```
/// 0a + 0b + 0c + 0d + 1e + 1f = 3
/// 0a + 1b + 0c + 0d + 0e + 1f = 5
/// 0a + 0b + 1c + 1d + 1e + 0f = 4
/// 1a + 1b + 0c + 1d + 0e + 0f = 7
/// ```
///
/// We want the minimal number of buttonpresses so we can guess from the lowest upwards:
///
/// ```
/// for (n = 7; n < 1000; n++) {
///     all above expressions plus this one
///     1a + 1b + 1c + 1d + 1e + 1f = n
/// }
/// ```
///
/// We don't have a square matrix.
/// Hipparchus is a fork of commons-math and seems better maintained.
/// According to the documentation of Hipparchus on [Linear Algebra](https://www.hipparchus.org/hipparchus-core/linear.html) we can't use an LU decomposition.
/// But QRDecomposition or SingularValueDecomposition do work with non-square matrixes.
/// QRDecomposition is well suited to solve linear least squares, which is exactly what we want.
///
/// We find a solution when `n = 10`: `a=,1 b=5, c=0, d=1, e=3, f=0`.
/// This is a different solution from what the instructions said, but I feel good about it nonetheless.
///
void main() {

    final double NEARLY_ZERO = 1e-6;

    RealMatrix coefficients = new Array2DRowRealMatrix(new double[][]{
            // button presses should yield joltages
            {0, 0, 0, 0, 1, 1},
            {0, 1, 0, 0, 0, 1},
            {0, 0, 1, 1, 1, 0},
            {1, 1, 0, 1, 0, 0},
            // sum of button presses should be minimal
            {1, 1, 1, 1, 1, 1}
    });
    DecompositionSolver solver = new QRDecomposition(coefficients, NEARLY_ZERO).getSolver();
    for (int n = 7; n < 1000; n++) {
        RealVector constants = new ArrayRealVector(new double[]{
                // button presses should yield joltages
                3,
                5,
                4,
                7,
                // sum of button presses should be minimal
                n
        });
        double[] solution = solver.solve(constants).toArray();

        // the solution should contain only positive numbers (double might be very close to zero)
        if (!Arrays.stream(solution).allMatch(it -> it >= -1 * NEARLY_ZERO)) {
            continue;
        }

        // the solution should contain only integers (or almost integers)
        if (!Arrays.stream(solution).allMatch(it -> Math.abs(it - Math.round(it)) < NEARLY_ZERO)) {
            continue;
        }

        IO.println("solution = " + Arrays.stream(solution)
                .mapToInt(it -> (int) Math.round(it))
                .mapToObj(Integer::toString)
                .collect(joining(", ")));
        IO.println("n = " + n);

        break;
    }

}
