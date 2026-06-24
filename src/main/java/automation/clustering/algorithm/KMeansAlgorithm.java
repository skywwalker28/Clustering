package automation.clustering.algorithm;

import automation.clustering.model.Cluster;
import automation.clustering.model.DeliveryPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KMeansAlgorithm {
    public static Map<Integer, List<DeliveryPoint>> cluster(List<DeliveryPoint> points, int drivers, int maxPoints) {
        if ((drivers * maxPoints < points.size())) {
            throw new RuntimeException("ERROR, " + drivers + " drivers can't take " + maxPoints +
                    " points per vehicle, because all points is " + points.size() +
                    ". Needs minimum " + (points.size() + drivers - 1) / drivers + " points per vehicle");
        }

        List<Cluster> clusters = new ArrayList<>();
        Map<Integer, List<DeliveryPoint>> result = new HashMap<>();

        for (int i = 1; i <= drivers; i++) {
            DeliveryPoint first = points.get(i % points.size());
            clusters.add(new Cluster(i, first.getLat(), first.getLon()));
        }

        for (int i = 1; i <= 15; i++) {
            System.out.println("\n\nIterations " + i);
            for (Cluster deletePointsCluster : clusters) deletePointsCluster.clearPoints();
            for (DeliveryPoint curPoint : points) {
                Cluster minCluster = null;
                double minDistance = Double.MAX_VALUE;
                for (Cluster curCluster : clusters) {
                    double dist = GeoDistance.getDistance(
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
            boolean isChanged = false;
            for (Cluster curCluster : clusters) {
                GeoDistance.outPut(curCluster);

                double oldCentroidLat = curCluster.getCentroidLat();
                double oldCentroidLon = curCluster.getCentroidLon();

                curCluster.recalculateCentroid();

                if (oldCentroidLat != curCluster.getCentroidLat() || oldCentroidLon != curCluster.getCentroidLon()) {
                    isChanged = true;
                    System.out.println("isChange = true");
                } else System.out.println("isChange = false");

                System.out.println("oldLat = " + oldCentroidLat + ", newLat = " + curCluster.getCentroidLat());
                System.out.println("oldLon = " + oldCentroidLon + ", newLon = " + curCluster.getCentroidLon());
                System.out.println("--------------------------------------------------");
            }

            if (!isChanged) {
                boolean itemsMoved = true;
                while (itemsMoved) {
                    itemsMoved = false;
                    for (Cluster cluster : clusters) {
                        if (cluster.getPoints().size() > maxPoints) {
                            itemsMoved = BalanceClusters.balanceOverflowClusters(clusters, cluster);
                        }
                    }
                }

                for (Cluster cluster : clusters) result.put(cluster.getId(), GeoDistance.sortingClusters(cluster));
                break;
            }
        }

        return result;
    }
}