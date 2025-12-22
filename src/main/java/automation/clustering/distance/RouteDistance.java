package automation.clustering.distance;

import automation.clustering.optimization.OptimizationJson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

        System.out.println("Request coordinates: " + points.size() + " points");

        String url = "https://api.openrouteservice.org/v2/directions/driving-car";

        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", OptimizationJson.API_KEY);
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        con.setConnectTimeout(5000);
        con.setReadTimeout(30000);


        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        System.out.println("Distance API response code: " + responseCode);

        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String jsonResponse = response.toString();
            System.out.println("Response received, length: " + jsonResponse.length() + " chars");

            return parseDistanceWithJackson(jsonResponse);
        } else {
            BufferedReader err = new BufferedReader(new InputStreamReader(con.getErrorStream(), "utf-8"));
            StringBuilder error = new StringBuilder();
            String line;

            while ((line = err.readLine()) != null) {
                error.append(line);
            }

            err.close();

            System.err.println("Distance API error: " + responseCode + ": " + error.toString());

            if (responseCode == 406 && points.size() > 2) {
                System.out.println("Trying alternative method with fewer points...");
                return calculateApproximateDistance(points);
            }

            return 0;
        }
    }

    public static double calculateDistance(List<double[]> points) {
        System.out.println("Calculating distance by segments...");
        double totalDistance = 0;

        for (int i = 0; i < points.size() - 1; i++) {
            List<double[]> segment = List.of(points.get(i), points.get(i + 1));

            try {
                double segmentDistance = getRouteDistanceForTwoPoints(segment);
                totalDistance += segmentDistance;
                System.out.println(" Segment " + (i+1) + ": " + String.format("%.2f", segmentDistance/1000) + " km");
            } catch (Exception e) {
                System.out.println("Error calculating segment: " + (i+1) + ": " + e.getMessage());
                totalDistance += calculateHaversineDistance(points.get(i), points.get(i + 1));
            }
        }

        System.out.println("Total approximate distance: " + String.format("%.2f", totalDistance/1000) + " km");
        return totalDistance;
    }



    private static double getRouteDistanceForTwoPoints(List<double[]> points) throws Exception {
        if (points.size() != 2) return 0;

        StringBuilder jsonBody = new StringBuilder();
        jsonBody.append("{\"coordinates\":[[")
                .append(points.get(0)[1])
                .append(",").append(points.get(0)[0])
                .append("],[").append(points.get(1)[1])
                .append(",").append(points.get(1)[0])
                .append("]],\"instructions\":\"false\"}");

        System.out.println("Calculating distance for 2 points segment");

        String url = "https://api.openrouteservice.org/v2/directions/driving-car";

        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", OptimizationJson.API_KEY);
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }


        int responseCode = con.getResponseCode();
        System.out.println("Segment API response code: " + responseCode);

        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return parseDistanceWithJackson(response.toString());
        }

        return 0;
    }

    public static double parseDistanceWithJackson(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode routes = root.path("routes");

            if (routes.isArray() && routes.size() > 0) {
                JsonNode firstRoute = routes.get(0);
                JsonNode summary = firstRoute.path("summary");

                if (!summary.isMissingNode()) {
                    double distance = summary.path("distance").asDouble();
                    System.out.println("Distance found: " + distance + " m (" + String.format(
                            "%.2f", distance/1000) + " km)"
                    );

                    return distance;
                }

                JsonNode segments = firstRoute.path("segments");
                if (segments.isArray() && segments.size() > 0) {
                    double totalDistance = 0;
                    for (JsonNode segment : segments) {
                        totalDistance += segment.path("distance").asDouble();
                    }

                    System.out.println("Total distance (sum of segments): " + totalDistance + " m");
                    return totalDistance;
                }
            }

            System.err.println("Could not find distance in response");
            System.out.println("Response structure: " + jsonResponse.substring(0,
                    Math.min(200, jsonResponse.length())) + "...");
        } catch (Exception e) {
            System.err.println("Error parsing distance with Jackson: " + e.getMessage());
        }

        return 0;
    }

    public static double calculateApproximateDistance(List<double[]> points) {
        System.out.println("Using approximate distance calculation...");
        double totalDistance = 0;

        for (int i = 0; i < points.size() - 1; i++) {
            double segmentDistance = calculateHaversineDistance(
                    points.get(i),
                    points.get(i + 1)
            );
            totalDistance += segmentDistance;

            System.out.printf(" Segment %d: %.2f km%n", i + 1, segmentDistance/1000.0);
        }

        System.out.printf("Total approximate distance: %.2f km%n", totalDistance/1000.0);
        return totalDistance;
    }

    private static double calculateHaversineDistance(double[] point1, double[] point2) {
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

        System.out.println("\n Calculating distance for " + points.size() + " points");

        try {
            return getRouteDistanceFromAPI(points);
        } catch (Exception e) {
            System.err.println("API failed, using approximate distance calculation" + e.getMessage());

            return calculateApproximateDistance(points);
        }
    }
}
