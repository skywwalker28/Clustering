package automation.clustering.model;

import lombok.Data;
import java.util.*;

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
    public void removePoint(DeliveryPoint point) { points.remove(point); }

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
}
