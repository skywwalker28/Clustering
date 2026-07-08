package automation.clustering.algorithm;

import automation.clustering.model.Cluster;
import automation.clustering.model.DeliveryPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static automation.clustering.algorithm.BalanceClusters.balanceOverflowClusters;
import static automation.clustering.algorithm.BalanceTraffic.balanceTraffic;
import static automation.clustering.algorithm.DistanceHelper.getDistance;
import static automation.clustering.algorithm.DistanceHelper.sortingCluster;

public class KMeansAlgorithm {
    public static Map<Integer, List<DeliveryPoint>> cluster(List<DeliveryPoint> points,
                                                            int maxPoints, int drivers) {
        if (drivers * maxPoints < points.size()) {
            throw new RuntimeException("ERROR, " + drivers + " drivers can't take " + maxPoints +
                    " points per vehicle, because all points is " + points.size() +
                    ". Needs minimum " + (points.size() + drivers - 1) / drivers + " points per vehicle");
        }

        List<Cluster> clusters = DistanceHelper.initializerCluster(points, drivers);
        Map<Integer, List<DeliveryPoint>> result = new HashMap<>();

        for (int i = 0; i < 50; i++) {
            for (Cluster deletePointsCluster : clusters) deletePointsCluster.clearPoints();

            for (DeliveryPoint curPoint : points) {
                Cluster minCluster = null;
                double minDistance = Double.MAX_VALUE;
                for (Cluster curCluster : clusters) {
                    double dist = getDistance(curCluster.getCentroidLat(), curCluster.getCentroidLon(),
                            curPoint.getLat(), curPoint.getLon());

                    if (dist < minDistance) {
                        minCluster = curCluster;
                        minDistance = dist;
                    }
                }
                if (minCluster != null) minCluster.addPoint(curPoint);
            }

            boolean isChanged = false;
            for (Cluster curCluster : clusters) {

                double oldCentroidLat = curCluster.getCentroidLat();
                double oldCentroidLon = curCluster.getCentroidLon();

                curCluster.recalculateCentroid();

                if (oldCentroidLat != curCluster.getCentroidLat() || oldCentroidLon != curCluster.getCentroidLon()) {
                    isChanged = true;
                }
            }


            if (!isChanged) {
                boolean itemsMoved = true;
                while (itemsMoved) {
                    itemsMoved = false;
                    for (Cluster cluster : clusters) {
                        if (cluster.getPoints().size() > maxPoints) {
                            itemsMoved = balanceOverflowClusters(clusters, cluster);
                        }
                    }
                }

                int safetyNet = 0;
                while (balanceTraffic(clusters, maxPoints) && safetyNet < 50) safetyNet++;

                for (Cluster cluster : clusters) {

                    List<DeliveryPoint> sorted = sortingCluster(cluster.getPoints());
                    String output = sorted.stream()
                            .map(point -> String.valueOf(point.getNumber()))
                            .collect(Collectors.joining(", "));

                    System.out.println("Cluster " + cluster.getId() + ": [" + output + "]");

                    result.put(cluster.getId(), sortingCluster(cluster.getPoints()));
                }

                break;
            }
        }

        return result;
    }
}