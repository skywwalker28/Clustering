package automation.clustering.algorithm;

import automation.clustering.model.Cluster;
import automation.clustering.model.DeliveryPoint;
import java.util.*;

public class DistanceHelper {
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

    public static List<DeliveryPoint> sortingCluster(Set<DeliveryPoint> points) {
        List<DeliveryPoint> result = new ArrayList<>();
        Set<DeliveryPoint> visited = new HashSet<>();
        double currentLat = 55.592605, currentLon = 37.747183;

        while (result.size() < points.size()) {
            DeliveryPoint nearestPoint = null;
            double minDistance = Double.MAX_VALUE;

            for (DeliveryPoint point : points) {
                if (visited.contains(point)) continue;

                double distance = getDistance(currentLat, currentLon, point.getLat(), point.getLon());

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestPoint = point;
                }
            }

            if (nearestPoint != null) {
                result.add(nearestPoint);
                visited.add(nearestPoint);

                currentLat = nearestPoint.getLat();
                currentLon = nearestPoint.getLon();
            }
        }

        boolean improved = true;
        while (improved) {
            improved = false;

            double bestDistance = getDistanceCluster(result);

            for (int i = result.size() - 1; i >= 0; i--) {
                for (int j = 0; j < result.size(); j++) {
                    List<DeliveryPoint> candidate = getSwap(result, i, j);
                    double candidateDistance = getDistanceCluster(candidate);

                    if (candidateDistance < bestDistance) {
                        bestDistance = candidateDistance;
                        result = candidate;
                        improved = true;
                    }
                }
            }
        }

        improved = true;
        while (improved) {
            improved = false;

            double bestDistance = getDistanceCluster(result);

            for (int i = 0; i < result.size() - 1; i++) {
                for (int j = i + 2; j < result.size(); j++) {
                    List<DeliveryPoint> candidate = getReverse(result, i, j);
                    double candidateDistance = getDistanceCluster(candidate);

                    if (candidateDistance < bestDistance) {
                        bestDistance = candidateDistance;
                        result = candidate;
                        improved = true;
                    }
                }
            }
        }

        return result;
    }


    private static List<DeliveryPoint> getSwap(List<DeliveryPoint> route, int start, int end) {
        List<DeliveryPoint> copyRoute = new ArrayList<>(route);
        DeliveryPoint point = copyRoute.remove(end);
        copyRoute.add(start, point);

        return copyRoute;
    }

    private static List<DeliveryPoint> getReverse(List<DeliveryPoint> route, int start, int end) {
        List<DeliveryPoint> copyRoute = new ArrayList<>(route);
        while (start < end) Collections.swap(copyRoute, start++, end--);

        return copyRoute;
    }

    public static double getDistanceCluster(List<DeliveryPoint> points) {
        double distance = 0.0;
        double currentLat = 55.592605, currentLon = 37.747183;

        for (DeliveryPoint point : points) {
            distance += getDistance(currentLat, currentLon, point.getLat(), point.getLon());
            currentLat = point.getLat();
            currentLon = point.getLon();
        }

        return distance;
    }

    public static double calculateTotalTwoClusterIndex(Cluster cluster1, Cluster cluster2) {
        double indexCluster1 = cluster1.calculateClusteringIndex();
        double indexCluster2 = cluster2.calculateClusteringIndex();

        return indexCluster1 + indexCluster2;
    }

    public static List<Cluster> initializerCluster(List<DeliveryPoint> points, int drivers) {
        List<Cluster> clusters = new ArrayList<>();
        List<DeliveryPoint> rotated = sortedPointCircle(points);
        int sectorSize = (points.size() + drivers - 1) / drivers;

        for (int i = 0; i < drivers; i++) {

            int start = i * sectorSize, end = Math.min(sectorSize + start, points.size());
            double lat = 0.0, lon = 0.0;
            for (int j = start; j < end; j++) {
                lat += rotated.get(j).getLat();
                lon += rotated.get(j).getLon();
            }

            int count = end - start;

            clusters.add(new Cluster(i+1, lat/count, lon/count));
        }

        return clusters;
    }

    private static List<DeliveryPoint> split(List<DeliveryPoint> sortedP, Map<DeliveryPoint, Double> angleP) {
        int splitIndex = 0;
        double maxGap = -1;

        for (int i = 0; i < sortedP.size(); i++) {
            int next = (i + 1) % sortedP.size();

            double angle1 = angleP.get(sortedP.get(i));
            double angle2 = angleP.get(sortedP.get(next));

            double gap = angle2 - angle1;

            if (gap < 0) gap += 2 * Math.PI;

            if (gap > maxGap) {
                maxGap = gap;
                splitIndex = next;
            }
        }

        List<DeliveryPoint> rotated = new ArrayList<>();
        for (int i = 0; i < sortedP.size(); i++) {
            rotated.add(sortedP.get((splitIndex + i) % sortedP.size()));
        }

        return rotated;
    }

    public static List<DeliveryPoint> sortedPointCircle(List<DeliveryPoint> points) {
        double centroidLat = 0.0, centroidLon = 0.0;

        for (DeliveryPoint point : points) {
            centroidLat += point.getLat();
            centroidLon += point.getLon();
        }

        centroidLat = centroidLat/points.size();
        centroidLon = centroidLon/points.size();

        Map<DeliveryPoint, Double> pointsAngle = new HashMap<>();
        for (DeliveryPoint point : points) {
            double angle = Math.atan2(point.getLat() - centroidLat, point.getLon() - centroidLon);
            pointsAngle.put(point, angle);
        }

        List<DeliveryPoint> sortedPoints = new ArrayList<>(points);
        sortedPoints.sort(Comparator.comparingDouble(pointsAngle::get));

        return split(sortedPoints, pointsAngle);
    }
}
