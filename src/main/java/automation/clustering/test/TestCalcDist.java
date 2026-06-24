package automation.clustering.test;

import automation.clustering.model.Cluster;
import automation.clustering.algorithm.GeoDistance;
import automation.clustering.model.DeliveryPoint;

public class TestCalcDist {

    public static void main(String[] args) {
        Cluster cluster = new Cluster(1, 55.702951, 37.768652);
        cluster.addPoint(new DeliveryPoint("2.", 10, 2, 55.776634, 37.607421));
        cluster.addPoint(new DeliveryPoint("3.", 10, 3, 55.741029, 37.628172));
        cluster.addPoint(new DeliveryPoint("4.", 10, 4, 55.756201, 37.675662));
        cluster.addPoint(new DeliveryPoint("5.", 10, 5, 55.708170, 37.654877));

        cluster.recalculateCentroid();
        System.out.println(getDistance(cluster));
    }

    public static double getDistance(Cluster cluster) {
        double distance;
        double minLat = Double.MAX_VALUE, maxLat = Double.MIN_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = Double.MIN_VALUE;

        for (DeliveryPoint point : cluster.getPoints()) {
            if (minLat > point.getLat()) minLat = point.getLat();
            if (maxLat < point.getLat()) maxLat = point.getLat();
            if (maxLon < point.getLon()) maxLon = point.getLon();
            if (minLon > point.getLon()) minLon = point.getLon();
        }

        double heigh = GeoDistance.getDistance(minLat, minLon, maxLat, minLon) / 1000.0;
        double width = GeoDistance.getDistance(minLat, minLon, minLat, maxLon) / 1000.0;

        double area = heigh * width;
        if (area < 0.1) distance = (heigh + width) * 2.0 * 1.35;
        else distance = 0.72 * Math.sqrt(cluster.getPoints().size() * area) * 1.35;

        return distance;
    }
}
