package automation.clustering.distance;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static automation.clustering.distance.CreateStringBuilder.createJsonBuilder;
import static automation.clustering.distance.Calculate.calculateApproximateDistance;
import static automation.clustering.optimization.RouteOptimizationService.API_KEY;

public class OrsDistance {

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static double[] getRouteDistanceFromAPI(List<double[]> points) throws Exception {
        if (points.size() < 2) return new double[]{0.0, 0.0};

        String jsonRequest = createJsonBuilder(points);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openrouteservice.org/v2/directions/driving-car"))
                .header("Content-Type", "application/json")
                .header("Authorization", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println(response.body() + "\nStart artificial calculating...");
            return calculateApproximateDistance(points);
        }

        return parseDistanceResponse(response.body(), points);
    }

    public static double[] parseDistanceResponse(String json, List<double[]> points) {
        double[] result = new double[2];
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        JsonArray routes = root.getAsJsonArray("routes");
        if (routes.isEmpty()) {
            System.err.println("'Routes' in distance is empty. Start artificial calculating...");
            return calculateApproximateDistance(points);
        }

        JsonObject firstRoute = routes.get(0).getAsJsonObject();
        JsonObject summary = firstRoute.getAsJsonObject("summary");

        if (summary == null) {
            System.err.println("'Summary' not found in route. Start artificial calculating...");
            return calculateApproximateDistance(points);
        }

        result[0] = summary.get("distance").getAsDouble();
        result[1] = summary.get("duration").getAsDouble()/60;

        return result;
    }
}
