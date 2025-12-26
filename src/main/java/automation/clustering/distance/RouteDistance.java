package automation.clustering.distance;

import automation.clustering.optimization.RouteOptimizationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RouteDistance {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static double getRouteDistanceFromAPI(List<double[]> points) throws Exception {
        if (points.size() < 2) return 0;

        StringBuilder jsonBody = new StringBuilder();
        jsonBody.append("{\"coordinates\":[");

        for (int i = 0; i < points.size(); i++) {
            jsonBody.append("[").append(points.get(i)[1]).append(",").append(points.get(i)[0]).append("]");
            if (i < points.size() - 1) jsonBody.append(",");
        }

        jsonBody.append("],\"instructions\":\"false\"}");

        String url = "https://api.openrouteservice.org/v2/directions/driving-car";

        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", RouteOptimizationService.API_KEY);
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        con.setConnectTimeout(5000);
        con.setReadTimeout(30000);


        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String jsonResponse = response.toString();

            return parseDistanceWithJackson(jsonResponse);
        } else {
            BufferedReader err = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder error = new StringBuilder();
            String line;

            while ((line = err.readLine()) != null) {
                error.append(line);
            }

            err.close();


            if (responseCode == 406 && points.size() > 2) {
                return calculateApproximateDistance(points);
            }

            return 0;
        }
    }



    public static double parseDistanceWithJackson(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode routes = root.path("routes");

            if (routes.isArray() && !routes.isEmpty()) {
                JsonNode firstRoute = routes.get(0);
                JsonNode summary = firstRoute.path("summary");

                if (!summary.isMissingNode()) {
                    return summary.path("distance").asDouble();
                }

                JsonNode segments = firstRoute.path("segments");
                if (segments.isArray() && !segments.isEmpty()) {
                    double totalDistance = 0;
                    for (JsonNode segment : segments) {
                        totalDistance += segment.path("distance").asDouble();
                    }

                    return totalDistance;
                }
            }

            System.err.println("Could not find distance in response");
        } catch (Exception e) {
            System.err.println("Error parsing distance with Jackson: " + e.getMessage());
        }

        return 0;
    }

    public static double calculateApproximateDistance(List<double[]> points) {
        double totalDistance = 0;

        for (int i = 0; i < points.size() - 1; i++) {
            double segmentDistance = calculateHaversineDistance(
                    points.get(i),
                    points.get(i + 1)
            );
            totalDistance += segmentDistance;

        }

        return totalDistance;
    }

    public static double calculateHaversineDistance(double[] point1, double[] point2) {
        double lat1 = Math.toRadians(point1[0]);
        double lon1 = Math.toRadians(point1[1]);
        double lat2 = Math.toRadians(point2[0]);
        double lon2 = Math.toRadians(point2[1]);

        double dLon = lon2 - lon1;
        double dLat = lat2 - lat1;

        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = 6371000 * c;

        return distance * 1.3;
    }

    public static double getRouteDistance(List<double[]> points) {
        if (points.size() < 2) return 0;

        try {
            return getRouteDistanceFromAPI(points);
        } catch (Exception e) {
            System.err.println("API failed, using approximate distance calculation" + e.getMessage());

            return calculateApproximateDistance(points);
        }
    }
}
