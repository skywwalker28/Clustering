package automation.clustering.algorithm;

import automation.clustering.model.Cluster;
import automation.clustering.model.DeliveryPoint;

import java.util.List;

public class BalanceClusters {

    public static boolean balanceOverflowClusters(List<Cluster> clusters, Cluster fromCluster) {
        double minDiff = Double.MAX_VALUE;
        Cluster toCluster = null;
        DeliveryPoint nearestPoint = null;
        for (DeliveryPoint point : fromCluster.getPoints()) {
            double ownDistance = DistanceHelper.getDistance(
                    point.getLat(), point.getLon(), fromCluster.getCentroidLat(), fromCluster.getCentroidLon()
            );

            for (Cluster cluster : clusters) {
                if (cluster == fromCluster || cluster.getGivenPoints().contains(point)) continue;


                double alienDistance = DistanceHelper.getDistance(
                        point.getLat(), point.getLon(), cluster.getCentroidLat(), cluster.getCentroidLon()
                );

                double diff = alienDistance - ownDistance;

                if (diff < minDiff) {
                    minDiff = diff;
                    toCluster = cluster;
                    nearestPoint = point;
                }
            }

        }

        if (toCluster != null) {
            fromCluster.removePoint(nearestPoint);
            toCluster.addPoint(nearestPoint);

            fromCluster.recalculateCentroid();
            toCluster.recalculateCentroid();

            return true;
        }

        return false;
    }
}
