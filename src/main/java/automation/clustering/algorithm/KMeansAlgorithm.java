package automation.clustering.algorithm;

import automation.clustering.model.Cluster;
import automation.clustering.model.DeliveryPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KMeansAlgorithm {
    public static Map<Integer, List<DeliveryPoint>> cluster(List<DeliveryPoint> points,
                                                            int maxPoints, int drivers, int[] allPoints) {
        if (drivers * maxPoints < points.size()) {
            throw new RuntimeException("ERROR, " + drivers + " drivers can't take " + maxPoints +
                    " points per vehicle, because all points is " + points.size() +
                    ". Needs minimum " + (points.size() + drivers - 1) / drivers + " points per vehicle");
        }

        List<Cluster> clusters = DistanceHelper.initializerCluster(points, drivers);
        Map<Integer, List<DeliveryPoint>> result = new HashMap<>();

        for (Cluster deletePointsCluster : clusters) deletePointsCluster.clearPoints();

        for (DeliveryPoint curPoint : points) {
            Cluster minCluster = null;
            double minDistance = Double.MAX_VALUE;
            for (Cluster curCluster : clusters) {
                double dist = DistanceHelper.getDistance(
                        curCluster.getCentroidLat(), curCluster.getCentroidLon(),
                        curPoint.getLat(), curPoint.getLon());

                if (dist < minDistance) {
                    minCluster = curCluster;
                    minDistance = dist;
                }
            }
            if (minCluster != null) {
                minCluster.addPoint(curPoint);
            }
        }


        boolean itemsMoved = true;
        while (itemsMoved) {
            itemsMoved = false;
            for (Cluster cluster : clusters) {
                if (cluster.getPoints().size() > maxPoints) {
                    itemsMoved = BalanceClusters.balanceOverflowClusters(clusters, cluster);
                }
            }
        }

        itemsMoved = true;
        while (itemsMoved) itemsMoved = BalanceTraffic.balanceNearestPoint(clusters, points, maxPoints);

        for (Cluster cluster : clusters) {
            System.out.print("Cluster " + cluster.getId() +" [ ");
            for (DeliveryPoint point : cluster.getPoints()) System.out.print(point.getNumber() + " ");
            System.out.println("]");

            result.put(cluster.getId(), DistanceHelper.sortingCluster(cluster.getPoints()));
            allPoints[0] += cluster.getPoints().size();
        }



        return result;
    }
}