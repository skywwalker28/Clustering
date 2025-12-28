package automation.clustering.optimization;

import automation.clustering.excel.ExcelExporter;
import automation.clustering.excel.ExcelReader;
import automation.clustering.geocoding.OpenRouteGeocoder;
import automation.clustering.map.RouteMapExporter;
import automation.clustering.model.DeliveryPoint;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class RouteOptimizationService {

    public static final String API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjRlOD" +
          "I1N2ZkOGU1YzRmZjdiMjgxNTJhYWViZjFkZDY2IiwiaCI6Im11cm11cjY0In0=";

    private static final int MAX_POINTS_PER_DRIVER = 10;
    private static final int MAX_VEHICLES = 5;

    private static final int MAX_WEIGHT = 550;
    private static final double BMM_LAT = 55.592605;
    private static final double BMM_LON = 37.747183;

    public void optimizeAndDisplayRoutes() {
        try {
            List<DeliveryPoint> points =
                    ExcelReader.readDeliveryPointsFromExcel("/Users/skywalker/Downloads/ИМТЭК 22.12.2025.xlsx");

            List<String> addresses = points.stream()
                    .map(DeliveryPoint::getAddress)
                    .toList();

            List<Integer> weights = points.stream()
                    .map(DeliveryPoint::getWeightKg)
                    .toList();


            List<double[]> coordinates = OpenRouteGeocoder.geocodingAddresses(addresses);
            if (coordinates.isEmpty()) {
                System.err.println("Error: No coordinates available!");
                return;
            }

            double[][] parseCoordinates = new double[coordinates.size()][2];
            for (int i = 0; i < coordinates.size(); i++) {
                parseCoordinates[i][0] = coordinates.get(i)[0];
                parseCoordinates[i][1] = coordinates.get(i)[1];
            }

            String requestJson = buildORSOptimizationJson(coordinates, weights);
            String response = sendORSRequest(requestJson);

            Map<Integer, List<double[]>> routes =
                    OptimizationResponse.parseOptimizationResponse(response, parseCoordinates);

            Map<Integer, List<Integer>> driverWeights = getIntegerListMap(routes, coordinates, weights);

            Map<Integer, List<String>> driverAddresses = mapAddressesToRoutes(routes, addresses, coordinates);


            RouteMapExporter.exportHtmlMap(routes, driverAddresses, "routes_map.html");

            System.out.println("Всего требуется водителей: " + routes.size());

            int totalPoints = 0;
            for (Map.Entry<Integer, List<double[]>> entry : routes.entrySet()) {
                int driverNum = entry.getKey() + 1;
                List<double[]> route = entry.getValue();

                totalPoints += route.size();
                System.out.println("\nDriver " + driverNum + ": " + route.size() + " points");

                List<String> addressesForDriver = driverAddresses.get(entry.getKey());
                for (int i = 0; i < addressesForDriver.size(); i++) {
                    System.out.println(" " + (i + 1) + ". " + addressesForDriver.get(i));
                }
            }

            System.out.println("\nВсего распределенно точек: " + totalPoints + "/" + coordinates.size());

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            ExcelExporter.exportToExcelSingleSheet(routes, driverAddresses, driverWeights,
                    "optimized_routes_" + timestamp + ".xlsx");

        } catch (Exception e) {
            System.err.println("Error in RouteOptimizationService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Map<Integer, List<Integer>> getIntegerListMap(Map<Integer, List<double[]>> routes,
                                                                 List<double[]> coordinates, List<Integer> weights) {
        Map<Integer, List<Integer>> driverWeights = new HashMap<>();
        for (Map.Entry<Integer, List<double[]>> entry : routes.entrySet()) {
            int driverId = entry.getKey();
            List<double[]> routePoints = entry.getValue();
            List<Integer> weightsForDriver = new ArrayList<>();

            for (double[] point : routePoints) {
                int index = -1;
                for (int i = 0; i < coordinates.size(); i++) {
                    if (coordinates.get(i)[0] == point[0] && coordinates.get(i)[1] == point[1]) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    weightsForDriver.add(weights.get(index));
                } else {
                    weightsForDriver.add(0);
                }
            }

            driverWeights.put(driverId, weightsForDriver);
        }
        return driverWeights;
    }

    private String buildORSOptimizationJson(List<double[]> coordinates, List<Integer> weights) {
        int neededVehicles =
                Math.min((int) Math.ceil((double) coordinates.size() / MAX_POINTS_PER_DRIVER), MAX_VEHICLES);

        StringBuilder jobs = new StringBuilder();
        for (int i = 0; i < coordinates.size(); i++) {
           double[] c = coordinates.get(i);
           int weight = weights.get(i);

            jobs.append("""
                {
                  "id": %d,
                  "location": [%f, %f],
                  "amount": [%d, 1]
                }
                """.formatted(
                    i,
                    c[1], c[0],
                    weight
            ));

            if (i < coordinates.size() - 1) jobs.append(",");
        }

        StringBuilder vehicles = new StringBuilder();
        for (int i = 0; i < neededVehicles; i++) {
            vehicles.append("""
                {
                  "id": %d,
                  "start": [%f, %f],
                  "capacity": [%d, %d],
                  "profile": "driving-car"
                }
                """.formatted(
                    i,
                    BMM_LON, BMM_LAT,
                    MAX_WEIGHT,
                    MAX_POINTS_PER_DRIVER
            ));

            if (i < neededVehicles - 1) vehicles.append(",");
        }

        return """
        {
          "jobs": [%s],
          "vehicles": [%s]
        }
        """.formatted(jobs, vehicles);
    }


    private String sendORSRequest(String json) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openrouteservice.org/optimization"))
                .header("Authorization", API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("ORS API returned code " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    private static Map<Integer, List<String>> mapAddressesToRoutes(
            Map<Integer, List<double[]>> driverRoutes,
            List<String> originalAddresses,
            List<double[]> allPoints
    ) {
        Map<Integer, List<String>> result = new HashMap<>();
        Map<String, String> coordToAddress = new HashMap<>();
        for (int i = 0; i < allPoints.size(); i++) {
            String key = String.format("%.6f,%.6f", allPoints.get(i)[0], allPoints.get(i)[1]);
            coordToAddress.put(key, originalAddresses.get(i));
        }

        for (Map.Entry<Integer, List<double[]>> entry : driverRoutes.entrySet()) {
            List<String> driverAddresses = new ArrayList<>();
            for (double[] point : entry.getValue()) {
                String key = String.format("%.6f,%.6f", point[0], point[1]);
                driverAddresses.add(coordToAddress.getOrDefault(key, String.format("[%.6f, %.6f]", point[0], point[1])));
            }
            result.put(entry.getKey(), driverAddresses);
        }
        return result;
    }
}
