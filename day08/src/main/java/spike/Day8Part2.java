package spike;

import com.github.davidmoten.rtreemulti.Entry;
import com.github.davidmoten.rtreemulti.RTree;
import com.github.davidmoten.rtreemulti.geometry.Point;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day8Part2 {
    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");
        // Parse points
        List<Point> points = lines.stream()
                .map(it -> it.split(","))
                .map(it -> Point.create(
                        Double.parseDouble(it[0]),
                        Double.parseDouble(it[1]),
                        Double.parseDouble(it[2])
                ))
                .toList();
        RTree<String, Point> tree = RTree.dimensions(3).create();
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            tree = tree.add(i + ".", point);
        }
        // Unique connections sorted by distance
        Set<Connection> connections = new HashSet<>();
        for (Point point : points) {
            ArrayList<Entry<String, Point>> nearest = (ArrayList<Entry<String, Point>>) tree.nearest(point, Double.MAX_VALUE, 10);
            for (int i = 1; i < 10; i++) {
                connections.add(new Connection(point, nearest.get(i).geometry()));
            }
        }
        List<Connection> shortestConnections = connections.stream()
                .sorted()
                .toList();

        // Cluster the connections into circuits
        Set<Point> connectedPoints = new HashSet<>();
        List<Circuit> circuits = new ArrayList<>();
        for (Connection connection : shortestConnections) {
            boolean isUnconnected = true;
            for (Circuit circuit : circuits) {
                if (circuit.addConnection(connection)) {
                    isUnconnected = false;
                    break;
                }
            }
            if (isUnconnected) {
                Circuit circuit = new Circuit();
                circuit.addConnection(connection);
                circuits.add(circuit);
            }
            boolean added = connectedPoints.add(connection.from);
            added = connectedPoints.add(connection.to) || added;
            if (added) {
                if (connectedPoints.size() > points.size() - 3) {
                    circuits = mergeCircuits(circuits);
                }
                if (connectedPoints.size() == points.size()) {
                    double result = connection.from.values()[0] * connection.to.values()[0];
                    System.out.println("*** result = " + ((long) result));
                    return;
                }
            }
        }
        // 107256172
    }

    private static List<Circuit> mergeCircuits(List<Circuit> circuits) {
        boolean merged;
        do {
            merged = false;
            Set<Circuit> toAdd = new HashSet<>();
            Set<Circuit> toRemove = new HashSet<>();

            for (Circuit a : circuits) {
                if (toRemove.contains(a)) continue;
                for (Circuit b : circuits) {
                    if (toRemove.contains(b)) continue;
                    if (a == b) continue;
                    if (!a.sharePoint(b)) continue;
                    Circuit mergedCircuit = a.merge(b);
                    toAdd.add(mergedCircuit);
                    toRemove.add(a);
                    toRemove.add(b);
                    merged = true;
                }
            }

            if (merged) {
                circuits = new ArrayList<>(circuits);
                circuits.removeAll(toRemove);
                circuits.addAll(toAdd);
            }

        } while (merged);

        return circuits;
    }

    private static final class Circuit implements Comparable<Circuit> {
        private final Set<Point> points = new HashSet<>();

        private boolean addConnection(Connection connection) {
            if (points.isEmpty()) {
                addPoint(connection.from);
                addPoint(connection.to);
                return true;
            } else if (points.contains(connection.from)) {
                addPoint(connection.to);
                return true;
            } else if (points.contains(connection.to)) {
                addPoint(connection.from);
                return true;
            }
            return false;
        }

        private boolean addPoint(Point from) {
            return points.add(from);
        }

        private boolean sharePoint(Circuit other) {
            for (Point point : this.points) {
                if (other.points.contains(point)) {
                    return true;
                }
            }
            return false;
        }

        private Circuit merge(Circuit other) {
            Circuit merged = new Circuit();
            merged.points.addAll(this.points);
            merged.points.addAll(other.points);
            return merged;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Circuit circuit = (Circuit) o;
            return Objects.equals(points, circuit.points);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(points);
        }

        @Override
        public int compareTo(Circuit o) {
            return Integer.compare(size(), o.size());
        }

        private int size() {
            return points.size();
        }
    }

    record Connection(Point from, Point to) implements Comparable<Connection> {
        Connection(Point from, Point to) {
            if (shouldSwap(from, to)) {
                this.from = to;
                this.to = from;
            } else {
                this.from = from;
                this.to = to;
            }
        }

        private static boolean shouldSwap(Point p1, Point p2) {
            for (int i = 0; i < 3; i++) {
                int cmp = Double.compare(p1.values()[i], p2.values()[i]);
                if (cmp != 0) {
                    return cmp > 0;
                }
            }
            return false;
        }

        @Override
        public int compareTo(Connection o) {
            return Double.compare(length(), o.length());
        }

        private double length() {
            return from.distance(to);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day8Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
