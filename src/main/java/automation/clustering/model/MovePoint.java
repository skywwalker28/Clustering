package automation.clustering.model;

import lombok.Data;

@Data
public class MovePoint {
    DeliveryPoint point;
    Cluster fromCluster;
    Cluster toCluster;

    public MovePoint(DeliveryPoint point, Cluster fromCluster, Cluster toCluster) {
        this.point = point;
        this.fromCluster = fromCluster;
        this.toCluster = toCluster;
    }
}
