package spike;

import org.junit.jupiter.api.Test;
import spike.Day10Part2.Machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static spike.Day10Part2.determineMinimalButtonPresses;
import static spike.Day10Part2.parseMachine;

class Day10Part2Test {
//    @Test
//    void shouldSolveWithDecompositionSolver() {
//        Machine machine = parseMachine("[..##] (1,3) (0,2,3) (0,1) (2,3) {30,15,29,34}");
//        int actual = tryToSolveWithDecompositionSolver(machine);
//        assertEquals(44, actual);
//    }
//    @Test
//    void shouldSolveWithDecompositionSolver2() {
//        Machine machine = parseMachine("[###.#..] (3,4,5) (0,3,4,5) (1,2,4,5,6) (2,4,5,6) (2,3,4,5) (0,2,3,5,6) (0,1,3) (0,1,3,4,5) (1,4,6) {32,27,33,53,43,53,26}");
//        int actual = tryToSolveWithDecompositionSolver(machine);
//        assertEquals(-1, actual);
//    }
    @Test
    void shouldSolveWithDecompositionSolver2() {
        Machine machine = parseMachine("[#...#.##.] (0,1,2,3,5,7,8) (4,5,6,7) (0,1,5,6,7,8) (0,3,5,6,7,8) (0,2,3,5,7) (0,2,4,6,7,8) (0,1,3,4,7,8) (0,1,2,3,6) (0,1,6) (0,1,3,4,5,6,8) (0,2,3,4,6,7,8) {294,255,50,80,58,243,262,271,267}");
        int actual = determineMinimalButtonPresses(machine);
        assertEquals(298, actual);
    }
    @Test
    void shouldSolveWithDecompositionSolver3() {
        Machine machine = parseMachine("[..#.#...] (0,7) (0,4,5,6,7) (2,3,4,5,6,7) (0,1,2,6) (3) (0,4,6) (0,2) (0,1,2,3,4,5) (0,1) (2,4) {65,38,56,27,44,15,44,25}");
        int actual = determineMinimalButtonPresses(machine);
        assertEquals(106, actual);
    }
}
/*
 * complicated machine = [..##] (1,3) (0,2,3) (0,1) (2,3) {30,15,29,34}
 *  simpleSolution = 35
 *  realSolution = 44
 * complicated machine = [....#.#.] (0,1,5) (2,4,6) (2,3) (2,3,7) (0,1,4,5,6,7) (0,2,3,4,5,6) {25,14,42,28,32,25,32,13}
 *  simpleSolution = 44
 *  realSolution = 56
 * complicated machine = [.#.###...] (0,1,2,3,4,8) (4,8) (0,4,8) (2,3,4,5,6,8) (3,6,7) (3,7,8) (0,2,4,5,6,7) (0,1,2,3,6,7) {40,26,37,60,56,11,31,47,65}
 *  simpleSolution = 81
 *  realSolution = 94
 * complicated machine = [.#.#.] (1,2,4) (1,3) (0) (1) (1,2,3) {18,44,26,10,18}
 *  simpleSolution = 44
 *  realSolution = 62
 * complicated machine = [###.#..] (3,4,5) (0,3,4,5) (1,2,4,5,6) (2,4,5,6) (2,3,4,5) (0,2,3,5,6) (0,1,3) (0,1,3,4,5) (1,4,6) {32,27,33,53,43,53,26}
 *  simpleSolution = 65
 *  realSolution = 64
 * complicated machine = [..##..#.] (0,2,7) (0,1,4,5,7) (1,2,3,5,6,7) (0,2,3,4,5,6,7) (4,5) (0,3,5,7) (0,1,6) {33,26,29,41,26,61,31,46}
 *  simpleSolution = 61
 *  realSolution = 71
 * complicated machine = [..##] (1) (0,2) (2,3) (0,3) {20,19,24,32}
 *  simpleSolution = 35
 *  realSolution = 57
 * complicated machine = [....#...] (0,1,6) (1,2,4,6) (0,1,3,5,7) (0,4,5) (0,1,5,6,7) (2,3,4,6) (0,2,3,4,5,6,7) (2,3,5,7) (2,6) (0,1,2,5,6,7) {49,49,58,40,21,57,58,57}
 *  simpleSolution = 79
 *  realSolution = 78
 * complicated machine = [.#.####] (0,3) (1,2) (0,1,4,5,6) (0,4) (0,1,2,3,5) {53,38,30,27,26,18,8}
 *  simpleSolution = 53
 *  realSolution = 73
 * complicated machine = [...###] (0,1,5) (0,1,2,3,4) (3,5) (0,1,2,5) (2,5) (0,3,5) {45,30,43,138,20,146}
 *  simpleSolution = 163
 *  realSolution = 166
 */