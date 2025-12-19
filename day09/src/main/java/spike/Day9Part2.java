package spike;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class Day9Part2 {
    static void main() throws Exception {
//        List<String> lines = readInputLines("/example.txt");
        List<String> lines = readInputLines("/input.txt");
        GeometryFactory geometryFactory = new GeometryFactory();
        List<Point> points = lines.stream()
                .map(it -> it.split(","))
                .map(it -> geometryFactory.createPoint(new Coordinate(Double.parseDouble(it[0]), Double.parseDouble(it[1]))))
                .toList();
        List<Polygon> rectangles = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            Point a = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                Point b = points.get(j);
                rectangles.add(rectangle(a.getCoordinate(), b.getCoordinate(), geometryFactory));
            }
        }

        // part 1
//        double maxArea = 0;
//        for (Polygon rectangle : rectangles) {
//            maxArea = Math.max(maxArea, rectangle.getArea());
//        }
//        System.out.printf("maxArea = %.1f%n", maxArea);
        // 4769758290

        // part 2
        List<Point> pointsIncludingWrapingToFirst = new ArrayList<>(points.size() + 1);
        pointsIncludingWrapingToFirst.addAll(points);
        pointsIncludingWrapingToFirst.add(points.getFirst());
        Polygon polygon = geometryFactory.createPolygon(pointsIncludingWrapingToFirst.stream()
                .map(Point::getCoordinate)
                .toArray(Coordinate[]::new));

        // extend rightward and downward
        List<Geometry> parts = new ArrayList<>();
        parts.add(polygon);
        Coordinate[] coordinates = polygon.getCoordinates();
        for (int j = 0; j < coordinates.length - 1; j++) {
            Coordinate a = coordinates[j];
            Coordinate b = coordinates[j + 1];
            parts.add(rectangle(a, b, geometryFactory));
        }
        polygon = (Polygon) UnaryUnionOp.union(parts);

        PreparedGeometry prepared = PreparedGeometryFactory.prepare(polygon);

        // DEBUG
//        WKTWriter wktWriter = new WKTWriter(2);
//        wktWriter.setPrecisionModel(new PrecisionModel(1.));
//        System.out.println("polygon = " + wktWriter.write(polygon));

        double maxArea = rectangles.parallelStream()
                .filter(prepared::contains)
                .mapToDouble(Polygon::getArea)
                .max()
                .orElseThrow();
        System.out.printf("maxArea = %.1f%n", maxArea);
        // 1588990708
    }

    private static Polygon rectangle(Coordinate a, Coordinate b, GeometryFactory geometryFactory) {
        double top = Math.min(a.getY(), b.getY());
        double right = Math.max(a.getX(), b.getX()) + 1;
        double bottom = Math.max(a.getY(), b.getY()) + 1;
        double left = Math.min(a.getX(), b.getX());
        return geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(left, top),
                new Coordinate(right, top),
                new Coordinate(right, bottom),
                new Coordinate(left, bottom),
                new Coordinate(left, top),
        });
    }

    @SuppressWarnings("SameParameterValue")
    private static List<String> readInputLines(String name) throws IOException {
        try (var in = new BufferedReader(new InputStreamReader(requireNonNull(Day9Part2.class.getResourceAsStream(name)), UTF_8))) {
            return in.lines().toList();
        }
    }
}
