package automation.clustering.optimization;

import automation.clustering.excel.ExcelExporter;
import automation.clustering.geocoding.OpenRouteGeocoder;
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

    private static final int MAX_POINTS_PER_DRIVER = 7;
    private static final int MAX_VEHICLES = 5;

    private static final double BMM_LAT = 55.592605;
    private static final double BMM_LON = 37.747183;

    public void optimizeAndDisplayRoutes() {
        try {
            List<String> addresses = Arrays.asList(
                    "г. Пушкино, Московский проспект 2",
                    "Московская обл, Долгопрудный, Пацаева 12.",
                    "г. Химки, мкр. Сходня улица Кирова 3с2",
                    "г Москва 2-я Владимирская 38/18",
                    "г.Зеленоград, пл. Привокзальная, д. 1, стр. 5",
                    "г. Красногорск, м-н Опалиха, ул. Опалиха 2",
                    "Московская область, Красногорск, Красногорский бульвар, 18",
                    "Вокзальная улица, 20/1",
                    "Институтская 26, Нахабино",
                    "Московская область,Новлянская улица, 10",
                    "г. Балашиха, мкр Железнодорожный, Проспект героев дом 5",
                    "Бронницы, ул. Красная 26",
                    "Московская область, г. Электросталь, г. Электросталь, ул Ялагина, д. 3",
                    "Мкр.Железнодорожный, Балашиха, Советская улица",
                    "улица Майора Удачина"
            );

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

            String requestJson = buildORSOptimizationJson(coordinates);

            String response = sendORSRequest(requestJson);

            Map<Integer, List<double[]>> routes =
                    OptimizationResponse.parseOptimizationResponse(response, parseCoordinates);

            Map<Integer, List<String>> driverAddresses = mapAddressesToRoutes(routes, addresses, coordinates);

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
            String fileName = "optimized_routes_" + timestamp + ".xlsx";
            ExcelExporter.exportToExcelSingleSheet(routes, driverAddresses, fileName);
            System.out.println("\n✅ Excel report created: " + fileName);

        } catch (Exception e) {
            System.err.println("Error in RouteOptimizationService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildORSOptimizationJson(List<double[]> coordinates) {
        int neededVehicles = Math.min(coordinates.size() / MAX_POINTS_PER_DRIVER + 1, MAX_VEHICLES);

        StringBuilder jobs = new StringBuilder();
        for (int i = 0; i < coordinates.size(); i++) {
            double[] coord = coordinates.get(i);
            jobs.append("{\"id\":").append(i)
                    .append(",\"location\":[").append(coord[1]).append(",").append(coord[0]).append("]}");
            if (i < coordinates.size() - 1) jobs.append(",");
        }

        StringBuilder vehicles = new StringBuilder();
        for (int i = 0; i < neededVehicles; i++) {
            vehicles.append("{\"id\":").append(i)
                    .append(",\"start\":[").append(BMM_LON).append(",").append(BMM_LAT).append("],")
                    .append("\"profile\":\"driving-car\"}");
            if (i < neededVehicles - 1) vehicles.append(",");
        }

        return """
        {
          "jobs": [%s],
          "vehicles": [%s]
        }
        """.formatted(jobs.toString(), vehicles.toString());
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
