package automation.clustering.json;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static automation.clustering.optimization.RouteOptimizationService.API_KEY;

public class BuildORS {
    private static final int MAX_POINTS_PER_DRIVER = 10;
    private static final int MAX_VEHICLES = 5;

    private static final int MAX_WEIGHT = 550;
    private static final double BMM_LAT = 55.592605;
    private static final double BMM_LON = 37.747183;

    public static String buildORSOptimizationJson(List<double[]> coordinates, List<Integer> weights) {
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

    public static String sendORSRequest(String json) throws Exception {
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

}
