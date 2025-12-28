package automation.clustering.optimization;

import automation.clustering.json.BuildORS;
import automation.clustering.excel.ExcelExporter;
import automation.clustering.excel.ExcelReader;
import automation.clustering.geocoding.OpenRouteGeocoder;
import automation.clustering.map.RouteMapExporter;
import automation.clustering.model.DeliveryPoint;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.*;
import static automation.clustering.json.BuildORS.sendORSRequest;

@Service
public class RouteOptimizationService {

    public static final String API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjRlOD" +
          "I1N2ZkOGU1YzRmZjdiMjgxNTJhYWViZjFkZDY2IiwiaCI6Im11cm11cjY0In0=";

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

            String requestJson = BuildORS.buildORSOptimizationJson(coordinates, weights);
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
