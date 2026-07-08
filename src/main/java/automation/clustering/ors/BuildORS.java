package automation.clustering.ors;

import automation.clustering.model.DeliveryPoint;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;

import static automation.clustering.main.RouteOptimizationStart.dotenv;
import static automation.clustering.ors.CreateDrivers.getStringBuilder;
import static automation.clustering.ors.CreateDrivers.getStringBuilderBMM;


public class BuildORS {
    static final int MAX_POINTS_PER_DRIVER = 8;
    public static final String API_KEY = dotenv.get("API_ORS");

    @Setter
    private static HttpClient httpClient = HttpClient.newHttpClient();


    public static String buildORSOptimizationJson(List<double[]> coordinates, List<DeliveryPoint> weights) {

        int neededVehicles = Math.min((int) Math.ceil((double) coordinates.size() / MAX_POINTS_PER_DRIVER), 3);
        System.out.println("neededVehicle: " + neededVehicles);

        StringBuilder jobs = getStringBuilder(coordinates, weights);
        StringBuilder vehicles = getStringBuilderBMM();

        return """
        {
          "jobs": [%s],
          "vehicles": [%s]
        }
        """.formatted(jobs, vehicles);
    }

    public static String sendORSRequest(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openrouteservice.org/optimization"))
                .header("Authorization", API_KEY)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("ORS API returned code " + response.statusCode() + ": " + response.body());
            }
            return response.body();
        } catch (HttpTimeoutException e) {
            System.out.println("15 секунд не можем достучатся до ors сервиса");
            return null;
        }
    }
}
