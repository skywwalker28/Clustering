package automation.clustering.generateRandom;

import java.util.Random;

public class GenerateRandomPointsToMoscow {
    public static double[][] generate30PointsToMoscow() {
        double[][] points = new double[30][2];
        Random random = new Random();

        double centerLat = 55.7558;
        double centerLon = 37.6173;

        for (int i = 0; i < 30; i++) {
            double latOffset = (random.nextDouble() - 0.5) * 0.18;
            double lonOffset = (random.nextDouble() - 0.5) * 0.36;

            points[i][0] = centerLat + latOffset;
            points[i][1] = centerLon + lonOffset;

            points[i][0] = Math.max(55.55, Math.min(55.95, points[i][0]));
            points[i][1] = Math.max(37.35, Math.min(37.85, points[i][1]));
        }

        points[0] = new double[]{55.7558, 37.6173};
        points[1] = new double[]{55.7516, 37.6185};
        points[2] = new double[]{55.7495, 37.6214};
        points[3] = new double[]{55.7605, 37.6186};
        points[4] = new double[]{55.7320, 37.6030};

        System.out.println("Sample of generated points:");
        for (int i = 0; i < Math.min(5, points.length); i++) {
            System.out.printf("  Point %d: [%.6f, %.6f]%n", i, points[i][0], points[i][1]);
        }

        return points;
    }
}
