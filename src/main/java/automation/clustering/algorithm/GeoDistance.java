package automation.clustering.algorithm;

import automation.clustering.model.Cluster;
import automation.clustering.model.DeliveryPoint;

import java.util.ArrayList;
import java.util.List;

public class GeoDistance {
    private static final double EARTH_RADIUS = 6371000.0;

    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat1 - lat2);
        double dLon = Math.toRadians(lon1 - lon2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    public static void outPut(Cluster cluster) {
        StringBuilder sb = new StringBuilder();
        for (DeliveryPoint point : cluster.getPoints()) sb.append(point.getAddress()).append(",");
        if (!sb.isEmpty()) sb.delete(sb.length() - 1, sb.length());
        System.out.println("Cluster " + cluster.getId() + " [" + sb + "]");
    }

    public static List<DeliveryPoint> sortingClusters(Cluster cluster) {
        List<DeliveryPoint> result = new ArrayList<>();
        double currentLat = 55.592605, currentLon = 37.747183;

        while (result.size() < cluster.getPoints().size()) {
            DeliveryPoint nearestPoint = null;
            double minDistance = Double.MAX_VALUE;

            for (DeliveryPoint point : cluster.getPoints()) {
                if (result.contains(point)) continue;

                double distance = getDistance(currentLat, currentLon, point.getLat(), point.getLon());

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestPoint = point;
                }
            }

            if (nearestPoint != null) {
                result.add(nearestPoint);
                currentLat = nearestPoint.getLat();
                currentLon = nearestPoint.getLon();
            }
        }

        return result;
    }
}
