# Day 10

Each machine is parsed from a line into a `Machine` record: a set of `lights`
(target on/off pattern, used only by Part 1), a list of `buttons` (each a
`BitSet` of which joltage/light positions it affects), and (for Part 2) a list
of target `joltages` per position. Pressing a button adds 1 to every joltage
position it touches; the goal is to find the minimal total number of button
presses so every position reaches its exact target joltage.

## Day10Part1.java

Brute-force backtracking search. For each machine it tries press-counts `1..9`
and does a recursive DFS over button choices (XOR-ing the button's bits into
the current light pattern and backtracking) until the exact target light
pattern is reached. Works because Part 1 only cares about on/off light state
(XOR / mod-2 arithmetic), not accumulated joltage counts.

## Day10Part2.java — Choco Solver (Constraint Programming)

Models the problem as a CP problem using [Choco Solver](https://choco-solver.org/):

- One `IntVar` per button (press count), bounded `[0, upperBound]` where
  `upperBound` is the smallest joltage target among the lights that button
  affects (a button can never be pressed more times than any single joltage
  it contributes to).
- One `sum(...) = target` constraint per joltage position.
- A `total` variable equal to the sum of all button variables, constrained
  between `max(joltages)` and `sum(joltages)`, minimized via
  `model.setObjective(Model.MINIMIZE, total)`.
- Solver is tuned with `BlackBoxConfigurator.forCSP()` (faster than the
  default COP mode for this problem) plus refined partial assignment
  generation for an extra speed-up.

Repeatedly calls `solver.solve()` until no more (better) solutions are found,
then reads `getBestSolutionValue()`. Fully delegates correctness to the CP
solver — no hand-written numerical logic.

## Day10Part2bis.java — Gaussian Elimination + Free-Variable Enumeration

Builds the linear system `A·x = b` (`A` = button/joltage incidence matrix,
`b` = joltage targets) and solves it manually via `LinearSolver`:

1. Reduce `[A|b]` to Reduced Row Echelon Form (RREF).
2. Identify "free" (non-pivot) variables — columns with no leading 1.
3. Enumerate all combinations of free-variable values (`0..149` each, capped
   by `maxTry`), back-substitute to get the dependent variables for each
   combination, and keep the integer, non-negative solution with the smallest
   sum (ties broken by smallest sum of squares).

Correctness relies on `EPSILON`-based floating-point comparisons to detect
integrality and validity. If no valid solution is found within the
enumeration cap, it prints an error and returns `0` for that machine instead
of failing loudly — a silent-failure risk. Enumeration cost grows
exponentially with the number of free variables.

## Day10Part2Simplex.java — Hand-Rolled Branch & Bound over Simplex

Uses [Hipparchus](https://hipparchus.org/)'s `SimplexSolver` to solve the LP
relaxation (`A·x = b`, `x >= 0`, minimize `sum(x)`), then wraps it in a custom
Branch & Bound implementation to enforce integrality:

1. Solve the relaxed (fractional) LP at the root.
2. If the solution has a fractional variable, branch into two sub-problems
   that add `x[i] <= floor(x[i])` or `x[i] >= ceil(x[i])`, and push both onto
   a priority queue ordered by relaxed objective value (best-first search).
3. Pop nodes in order of their LP bound, pruning any branch whose bound is
   already worse than the best integer solution found so far, until an
   all-integer solution is found.

More code and complexity than the OjAlgo/Choco versions since it reimplements
integer-programming machinery that those libraries provide out of the box,
but it is well-commented and throws explicit exceptions (rather than
silently returning `0`) if a machine turns out to be unsolvable.

## Day10Part2OjAlgo.java — ojAlgo MILP Solver

The most concise implementation. Uses [ojAlgo](https://www.ojalgo.org/)'s
`ExpressionsBasedModel` to declare the problem directly as a Mixed Integer
Linear Program:

- One non-negative integer variable per button, each with `weight(1)` so the
  solver minimizes the sum of all button presses.
- One equality expression per joltage position (`expression.level(target)`),
  with a coefficient of `1` for every button that affects that position.

Calls `model.minimise()` and returns the rounded objective value if the
result is feasible. Like the Choco version, all the hard optimization work
is delegated to a mature library — no custom numerical/tolerance code.

## Comparison

| Solution            | Technique                                      | Library does the heavy lifting?     | Notes                                                        |
|---------------------|------------------------------------------------|-------------------------------------|--------------------------------------------------------------|
| `Day10Part2`        | Constraint Programming                         | Yes (Choco)                         | Explicit performance tuning, tight variable bounds           |
| `Day10Part2bis`     | Gaussian elimination + brute-force enumeration | No (fully custom)                   | Fragile epsilon comparisons, silently returns `0` on failure |
| `Day10Part2Simplex` | Simplex + custom Branch & Bound                | Partially (Hipparchus does LP only) | More code, but fails loudly on error                         |
| `Day10Part2OjAlgo`  | MILP                                           | Yes (ojAlgo)                        | Simplest, most maintainable                                  |
