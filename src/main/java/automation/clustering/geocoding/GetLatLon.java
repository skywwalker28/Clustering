package automation.clustering.geocoding;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static automation.clustering.geocoding.GeocodingAddresses.geoResult;

public class GetLatLon {
    public static double[] parseJsonAndGetLatLon(String json) {
        double latitude = 0.0, longitude = 0.0;
        int bestGeo = 10;
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        JsonArray suggestions = root.getAsJsonArray("suggestions");
        if (suggestions.isEmpty()) {
            return null;
        }

        for (int i = 0; i < suggestions.size(); i++) {
            JsonObject firstSuggestions = suggestions.get(i).getAsJsonObject();
            JsonObject data = firstSuggestions.getAsJsonObject("data");
            int currentGeo = data.get("qc_geo").getAsInt();


            if (!data.get("geo_lat").isJsonNull() && !data.get("geo_lon").isJsonNull()) {
                if (currentGeo < bestGeo) {
                    bestGeo = currentGeo;
                    latitude = data.get("geo_lat").getAsDouble();
                    longitude = data.get("geo_lon").getAsDouble();
                }

            } else if (data.has("geo_center") && !data.get("geo_center").isJsonNull()) {
                JsonObject geoCenter = data.getAsJsonObject("geo_center");
                if (!geoCenter.get("coordinates").isJsonNull()) {
                    JsonArray dataFromCenter = geoCenter.getAsJsonArray("coordinates");
                    double lon = dataFromCenter.get(0).getAsDouble();
                    double lat = dataFromCenter.get(1).getAsDouble();

                    if (currentGeo < bestGeo && lon != 0.0 && lat != 0.0) {
                        bestGeo = currentGeo;
                        latitude = lat;
                        longitude = lon;
                    }
                } else System.err.println("GeoCenter not contains coordinates");
            } else System.err.println("Data does not contains in 'Data' and 'GeoCenter'");

            if (bestGeo == 0) break;
        }

        if (latitude != 0.0 && longitude != 0.0) System.out.println(geoResult(bestGeo));
        else return null;

        return new double[]{latitude, longitude};
    }
}
