package spike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day8Part2Bis {
    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");
        // Parse points
        List<Node> points = lines.stream()
                .map(it -> it.split(","))
                .map(it -> new Node(
                        Long.parseLong(it[0]),
                        Long.parseLong(it[1]),
                        Long.parseLong(it[2])
                ))
                .toList();

        // Unique connections sorted by distance
        Set<Edge> edges = new HashSet<>();
        for (int i = 0; i < points.size(); i++) {
            Node a = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                Node b = points.get(j);
                edges.add(Edge.create(a, b));
            }
        }
        List<Edge> shortestEdges = edges.stream().sorted().toList();

        // Cluster the connections into circuits
        Set<Node> connectedPoints = new HashSet<>();
        List<Cluster> clusters = new ArrayList<>();
        for (Edge edge : shortestEdges) {
            boolean isUnconnected = true;
            for (Cluster cluster : clusters) {
                if (cluster.addConnection(edge)) {
                    isUnconnected = false;
                    break;
                }
            }
            if (isUnconnected) {
                Cluster cluster = new Cluster();
                cluster.addConnection(edge);
                clusters.add(cluster);
            }
            boolean added = connectedPoints.add(edge.from);
            added = connectedPoints.add(edge.to) || added;
            if (added) {
                if (connectedPoints.size() > points.size() - 3) {
                    clusters = mergeClusters(clusters);
                }
                if (connectedPoints.size() == points.size()) {
                    double result = edge.from.x * edge.to.x;
                    System.out.println("*** result = " + ((long) result));
                    return;
                }
            }
        }
        // 107256172
    }

    private static List<Cluster> mergeClusters(List<Cluster> clusters) {
        boolean merged;
        do {
            merged = false;
            Set<Cluster> toAdd = new HashSet<>();
            Set<Cluster> toRemove = new HashSet<>();

            for (Cluster a : clusters) {
                if (toRemove.contains(a)) continue;
                for (Cluster b : clusters) {
                    if (toRemove.contains(b)) continue;
                    if (a == b) continue;
                    if (!a.shareNode(b)) continue;
                    Cluster mergedCluster = a.merge(b);
                    toAdd.add(mergedCluster);
                    toRemove.add(a);
                    toRemove.add(b);
                    merged = true;
                }
            }

            if (merged) {
                clusters = new ArrayList<>(clusters);
                clusters.removeAll(toRemove);
                clusters.addAll(toAdd);
            }

        } while (merged);

        return clusters;
    }

    private static final class Cluster {
        private final Set<Node> nodes;

        public Cluster() {
            this(new HashSet<>());
        }

        private Cluster(Set<Node> nodes) {
            this.nodes = nodes;
        }

        private boolean addConnection(Edge edge) {
            if (nodes.isEmpty()) {
                addNode(edge.from);
                addNode(edge.to);
                return true;
            } else if (nodes.contains(edge.from)) {
                return addNode(edge.to);
            } else if (nodes.contains(edge.to)) {
                return addNode(edge.from);
            }
            return false;
        }

        private boolean addNode(Node node) {
            return nodes.add(node);
        }

        private boolean shareNode(Cluster other) {
            for (Node point : this.nodes) {
                if (other.nodes.contains(point)) {
                    return true;
                }
            }
            return false;
        }

        private Cluster merge(Cluster other) {
            Set<Node> newNodes = new HashSet<>(this.nodes);
            newNodes.addAll(other.nodes);
            return new Cluster(newNodes);
        }
    }

    record Edge(Node from, Node to, double distSq) implements Comparable<Edge> {

        static Edge create(Node from, Node to) {
            double dX = from.x - to.x;
            double dY = from.y - to.y;
            double dZ = from.z - to.z;
            double distSq = dX * dX + dY * dY + dZ * dZ;
            return from.compareTo(to) <= 0
                    ? new Edge(from, to, distSq)
                    : new Edge(to, from, distSq);
        }

        @Override
        public int compareTo(Edge o) {
            return Double.compare(distSq, o.distSq);
        }
    }

    record Node(long x, long y, long z) implements Comparable<Node> {

        private static final Comparator<Node> NODE_COMPARATOR = Comparator.comparingLong((Node p) -> p.x)
                .thenComparingLong((Node p) -> p.y)
                .thenComparingLong((Node p) -> p.z);

        @Override
        public int compareTo(Node o) {
            return NODE_COMPARATOR.compare(this, o);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day8Part2Bis.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
