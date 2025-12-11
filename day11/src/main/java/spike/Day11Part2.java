package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class Day11Part2 {

    static void main() throws Exception {
//        List<String> lines = readInputLines("/example2.txt").stream()
        List<String> lines = readInputLines("/input.txt").stream()
                .filter(not(String::isBlank))
                .toList();
        List<Device> devices = lines.stream()
                .map(line -> {
                    String[] split = line.split(": ");
                    String name = split[0];
                    List<String> outputs = Arrays.asList(split[1].split(" "));
                    return new Device(name, outputs);
                })
                .toList();
        Map<String, Device> devicesByName = new HashMap<>();
        devices.forEach(device -> devicesByName.put(device.name(), device));
        String source = "svr";
        String intermediateTarget1 = "fft";
        String intermediateTarget2 = "dac";
        String target = "out";

        Set<String> requiredNodes = Set.of(intermediateTarget1, intermediateTarget2);
        long pathCount = countPathsWithRequiredNodes(devicesByName, source, target, requiredNodes);
        System.out.println("paths.size() = " + pathCount);
    }

    static long countPathsWithRequiredNodes(Map<String, Device> devicesByName,
                                           String source,
                                           String target,
                                           Set<String> requiredNodes) {
        // Use memoization: map from (current node, remaining required nodes) -> path count
        Map<CacheKey, Long> memo = new HashMap<>();
        return dfs(source, target, requiredNodes, new HashSet<>(), devicesByName, memo);
    }

    private record CacheKey(String node, Set<String> remaining) {}

    private static long dfs(String current,
                           String target,
                           Set<String> requiredRemaining,
                           Set<String> visited,
                           Map<String, Device> devicesByName,
                           Map<CacheKey, Long> memo) {
        if (current.equals(target)) {
            return requiredRemaining.isEmpty() ? 1 : 0;
        }

        CacheKey key = new CacheKey(current, new HashSet<>(requiredRemaining));
        if (memo.containsKey(key)) {
            return memo.get(key);
        }

        visited.add(current);

        Set<String> newRequired = new HashSet<>(requiredRemaining);
        newRequired.remove(current);

        long totalPaths = 0;
        for (String next : devicesByName.get(current).outputs()) {
            if (!visited.contains(next)) {
                totalPaths += dfs(next, target, newRequired, new HashSet<>(visited),
                        devicesByName, memo);
            }
        }

        memo.put(key, totalPaths);
        return totalPaths;
    }

    private record Device(String name, List<String> outputs) {
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day11Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
