/*
package automation.clustering.KMeansTests;

import automation.clustering.algorithm.GeoDistance;
import automation.clustering.model.DeliveryPoint;
import automation.clustering.model.Cluster;
import automation.clustering.algorithm.KMeansAlgorithm;

import java.util.List;

public class KMeansAlgorithmTests3 {
    public static void main(String[] args) {
//        testFourIsolatedDistricts();
//        testHighwayLine();
//        testOutlierDrop();
        testConcentricZones();
    }

    public static void testFourIsolatedDistricts() {
        List<DeliveryPoint> points = List.of(
                // Северо-Запад (Кластер 1)
                new DeliveryPoint("NW1", 10, 1, 55.8000, 37.4000),
                new DeliveryPoint("NW2", 10, 2, 55.8100, 37.4100),
                new DeliveryPoint("NW3", 10, 3, 55.7900, 37.3900),

                // Северо-Восток (Кластер 2)
                new DeliveryPoint("NE1", 10, 4, 55.8000, 37.7000),
                new DeliveryPoint("NE2", 10, 5, 55.8100, 37.7100),
                new DeliveryPoint("NE3", 10, 6, 55.7900, 37.6900),

                // Юго-Запад (Кластер 3)
                new DeliveryPoint("SW1", 10, 7, 55.6000, 37.4000),
                new DeliveryPoint("SW2", 10, 8, 55.6100, 37.4100),
                new DeliveryPoint("SW3", 10, 9, 55.5900, 37.3900),

                // Юго-Восток (Кластер 4)
                new DeliveryPoint("SE1", 10, 10, 55.6000, 37.7000),
                new DeliveryPoint("SE2", 10, 11, 55.6100, 37.7100),
                new DeliveryPoint("SE3", 10, 12, 55.5900, 37.6900)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 4, 10);

        System.out.println("\nResult for Four Isolated Districts:");
        for (Cluster cluster : result) GeoDistance.outPut(cluster);
    }


    public static void testHighwayLine() {
        List<DeliveryPoint> points = List.of(
                // Начало шоссе (Ближе к центру)
                new DeliveryPoint("H1", 10, 1, 55.7500, 37.6000),
                new DeliveryPoint("H2", 10, 2, 55.7600, 37.6300),
                new DeliveryPoint("H3", 10, 3, 55.7700, 37.6600),

                // Середина шоссе (МКАД)
                new DeliveryPoint("H4", 10, 4, 55.7900, 37.7200),
                new DeliveryPoint("H5", 10, 5, 55.8000, 37.7500),
                new DeliveryPoint("H6", 10, 6, 55.8100, 37.7800),

                // Конец шоссе (Область)
                new DeliveryPoint("H7", 10, 7, 55.8300, 37.8400),
                new DeliveryPoint("H8", 10, 8, 55.8400, 37.8700),
                new DeliveryPoint("H9", 10, 9, 55.8500, 37.9000)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 3, 10);

        System.out.println("\nResult for Highway Line:");
        for (Cluster cluster : result) GeoDistance.outPut(cluster);
    }

    public static void testOutlierDrop() {
        List<DeliveryPoint> points = List.of(
                // Город А (Плотный восток)
                new DeliveryPoint("A1", 10, 1, 55.7500, 37.8000),
                new DeliveryPoint("A2", 10, 2, 55.7510, 37.8020),
                new DeliveryPoint("A3", 10, 3, 55.7490, 37.7980),

                // Город Б (Плотный запад)
                new DeliveryPoint("B1", 10, 4, 55.7500, 37.4000),
                new DeliveryPoint("B2", 10, 5, 55.7520, 37.4010),
                new DeliveryPoint("B3", 10, 6, 55.7480, 37.3990),

                // Одинокий заказ глубоко в области (Выброс)
                new DeliveryPoint("FarOut", 10, 7, 55.9500, 38.3000)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        // Делим на 2 водителя
        List<Cluster> result = algorithm.cluster(points, 2, 10);

        System.out.println("\nResult for Outlier Drop:");
        for (Cluster cluster : result) GeoDistance.outPut(cluster);
    }

    public static void testConcentricZones() {
        List<DeliveryPoint> points = List.of(
                // Маленький плотный Центр (Кластер 1)
                new DeliveryPoint("Center1", 10, 1, 55.7500, 37.6100),
                new DeliveryPoint("Center2", 10, 2, 55.7510, 37.6110),
                new DeliveryPoint("Center3", 10, 3, 55.7490, 37.6090),

                // Полукольцо вокруг центра с Севера и Востока (Кластер 2)
                new DeliveryPoint("RingN1", 10, 4, 55.7900, 37.6100),
                new DeliveryPoint("RingNE", 10, 5, 55.7800, 37.6500),
                new DeliveryPoint("RingE1", 10, 6, 55.7500, 37.6800)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 2, 10);

        System.out.println("\nResult for Concentric Zones:");
        for (Cluster cluster : result) GeoDistance.outPut(cluster);
    }
}
*/
