package automation.clustering.geocoding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OpenRouteGeocoder {

    private static final String API_KEY = "b756271d85802e92aa6e6d398a0a5bf72fafcb19";
    private static final String CLEAN_URL = "https://cleaner.dadata.ru/api/v1/clean/address";

    public static double[] firstGeocodingAddress(String address) {
        double[] coordinates = geocodingAddress(address);

        if (coordinates != null) {
            return coordinates;
        }

        System.out.println("Trying clean geocoding as fallback...");
        return cleanGeocoding(address);
    }


    public static double[] geocodingAddress(String address) {
        System.out.println("Start geocoding address: " + address);

        try {
            String request = String.format("{\"query\": \"%s\", \"count\": 3}", address);

            String url = "https://suggestions.dadata.ru/suggestions/api/4_1/rs/suggest/address";

            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", "Token " + API_KEY);

            con.setDoOutput(true);
            con.setConnectTimeout(5000);
            con.setReadTimeout(10000);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = request.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            System.out.println("Response Code API: " + responseCode);

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                return parseGeocodingResponse(response.toString(), address);
            } else {
                System.err.println("Error API: " + responseCode);
                BufferedReader err = new BufferedReader(
                        new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
                String error = err.readLine();
                err.close();
                System.out.println("Information error: " + error);
            }

        } catch (Exception e) {
            System.err.println("Error in geocoding service: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static double[] cleanGeocoding(String address) {
        System.out.println("Start clean geocoding address: " + address);

        try {
            String request = String.format("[ \"%s\" ]", address.replace("\"", "\\\""));

            HttpURLConnection con = (HttpURLConnection) new URL(CLEAN_URL).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", "Token " + API_KEY);

            con.setDoOutput(true);
            con.setConnectTimeout(5000);
            con.setReadTimeout(10000);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = request.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            System.out.println("Response Code API(clean): " + responseCode);

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                return parseCleanGeocodingResponse(response.toString(), address);
            } else {
                System.err.println("Error API(clean): " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error in geocoding service: " + e.getMessage());
        }

        return null;
    }

    public static double[] parseGeocodingResponse(String jsonResponse, String originalAddress) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            JsonNode suggestion = root.path("suggestions");
            if (!suggestion.isArray() || suggestion.isEmpty()) {
                System.out.println("No suggestion found: " + originalAddress);
                return null;
            }

            JsonNode firstSuggestion = suggestion.get(0);
            JsonNode data = firstSuggestion.path("data");

            double latitude = data.path("geo_lat").asDouble(0);
            double longitude = data.path("geo_lon").asDouble(0);

            JsonNode geoCenter = data.path("geo_center");
            if (geoCenter.isObject()) {
                String type = geoCenter.path("type").asText();
                if ("Point".equals(type)) {
                    JsonNode coordinates = geoCenter.path("coordinates");
                    if (coordinates.isArray() && !coordinates.isEmpty()) {
                        longitude = coordinates.get(0).asDouble();
                        latitude = coordinates.get(1).asDouble();
                    }
                }
            }

            if (latitude == 0 || longitude == 0) {
                System.out.println("Invalid location: " + originalAddress);

                return null;
            }

            String qc = data.path("qc").asText("");
            String qcGeo = data.path("qc_geo").asText("");

            String geoAccuracy = switch(qcGeo) {
                case "0" -> "точное попадание в здании/участок";
                case "1" -> "ближайший дом (до 50м)";
                case "2" -> "улица (до 100 м)";
                case "3" -> "населенный пункт";
                case "4" -> "регион";
                case "5" -> "страна";
                default -> "неизвестная точность";
            };

            System.out.println("Coordinates: [" + longitude + ", " + latitude + "]");
            System.out.println("GeoAccuracy: " + geoAccuracy);
            System.out.println("\n");

            if (!qcGeo.isEmpty() && !qcGeo.equals("0") && !qcGeo.equals("1")) {
                System.out.println("Внимание: низкая точность геокодирования!");
            }

            System.out.println("\n");

            return new double[]{latitude, longitude};



        } catch (Exception e) {
            System.err.println("Error in parsing JSON: " + e.getMessage());
        }

        return null;
    }

    public static double[] parseCleanGeocodingResponse(String jsonResponse, String originalAddress) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            if (!root.isArray() || root.isEmpty()) {
                System.out.println("No suggestion found: " + originalAddress);
                return null;
            }

            JsonNode firstResult = root.get(0);

            double latitude = firstResult.path("geo_lat").asDouble(0);
            double longitude = firstResult.path("geo_lon").asDouble(0);

            if (latitude == 0 || longitude == 0) {
                System.out.println("No coordinates in clean response: " + originalAddress);
                return null;
            }

            String qc = firstResult.path("qc").asText();
            String qcGeo = firstResult.path("qc_geo").asText();
            String result = firstResult.path("result").asText();

            System.out.println("Clean geocoding result: " + result);
            System.out.println("QC_GEO (clean): " + qcGeo);
            System.out.println("Coordinates (clean): [" + longitude + ", " + latitude + "]");

            System.out.println("=".repeat(50));

            return new double[]{latitude, longitude};
        } catch (Exception e) {
            System.err.println("Error in parsing JSON: " + e.getMessage());
            return null;
        }
    }

    public static List<double[]> geocodingAddresses(List<String> addresses) {
        List<double[]> results = new ArrayList<>();

        System.out.println("\n" + "=".repeat(50));
        System.out.println("Geocoding addresses: " + addresses.size() + " addresses");
        System.out.println("=".repeat(50));

        for (String address : addresses) {
            double[] coordinates = firstGeocodingAddress(address);
            if (coordinates != null) {
                results.add(coordinates);
            } else {
                System.err.println("Skip address: " + address);
            }

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return results;
    }
}
