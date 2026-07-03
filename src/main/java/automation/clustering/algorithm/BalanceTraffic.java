package automation.clustering.algorithm;

import automation.clustering.model.Cluster;
import automation.clustering.model.DeliveryPoint;
import automation.clustering.model.MovePoint;

import java.util.HashSet;
import java.util.List;

import static automation.clustering.algorithm.DistanceHelper.*;

public class BalanceTraffic {

    public static boolean balanceTraffic(List<Cluster> clusters, int maxPoints) {
        MovePoint movePoint = null;
        double minGain = Double.MAX_VALUE;

        for (Cluster fromCluster : clusters) {

            for (DeliveryPoint point : fromCluster.getPoints()) {

                for (Cluster toCluster : clusters) {
                    if (fromCluster.getId() == toCluster.getId() || toCluster.getPoints().size() >= maxPoints) continue;

                    Cluster copyCluster1 = new Cluster(fromCluster.getId(), new HashSet<>(fromCluster.getPoints()));
                    Cluster copyCluster2 = new Cluster(toCluster.getId(), new HashSet<>(toCluster.getPoints()));

                    copyCluster1.removePoint(point);
                    copyCluster1.recalculateCentroid();

                    copyCluster2.addPoint(point);
                    copyCluster2.recalculateCentroid();

                    double oldIndex = calculateTotalTwoClusterIndex(fromCluster, toCluster);
                    double newIndex = calculateTotalTwoClusterIndex(copyCluster1, copyCluster2);
                    double newGain = newIndex - oldIndex;


                    if (newGain >= 0) continue;
                    if (newGain < minGain) {
                        movePoint = new MovePoint(point, fromCluster, toCluster);
                        minGain = newGain;
                    }
                }
            }
        }

        if (movePoint != null) {
            movePoint.getFromCluster().getPoints().remove(movePoint.getPoint());
            movePoint.getFromCluster().recalculateCentroid();

            movePoint.getToCluster().getPoints().add(movePoint.getPoint());
            movePoint.getToCluster().recalculateCentroid();

            return true;
        }

        return false;
    }
}
