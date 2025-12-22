package automation.clustering.optimization;

import automation.clustering.excel.ExcelExporter;
import automation.clustering.generateRandom.GenerateRandomPointsToMoscow;
import automation.clustering.view.Result;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RouteOptimizationService {

    public void optimizeAndDisplayRoutes() throws Exception {

        try {
            System.out.println("\n=== ROUTE OPTIMIZATION FOR 3 DRIVERS ===");


            double[][] points = GenerateRandomPointsToMoscow.generate30PointsToMoscow();
            System.out.println("Generate " + points.length + " points to Moscow");

            String url = "https://api.openrouteservice.org/optimization";
            System.out.println("Using endpoint: " + url);

            String json = OptimizationJson.buildOptimizationJson(points, 3);
            System.out.println("JSON payload preview: " + json.substring(0, Math.min(100, json.length())) + "...");

            String response = OptimizationJson.sendPostRequest(url, json);

            Map<Integer, List<double[]>> driverRoutes =
                    OptimizationResponse.parseOptimizationResponse(response, points);

            Result.printResult(driverRoutes);

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new java.util.Date());
            String fileName = "route_optimization_" + timestamp + ".xlsx";

            try {
                ExcelExporter.exportToExcel(driverRoutes, fileName);
                System.out.println("Excel report created: " + fileName);
                System.out.println("Sheet created:");
                System.out.println(" - Summary: Overview of all drivers");
                for (int i = 0; i < 3; i++) {
                    if (driverRoutes.containsKey(i)) {
                        System.out.println(" - Driver " + (i + 1) + ": Detailed route with distances");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error creating Excel file: " + e.getMessage());
                System.out.println("Falling back to console output...");

                System.out.println("\n=== DETAILED ROUTE (Console Fallback) ===");
                for (int driverId = 0; driverId < 3; driverId++) {
                    if (driverRoutes.containsKey(driverId)) {
                        try {
                            Result.printDriverRoute(driverId, driverRoutes.get(driverId));
                        } catch (Exception e1) {
                            System.out.println("Error printing route for driver " + driverId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in optimization service: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
