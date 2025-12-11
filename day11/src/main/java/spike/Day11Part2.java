package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class Day11Part2 {

    static void main() throws Exception {
        List<String> lines = readInputLines("/example2.txt").stream()
//        List<String> lines = readInputLines("/input.txt").stream()
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
        Set<List<String>> paths = new HashSet<>();
        paths.add(List.of(source));
        boolean changed = true;
        while (changed) {
            changed = false;
            Set<List<String>> pathsToAdd = new HashSet<>();
            for (Iterator<List<String>> iterator = paths.iterator(); iterator.hasNext(); ) {
                List<String> path = iterator.next();
                String head = path.getLast();
                if (head.equals(target)) {
                    continue;
                }
                boolean added = false;
                for (String output : devicesByName.get(head).outputs()) {
                    if (path.contains(output)) {
                        continue;
                    }
                    added = true;
                    List<String> newPath = Stream.concat(path.stream(), Stream.of(output)).toList();
                    pathsToAdd.add(newPath);
                }
                if (added) {
                    changed = true;
                }
                iterator.remove();
            }
            paths.addAll(pathsToAdd);
        }
        paths.removeIf(not(list -> list.contains(intermediateTarget1)));
        paths.removeIf(not(list -> list.contains(intermediateTarget2)));
        System.out.println("paths.size() = " + paths.size());
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
