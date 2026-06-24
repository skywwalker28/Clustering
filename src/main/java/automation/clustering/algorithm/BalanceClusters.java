package automation.clustering.algorithm;

import automation.clustering.model.Cluster;
import automation.clustering.model.DeliveryPoint;

import java.util.List;

public class BalanceClusters {

    public static boolean balanceOverflowClusters(List<Cluster> clusters, Cluster curCluster) {
        System.out.println("\nBALANCE OVERFLOW CLUSTERS");
        double minDiff = Double.MAX_VALUE;
        Cluster clusterAddPoint = null;
        DeliveryPoint nearestPoint = null;

        for (DeliveryPoint point : curCluster.getPoints()) {
            double ownDistance = GeoDistance.getDistance(
                    point.getLat(), point.getLon(), curCluster.getCentroidLat(), curCluster.getCentroidLon()
            );

            for (Cluster cluster : clusters) {
                if (cluster == curCluster || cluster.getGivenPoints().contains(point)) continue;


               double alienDistance = GeoDistance.getDistance(
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
            curCluster.getGivenPoints().add(nearestPoint);

            clusterAddPoint.addPoint(nearestPoint);

            System.out.println("______Update______" +
                    "\nCluster " + curCluster.getId() + " remove point " + nearestPoint.getAddress());
            System.out.println("Cluster " + clusterAddPoint.getId() + " add point " + nearestPoint.getAddress());

            curCluster.recalculateCentroid();
            clusterAddPoint.recalculateCentroid();

            return true;
        }

        return false;
    }
}
