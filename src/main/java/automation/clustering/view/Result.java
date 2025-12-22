package automation.clustering.view;

import automation.clustering.distance.RouteDistance;

import java.util.List;
import java.util.Map;

public class Result {
    public static void printResult(Map<Integer, List<double[]>> driverRoutes) {
        System.out.println("\n=== DISTRIBUTION RESULTS ===");

        if (driverRoutes == null || driverRoutes.isEmpty()) {
            System.out.println("ERROR: No driver routes found!");
            return;
        }

        int totalPoints = 0;
        for (Map.Entry<Integer, List<double[]>> entry : driverRoutes.entrySet()) {
            int vehicleId = entry.getKey();
            int driverId = entry.getKey() + 1;
            int pointCount = entry.getValue().size();
            totalPoints += pointCount;

            System.out.printf("\nDriver %d (Vehicle ID %d): %d points%n", driverId, vehicleId, pointCount);

            if (pointCount > 0) {
                System.out.println("First 3 points: ");
                for (int i = 0; i < Math.min(3, pointCount); i++) {
                    double[] point = entry.getValue().get(i);
                    System.out.printf("[%.6f, %.6f] ", point[0], point[1]);
                }

                if (pointCount > 3) System.out.println("...");
                System.out.println();
            }
        }

        System.out.printf("\n Total distributed: %d points%n", totalPoints);

        if (totalPoints < 30) {
            System.out.println("Error! Not all points are distributed");
        } else if (totalPoints > 30) {
            System.out.println("Warning: More points distributed then expected!");
        } else {
            System.out.println("Perfect! All 30 points distributed.");
        }
    }

    public static void printDriverRoute(int driverId, List<double[]> points) {
        if (points.size() < 2) {
            System.out.printf("\nDrover %d: only %d point, not route required%n", driverId, points.size());
            return;
        }

        System.out.printf("\n--- Driver %d (%d point) ---%n", driverId + 1, points.size());

        double totalDistance = RouteDistance.getRouteDistance(points);


        System.out.printf("Total distance: %.2f km%n", totalDistance / 1000.0);

        System.out.println("Visiting order:");
        for (int i = 0; i < points.size(); i++) {
            System.out.printf("    %d. [%.6f, %.6f]%n", i + 1, points.get(i)[0], points.get(i)[1]);
        }
    }
}
