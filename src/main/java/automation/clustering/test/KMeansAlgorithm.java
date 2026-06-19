package automation.clustering.test;

import automation.clustering.model.DeliveryPoint;

import java.util.ArrayList;
import java.util.List;

public class KMeansAlgorithm {
    public List<Cluster> cluster(List<DeliveryPoint> points, int drivers, int maxPoints) {
        if ((drivers * maxPoints < points.size())) {
            throw new RuntimeException("ERROR, " + drivers + " drivers can't take " + maxPoints +
                    " points per vehicle, because all points is " + points.size() +
                    ". Needs minimum " + (points.size() + drivers - 1) / drivers + " points per vehicle");
        }

        List<Cluster> result = new ArrayList<>();
        for (int i = 1; i <= drivers; i++) {
            DeliveryPoint first = points.get(i % points.size());
            result.add(new Cluster(i, first.getLat(), first.getLon()));
        }

        for (int i = 0; i < 50; i++) {
            for (Cluster deletePointsCluster : result) deletePointsCluster.clearPoints();
            for (DeliveryPoint curPoint : points) {
                Cluster minCluster = null;
                double minDistance = Double.MAX_VALUE;
                for (Cluster curCluster : result) {
                    double dist = GeoDistance.getDistanceMeters(
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
            for (Cluster curCluster : result) {
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
                    for (Cluster cluster : result) {
                        if (cluster.getPoints().size() > maxPoints) {
                            itemsMoved = BalanceClusters.balanceOverflowClusters(result, cluster);
                        }
                    }
                }
                break;
            }
        }
        return result;
    }

    public void outPut(Cluster cluster, int number) {
        StringBuilder sb = new StringBuilder();
        for (DeliveryPoint point : cluster.getPoints()) sb.append(point.getAddress()).append(",");
        if (!sb.isEmpty()) sb.delete(sb.length() - 1, sb.length());
        System.out.println("Cluster " + number + " [" + sb + "]");
    }
}