package automation.clustering.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptimizationResponse {
    public static Map<Integer, List<double[]>> parseOptimizationResponse(String response,
                                                                         double[][] allPoints) {
        Map<Integer, List<double[]>> driverRoutes = new HashMap<>();

        try {
            System.out.println("Parsing response from APi...");

            JsonNode root = new ObjectMapper().readTree(response);
            JsonNode routes = root.path("routes");

           if (!routes.isArray()) {
               System.out.println("Response does not contain 'routes' array");
               return driverRoutes;
           }

            for (JsonNode route : routes) {
                int vehicleId = route.path("vehicle").asInt();

                JsonNode steps = route.path("steps");
                List<double[]> driverPoints = new ArrayList<>();

                if (steps.isArray()) {
                    for (JsonNode step : steps) {
                        if (step.has("job")) {
                            int jobId = step.path("job").asInt();

                            if (jobId >= 0 && jobId < allPoints.length) {
                                driverPoints.add(allPoints[jobId]);
                            } else {
                                System.err.println("Warning Invalid job ID " + jobId + " for vehicle " + vehicleId);
                            }
                        }
                    }
                }

                if (!driverPoints.isEmpty()) {
                    driverRoutes.put(vehicleId, driverPoints);
                    System.out.println("Vehicle " + vehicleId + " has " + driverPoints.size() + " points");
                } else {
                    System.out.println(" Vehicle " + vehicleId + " has no job points");
                }
            }

            System.out.println("Successfully parsed " + driverRoutes.size() + " response routes.");
        } catch (Exception e) {
            System.err.println("Error parsing response with Jackson: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Raw answer snippet: " + response.substring(0,
                    Math.min(500, response.length())) + "...");
        }

        return driverRoutes;
    }
}
