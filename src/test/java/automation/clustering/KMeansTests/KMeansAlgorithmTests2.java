/*
package automation.clustering.KMeansTests;

import automation.clustering.algorithm.GeoDistance;
import automation.clustering.model.DeliveryPoint;
import automation.clustering.model.Cluster;
import automation.clustering.algorithm.KMeansAlgorithm;

import java.util.ArrayList;
import java.util.List;

public class KMeansAlgorithmTests2 {
    public static void main(String[] args) {
//        test13Points();
//        testBoundaryPingPong();
//        testForcedThirdCluster();
//        testOutliers();
        testCapacityStress();
    }

    public static void test13Points() {
        List<DeliveryPoint> points = List.of(
                // Центр (плотный)
                new DeliveryPoint("C1",10,1,55.7512,37.6184),
                new DeliveryPoint("C2",10,2,55.7520,37.6200),
                new DeliveryPoint("C3",10,3,55.7500,37.6170),
                new DeliveryPoint("C4",10,4,55.7530,37.6190),

                // Север
                new DeliveryPoint("N1",10,5,55.8500,37.6000),
                new DeliveryPoint("N2",10,6,55.8600,37.6100),
                new DeliveryPoint("N3",10,7,55.8400,37.5900),

                // Юг
                new DeliveryPoint("S1",10,8,55.6500,37.6000),
                new DeliveryPoint("S2",10,9,55.6400,37.6100),
                new DeliveryPoint("S3",10,10,55.6300,37.6200),

                // ПЕРИФЕРИЯ (ломает k-means)
                new DeliveryPoint("P1",10,11,55.4000,37.3000),
                new DeliveryPoint("P2",10,12,55.4200,37.2800),
                new DeliveryPoint("P3",10,13,55.3800,37.3200)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 3, 10);

        System.out.println("\nResult:");
        for (Cluster cluster : result) GeoDistance.outPut(cluster);
    }

    public static void testBoundaryPingPong() {

        System.out.println("==== TEST 1: BOUNDARY PING PONG ====");

        List<DeliveryPoint> points = List.of(
                new DeliveryPoint("A1", 10, 1, 55.7512, 37.6184),
                new DeliveryPoint("A2", 10, 2, 55.7520, 37.6190),
                new DeliveryPoint("A3", 10, 3, 55.7505, 37.6175),

                new DeliveryPoint("B1", 10, 4, 55.7530, 37.6300),
                new DeliveryPoint("B2", 10, 5, 55.7540, 37.6310),
                new DeliveryPoint("B3", 10, 6, 55.7550, 37.6320),

                // critical boundary point
                new DeliveryPoint("X", 10, 7, 55.7525, 37.6240)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 2, 10);

        System.out.println("\nRESULT:");
        for (Cluster c : result) {
            GeoDistance.outPut(c);
        }
    }

    public static void testForcedThirdCluster() {

        System.out.println("==== TEST 3: FORCED THIRD CLUSTER ====");

        List<DeliveryPoint> points = List.of(

                // cluster A (dense)
                new DeliveryPoint("A1", 10, 1, 55.7510, 37.6180),
                new DeliveryPoint("A2", 10, 2, 55.7515, 37.6185),
                new DeliveryPoint("A3", 10, 3, 55.7520, 37.6190),
                new DeliveryPoint("A4", 10, 4, 55.7525, 37.6195),
                new DeliveryPoint("A5", 10, 5, 55.7530, 37.6200),

                // cluster B (dense)
                new DeliveryPoint("B1", 10, 6, 55.7600, 37.6500),
                new DeliveryPoint("B2", 10, 7, 55.7610, 37.6510),
                new DeliveryPoint("B3", 10, 8, 55.7620, 37.6520),
                new DeliveryPoint("B4", 10, 9, 55.7630, 37.6530),
                new DeliveryPoint("B5", 10, 10, 55.7640, 37.6540),

                // sparse south-west cluster (CRITICAL)
                new DeliveryPoint("C1", 10, 11, 55.6000, 37.3000),
                new DeliveryPoint("C2", 10, 12, 55.6100, 37.3100),
                new DeliveryPoint("C3", 10, 13, 55.6200, 37.3200)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 3, 10);

        System.out.println("\nRESULT:");
        for (Cluster c : result) {
            GeoDistance.outPut(c);
        }
    }

    public static void testOutliers() {

        System.out.println("==== TEST 4: OUTLIERS ====");

        List<DeliveryPoint> points = List.of(

                // main cluster
                new DeliveryPoint("C1",10,1,55.7510,37.6180),
                new DeliveryPoint("C2",10,2,55.7520,37.6190),
                new DeliveryPoint("C3",10,3,55.7530,37.6200),
                new DeliveryPoint("C4",10,4,55.7540,37.6210),

                // far outliers
                new DeliveryPoint("O1",10,5,55.4000,37.2000),
                new DeliveryPoint("O2",10,6,55.4100,37.2100),
                new DeliveryPoint("O3",10,7,55.4200,37.2200),

                new DeliveryPoint("O4",10,8,56.0000,37.5000)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 2, 10);

        System.out.println("\nRESULT:");
        for (Cluster c : result) {
            GeoDistance.outPut(c);
        }
    }

    public static void testCapacityStress() {

        System.out.println("==== TEST 5: CAPACITY STRESS ====");

        List<DeliveryPoint> points = new ArrayList<>();

        // 25 close points (must be split across 3 drivers)
        for (int i = 0; i < 25; i++) {
            points.add(new DeliveryPoint(
                    "P" + i,
                    10,
                    i,
                    55.75 + (i % 3) * 0.001,
                    37.60 + (i % 5) * 0.001
            ));
        }

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 3, 10);

        System.out.println("\nRESULT:");
        for (Cluster c : result) {
            GeoDistance.outPut(c);
        }
    }
}
*/
