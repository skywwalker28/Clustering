package automation.clustering.algorithm;

import automation.clustering.model.Cluster;
import automation.clustering.model.DeliveryPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static automation.clustering.algorithm.BalanceClusters.balanceOverflowClusters;
import static automation.clustering.algorithm.BalanceTraffic.balanceTraffic;
import static automation.clustering.algorithm.DistanceHelper.getDistance;
import static automation.clustering.algorithm.DistanceHelper.sortingCluster;

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

        for (int i = 0; i < 50; i++) {
            System.out.println("\n\nIteration " + (i + 1));
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
                if (minCluster != null) {
                    minCluster.addPoint(curPoint);
                    System.out.println("Add point " + curPoint.getNumber() + ", to Cluster " + minCluster.getId());
                }
            }

            boolean isChanged = false;
            for (Cluster curCluster : clusters) {

                System.out.print("[");
                for (DeliveryPoint point : curCluster.getPoints()) System.out.print(point.getNumber() + ", ");
                System.out.println("]");

                double oldCentroidLat = curCluster.getCentroidLat();
                double oldCentroidLon = curCluster.getCentroidLon();

                curCluster.recalculateCentroid();

                if (oldCentroidLat != curCluster.getCentroidLat() || oldCentroidLon != curCluster.getCentroidLon()) {
                    isChanged = true;
                    System.out.println("isChange = true");
                    System.out.println("new centroid = " + curCluster.getCentroidLat() + ", " + curCluster.getCentroidLon());
                } else System.out.println("isChange = false");
                System.out.println("--------------------------------------------------");
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
                    System.out.print("Cluster " + cluster.getId() + " [ ");

                    List<DeliveryPoint> sorted = sortingCluster(cluster.getPoints());
                    for (DeliveryPoint point : sorted) System.out.print(point.getNumber() + " ");
                    System.out.println("]");

                    result.put(cluster.getId(), sortingCluster(cluster.getPoints()));
                    allPoints[0] += cluster.getPoints().size();
                }

                break;
            }
        }



        return result;
    }
}