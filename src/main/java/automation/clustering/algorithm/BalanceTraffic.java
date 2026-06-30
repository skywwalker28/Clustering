package automation.clustering.algorithm;

import automation.clustering.model.Cluster;
import automation.clustering.model.DeliveryPoint;

import java.util.List;

public class BalanceTraffic {

    public static boolean balanceNearestPoint(List<Cluster> clusters,
                                              List<DeliveryPoint> allPoints,
                                              int maxPoints) {

        List<DeliveryPoint> circle = DistanceHelper.sortedPointCircle(allPoints);

        for (int i = 0; i < circle.size(); i++) {

            DeliveryPoint point = circle.get(i);

            Cluster ownCluster = getCluster(point, clusters);
            if (ownCluster == null) continue;

            DeliveryPoint before = circle.get((i - 1 + circle.size()) % circle.size());
            DeliveryPoint after = circle.get((i + 1) % circle.size());

            Cluster beforeCluster = getCluster(before, clusters);
            Cluster afterCluster = getCluster(after, clusters);

            if (beforeCluster == ownCluster && afterCluster == ownCluster) continue;
            if (beforeCluster != ownCluster && afterCluster != ownCluster) continue;

            DeliveryPoint alienPoint;
            Cluster alienCluster;

            if (beforeCluster != ownCluster) {
                alienPoint = before;
                alienCluster = beforeCluster;
            } else {
                alienPoint = after;
                alienCluster = afterCluster;
            }

            if (alienCluster.getPoints().size() >= maxPoints) continue;

            double ownDistance = Double.MAX_VALUE;

            if (beforeCluster == ownCluster) {
                ownDistance = Math.min(
                        ownDistance,
                        DistanceHelper.getDistance(
                                point.getLat(), point.getLon(),
                                before.getLat(), before.getLon()));
            }

            if (afterCluster == ownCluster) {
                ownDistance = Math.min(
                        ownDistance,
                        DistanceHelper.getDistance(
                                point.getLat(), point.getLon(),
                                after.getLat(), after.getLon()));
            }

            double alienDistance =
                    DistanceHelper.getDistance(
                            point.getLat(), point.getLon(),
                            alienPoint.getLat(), alienPoint.getLon());

            if (alienDistance < ownDistance) {

                ownCluster.removePoint(point);
                alienCluster.addPoint(point);

                ownCluster.recalculateCentroid();
                alienCluster.recalculateCentroid();

                System.out.println(
                        "Move point " + point.getNumber() +
                                " from Cluster " + ownCluster.getId() +
                                " to Cluster " + alienCluster.getId());

                return true;
            }
        }

        return false;
    }

    private static Cluster getCluster(DeliveryPoint point,
                                      List<Cluster> clusters) {

        for (Cluster cluster : clusters)
            if (cluster.getPoints().contains(point))
                return cluster;

        return null;
    }
}