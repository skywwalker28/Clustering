package automation.clustering.test;

import automation.clustering.model.DeliveryPoint;

import java.util.List;

public class BalanceClusters {
    static KMeansAlgorithm algorithm = new KMeansAlgorithm();

    public static boolean balanceOverflowClusters(List<Cluster> clusters, Cluster curCluster) {
        System.out.println("BALANCE OVERFLOW CLUSTERS");
        double minDiff = Double.MAX_VALUE;
        Cluster clusterAddPoint = null;
        DeliveryPoint nearestPoint = null;

        for (DeliveryPoint point : curCluster.getPoints()) {

            double ownDistance = GeoDistance.getDistanceMeters(
                    point.getLat(), point.getLon(), curCluster.getCentroidLat(), curCluster.getCentroidLon()
            );

            for (Cluster cluster : clusters) {
                if (cluster == curCluster || cluster.getGivenPoints().contains(point)) {
                    if (cluster.getGivenPoints().contains(point)) {
                        System.out.println("Cluster not give point " + point.getAddress());
                    }

                    continue;
                }

               double alienDistance = GeoDistance.getDistanceMeters(
                       point.getLat(), point.getLon(), cluster.getCentroidLat(), cluster.getCentroidLon()
               );


               double diff = alienDistance - ownDistance;
               if (diff < minDiff) {
                   minDiff = diff;
                   clusterAddPoint = cluster;
                   nearestPoint = point;
               }
            }

        }

        if (clusterAddPoint != null) {
            curCluster.removePoint(nearestPoint);
            clusterAddPoint.addPoint(nearestPoint);

            curCluster.recalculateCentroid();
            clusterAddPoint.recalculateCentroid();

            return true;
        }

        return false;
    }
}
