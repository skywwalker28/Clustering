/*
package automation.clustering.KMeansTests;

import automation.clustering.algorithm.GeoDistance;
import automation.clustering.model.DeliveryPoint;
import automation.clustering.model.Cluster;
import automation.clustering.algorithm.KMeansAlgorithm;

import java.util.ArrayList;
import java.util.List;

public class KMeansAlgorithmTests {
    public static void main(String[] args) {
//        test4Points();
//        test5Points();
//        test20Points();
//        testRealRegistry7Points();
//        testRealRegistry26Points();
//        testRealRegistry25Points();
//        testPingPongBoundary7Points();
//        testCrossSwap6Points();
//        testOutlierLock7Points();
    }

    public static void test4Points() {
        System.out.println("____TEST 1____");
        List<DeliveryPoint> points = List.of(
                new DeliveryPoint("Запад-1", 10, 1, 55.7500, 37.5000),
                new DeliveryPoint("Запад-2", 10, 2, 55.7510, 37.5010),
                new DeliveryPoint("Восток-1", 10, 3, 55.7500, 37.7000),
                new DeliveryPoint("Восток-2", 10, 4, 55.7510, 37.7010)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 2, 2);

        for (Cluster cluster : result) {
            GeoDistance.outPut(cluster);
        }

        boolean success = result.stream().allMatch(c -> c.getPoints().size() == 2);
        System.out.println("Result: " + success + "\n");
    }

    public static void test5Points() {
        System.out.println("____TEST 2____");
        List<DeliveryPoint> points = List.of(
                new DeliveryPoint("Север-Центр", 10, 1, 55.8500, 37.6000),
                new DeliveryPoint("Север-Запад", 10, 2, 55.8600, 37.5900),
                new DeliveryPoint("Север-Восток", 10, 3, 55.8600, 37.6100),

                // Северо-Юг (посередине)
                new DeliveryPoint("Север-Южный-Край", 10, 4, 55.8000, 37.6000),

                // Одинокая точка на Юге
                new DeliveryPoint("Юг-Одинокая", 10, 5, 55.6500, 37.6000)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 2, 3);

        for (Cluster cluster : result) {
            GeoDistance.outPut(cluster);
        }

        boolean success = result.stream().allMatch(c -> c.getPoints().size() <= 3);
        System.out.println("Result: " + success + "\n");
    }

    public static void test20Points() {
        System.out.println("____TEST 3___");
        List<DeliveryPoint> points = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            points.add(
                    new DeliveryPoint("Точка-" + i, 15, i, 55.6000 + (i * 0.01), 37.6000));

        }

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 5, 4);

        for (Cluster cluster : result) {
            GeoDistance.outPut(cluster);
        }

        boolean success = result.stream().allMatch(c -> c.getPoints().size() == 4);
        System.out.println("Result: " + success + "\n");
    }

    public static void testRealRegistry7Points() {
        System.out.println("____TEST 4____");
        List<DeliveryPoint> points = List.of(
                new DeliveryPoint("1: Стрельбище Динамо 10", 12, 1, 55.949315, 37.751614),
                new DeliveryPoint("2: Большая Бронная 2/6", 30, 2, 55.759239, 37.600109),
                new DeliveryPoint("3: Ходынский бульвар 4а", 40, 3, 55.790403, 37.531273),
                new DeliveryPoint("4: МКАД 87 км, д 8", 220, 4, 55.910556, 37.618664),
                new DeliveryPoint("5: Воскресенск, Новлянская 10", 100, 5, 55.321151, 38.653696),
                new DeliveryPoint("6: Егорьевск, Майора Удачина 8", 120, 6, 55.385315, 39.011613),
                new DeliveryPoint("7: 1-я Тверская-Ямская 16/23", 100, 7, 55.772591, 37.594739)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 2, 10);

        for (Cluster cluster : result) {
            GeoDistance.outPut(cluster);
        }

        boolean success = result.stream().allMatch(c -> c.getPoints().size() >= 2 &&
                c.getPoints().size() <= 5);
        System.out.println("Result: " + success + "\n");
    }

    public static void testRealRegistry26Points() {
        System.out.println("____TEST 5____");
        List<DeliveryPoint> points = List.of(
                new DeliveryPoint("1.", 10, 1, 55.706915, 37.592186),
                new DeliveryPoint("2.", 10, 2, 55.823485, 37.327660),
                new DeliveryPoint("3.", 10, 3, 55.903848, 37.387114),
                new DeliveryPoint("4.", 10, 4, 55.774431, 37.644175),
                new DeliveryPoint("5. ", 10, 5, 55.760167, 37.771960),
                new DeliveryPoint("7.", 10, 7, 55.759239, 37.600109),
                new DeliveryPoint("8.", 10, 8, 55.793318, 37.601556),
                new DeliveryPoint("9.", 10, 9, 55.777931, 37.634123),
                new DeliveryPoint("10.", 10, 10, 55.834012, 37.280145),
                new DeliveryPoint("11.", 10, 11, 55.992850, 37.195044),
                new DeliveryPoint("12.", 10, 12, 55.981881, 37.161042),
                new DeliveryPoint("13.", 10, 13, 55.742914, 37.555981),
                new DeliveryPoint("14.", 10, 14, 55.756201, 37.675662),
                new DeliveryPoint("15.", 10, 15, 55.717646, 37.423984),
                new DeliveryPoint("16.", 10, 16, 55.786524, 37.551390),
                new DeliveryPoint("17.", 10, 17, 55.642431, 37.100994),
                new DeliveryPoint("18.", 10, 18, 55.702951, 37.768652),
                new DeliveryPoint("19.", 10, 19, 55.708170, 37.654877),
                new DeliveryPoint("20.", 10, 20, 55.545831, 37.722002),
                new DeliveryPoint("21.", 10, 21, 55.714552, 37.731215),
                new DeliveryPoint("22.", 10, 22, 55.693142, 37.561490),
                new DeliveryPoint("23.", 10, 23, 55.770954, 37.540121),
                new DeliveryPoint("24.", 10, 24, 55.752312, 37.948512),
                new DeliveryPoint("25.", 10, 25, 55.746124, 37.784512),
                new DeliveryPoint("26.", 10, 26, 55.564512, 38.226845),
                new DeliveryPoint("27.", 10, 27, 55.938121, 37.498451)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 4, 10);

        for (Cluster cluster : result) {
            GeoDistance.outPut(cluster);
        }

        boolean success = result.stream().allMatch(c -> c.getPoints().size() <= 10);
        System.out.println("Result: " + success + "\n");
    }

    public static void testRealRegistry25Points() {
        System.out.println("____TEST 6____");
        List<DeliveryPoint> points = List.of(
                new DeliveryPoint("1.", 10, 1, 55.702951, 37.768652),
                new DeliveryPoint("2.", 10, 2, 55.776634, 37.607421),
                new DeliveryPoint("3.", 10, 3, 55.741029, 37.628172),
                new DeliveryPoint("4.", 10, 4, 55.756201, 37.675662),
                new DeliveryPoint("5.", 10, 5, 55.708170, 37.654877),
                new DeliveryPoint("6.", 10, 6, 55.694602, 37.595914),
                new DeliveryPoint("7.", 10, 7, 55.714552, 37.731215),
                new DeliveryPoint("8.", 10, 8, 55.836066, 37.161021),
                new DeliveryPoint("9.", 10, 9, 56.012542, 37.478412),
                new DeliveryPoint("10.", 10, 10, 55.821312, 37.319842),
                new DeliveryPoint("11.", 10, 11, 55.845112, 37.185211),
                new DeliveryPoint("12.", 10, 12, 55.626845, 37.509412),
                new DeliveryPoint("13.", 10, 13, 55.752312, 37.948512),
                new DeliveryPoint("14.", 10, 14, 55.692121, 37.662145),
                new DeliveryPoint("15.", 10, 15, 55.698451, 37.312145),
                new DeliveryPoint("16.", 10, 16, 55.669841, 37.284512),
                new DeliveryPoint("17.", 10, 17, 55.793318, 37.601556),
                new DeliveryPoint("18.", 10, 18, 55.395121, 36.741215),
                new DeliveryPoint("19.", 10, 19, 55.603121, 37.534512),
                new DeliveryPoint("20.", 10, 20, 55.823485, 37.327660),
                new DeliveryPoint("21.", 10, 21, 55.691245, 37.728451),
                new DeliveryPoint("22.", 10, 22, 55.614512, 36.984512),
                new DeliveryPoint("23.", 10, 23, 55.594512, 37.041215),
                new DeliveryPoint("24.", 10, 24, 55.821451, 37.568451),
                new DeliveryPoint("25.", 10, 25, 55.791245, 37.551241)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 3, 10);

        System.out.println("\nResult: ");
        for (Cluster cluster : result) {
            GeoDistance.outPut(cluster);
        }

        boolean success = result.stream().allMatch(c -> c.getPoints().size() <= 10);
        System.out.println("Result: " + success + "\n");
    }

    public static void testPingPongBoundary7Points() {
        System.out.println("____TEST 7____");
        List<DeliveryPoint> points = List.of(
                // cluster A (центр Москва)
                new DeliveryPoint("A1", 10, 1, 55.751244, 37.618423),
                new DeliveryPoint("A2", 10, 2, 55.752000, 37.620000),
                new DeliveryPoint("A3", 10, 3, 55.750500, 37.619000),

                // cluster B (чуть восточнее)
                new DeliveryPoint("B1", 10, 4, 55.760000, 37.650000),
                new DeliveryPoint("B2", 10, 5, 55.761000, 37.651000),
                new DeliveryPoint("B3", 10, 6, 55.762000, 37.652000),

                // пограничная точка (пограничная)
                new DeliveryPoint("X", 10, 7, 55.756000, 37.635000)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 2, 10);

        System.out.println("\nResult:");
        for (Cluster cluster : result) {
            GeoDistance.outPut(cluster);
        }
    }

    public static void testCrossSwap6Points() {
        System.out.println("____TEST 8____");

        List<DeliveryPoint> points = List.of(
                // Cluster A (лево)
                new DeliveryPoint("A1", 10, 1, 55.750000, 37.600000),
                new DeliveryPoint("A2", 10, 2, 55.751000, 37.601000),

                // Cluster B (право)
                new DeliveryPoint("B1", 10, 3, 55.750000, 37.700000),
                new DeliveryPoint("B2", 10, 4, 55.751000, 37.701000),

                // Две “перекрёстные” точки
                new DeliveryPoint("X1", 10, 5, 55.750500, 37.650000),
                new DeliveryPoint("X2", 10, 6, 55.750600, 37.651000)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 2, 10);

        System.out.println("\nResult: ");
        for (Cluster cluster : result) {
            GeoDistance.outPut(cluster);
        }
    }

    public static void testOutlierLock7Points() {
        List<DeliveryPoint> points = List.of(
                // плотный кластер Москва
                new DeliveryPoint("1", 10, 1, 55.751244, 37.618423),
                new DeliveryPoint("2", 10, 2, 55.752000, 37.619000),
                new DeliveryPoint("3", 10, 3, 55.753000, 37.620000),
                new DeliveryPoint("4", 10, 4, 55.750000, 37.617000),

                // периферия
                new DeliveryPoint("5", 10, 5, 55.600000, 37.400000),
                new DeliveryPoint("6", 10, 6, 55.610000, 37.410000),

                // выброс
                new DeliveryPoint("OUT", 10, 7, 55.400000, 37.200000)
        );

        KMeansAlgorithm algorithm = new KMeansAlgorithm();
        List<Cluster> result = algorithm.cluster(points, 2, 10);

        System.out.println("\nResult: ");
        for (Cluster cluster : result) {
            GeoDistance.outPut(cluster);
        }
    }
}
*/
