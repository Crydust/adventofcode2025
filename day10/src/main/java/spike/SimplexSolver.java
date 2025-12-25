
package spike;

public class SimplexSolver {
    private static final double EPSILON = 1e-11;

    public record Result(double objValue, double[] x, boolean feasible) {
    }

    /**
     * Minimizes c^T * x subject to Ax <= b, x >= 0 using Dual Simplex.
     */
    public static Result solve(double[][] A, double[] b, double[] c) {
        int m = A.length;
        int n = A[0].length;

        // Tableau: [ Constraints (A) | Slacks (I) | RHS (b) ]
        // Row m:   [ Objective (c)   | 0s         | 0       ]
        double[][] tab = new double[m + 1][n + m + 1];
        int[] basis = new int[m];

        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, tab[i], 0, n);
            tab[i][n + i] = 1.0; 
            tab[i][n + m] = b[i];
            basis[i] = n + i;
        }
        System.arraycopy(c, 0, tab[m], 0, n);

        return runDualSimplex(tab, basis, n);
    }

    private static Result runDualSimplex(double[][] tab, int[] basis, int nVars) {
        int m = tab.length - 1;
        int nTotal = tab[0].length - 1;

        while (true) {
            // Find pivot row p (most negative RHS)
            int p = -1;
            for (int i = 0; i < m; i++) {
                if (tab[i][nTotal] < -EPSILON && (p == -1 || tab[i][nTotal] < tab[p][nTotal])) p = i;
            }
            if (p == -1) break; // Optimal/Feasible found

            // Find pivot column q (Ratio test)
            int q = -1;
            for (int j = 0; j < nTotal; j++) {
                if (tab[p][j] < -EPSILON) {
                    if (q == -1 || tab[m][j] / -tab[p][j] < tab[m][q] / -tab[p][q]) q = j;
                }
            }
            if (q == -1) return new Result(0, null, false); // Infeasible

            pivot(tab, p, q);
            basis[p] = q;
        }

        double[] x = new double[nVars];
        for (int i = 0; i < m; i++) {
            if (basis[i] < nVars) x[basis[i]] = tab[i][nTotal];
        }
        return new Result(-tab[m][nTotal], x, true);
    }

    private static void pivot(double[][] tab, int p, int q) {
        double div = tab[p][q];
        for (int j = 0; j < tab[p].length; j++) tab[p][j] /= div;
        for (int i = 0; i < tab.length; i++) {
            if (i != p) {
                double factor = tab[i][q];
                for (int j = 0; j < tab[i].length; j++) tab[i][j] -= factor * tab[p][j];
            }
        }
    }
}