package automation.clustering.model;

import lombok.Data;
import java.util.*;

import static automation.clustering.algorithm.DistanceHelper.getDistance;

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

        double centroidLat = getCentroidLat(), centroidLon = getCentroidLon();
        double wss = 0.0, varianceLat = 0.0, varianceLon = 0.0, covLatLon = 0.0;

        int n = getPoints().size();
        double lonCorrection = Math.cos(Math.toRadians(centroidLat));

        for (DeliveryPoint point : getPoints()) {
            double dLat = point.getLat() - centroidLat;
            double dLon = (point.getLon() - centroidLon) * lonCorrection;

            varianceLat += dLat * dLat;
            varianceLon += dLon * dLon;
            covLatLon += dLat * dLon;

            double distance = getDistance(point.getLat(), point.getLon(), centroidLat, centroidLon);
            wss += distance * distance;
        }

        double a = varianceLat / n, c = varianceLon / n, b = covLatLon / n;
        double trace = a + c, determinant = a * c - b * b;

        double discriminant = Math.max(0.0, trace * trace - 4.0 * determinant);
        double sqrtDist = Math.sqrt(discriminant);

        double lambda1 = (trace + sqrtDist) / 2.0;
        double lambda2 = (trace - sqrtDist) / 2.0;

        lambda2 = Math.max(lambda2, 1e-12);

        double elongation = Math.sqrt(lambda1 / lambda2);
        elongation = Math.min(elongation, 10.0);

        double totalWssSqKm = wss / 1000000.0;

        double areaPenalty = totalWssSqKm * totalWssSqKm;

        return areaPenalty * Math.sqrt(elongation);
    }

}
