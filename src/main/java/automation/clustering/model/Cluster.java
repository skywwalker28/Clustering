package automation.clustering.model;

import lombok.Data;
import java.util.*;

import static automation.clustering.algorithm.DistanceHelper.getDistance;
import static automation.clustering.algorithm.DistanceHelper.sortingCluster;

@Data
public class Cluster {
    private int id;
    private double centroidLat;
    private double centroidLon;
    private Set<DeliveryPoint> points = new HashSet<>();
    private final Set<DeliveryPoint> givenPoints = new HashSet<>();

    public Cluster(int id, double startLat, double startLon) {
        this.id = id;
        centroidLat = startLat;
        centroidLon = startLon;
    }

    public Cluster(int id, Set<DeliveryPoint> points) {
        this.id = id;
        this.points = points;
    }

    public void clearPoints() {
        points.clear();
    }
    public void addPoint(DeliveryPoint point) {
        points.add(point);
    }
    public void removePoint(DeliveryPoint point) {
        points.remove(point);
        givenPoints.add(point);
    }

    public void recalculateCentroid() {
        if (points.isEmpty()) return;

        double sumLat = 0.0;
        double sumLon = 0.0;
        for (DeliveryPoint current : points) {
            sumLat += current.getLat();
            sumLon += current.getLon();
        }

        centroidLat = sumLat / points.size();
        centroidLon = sumLon / points.size();
    }

    public double calculateClusteringIndex() {
        if (getPoints().isEmpty()) return 0.0;

        double sumDistance = 0.0;
        double maxLat = -Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE, minLon = Double.MAX_VALUE;

        List<DeliveryPoint> points = sortingCluster(getPoints());

        double routeLength = 0.0;
        for (int i = 0; i < points.size()-1; i++) {
            routeLength += getDistance(points.get(i).getLat(), points.get(i).getLon(),
                    points.get(i+1).getLat(), points.get(i+1).getLon());
        }


        for (int i = 0; i < points.size(); i++) {
            DeliveryPoint point1 = points.get(i);

            maxLat = Math.max(maxLat, point1.getLat());
            maxLon = Math.max(maxLon, point1.getLon());

            minLat = Math.min(minLat, point1.getLat());
            minLon = Math.min(minLon, point1.getLon());

            for (int j = i+1; j < points.size(); j++) {
                DeliveryPoint point2 = points.get(j);

                double distance = getDistance(point1.getLat(), point1.getLon(), point2.getLat(), point2.getLon());
                sumDistance += distance;

            }
        }

        double width = maxLon - minLon;
        double heigh = maxLat - minLat;


        return routeLength + sumDistance + 3 * (width + heigh);
    }
}
