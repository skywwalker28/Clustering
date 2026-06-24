package automation.clustering.main;

import automation.clustering.wrapper.CoordinateWrapper;
import automation.clustering.model.DeliveryPoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;

public class HelperOptimization {

    public static Map<CoordinateWrapper, DeliveryPoint> getRelationship(
            List<double[]> coordinates, List<DeliveryPoint> points) {

        Map<CoordinateWrapper, DeliveryPoint> result = new HashMap<>();

        for (int i = 0; i < coordinates.size(); i++) {
            double[] lat_lon = coordinates.get(i);

            if (lat_lon == null) continue;
            result.put(new CoordinateWrapper(lat_lon), points.get(i));
        }
        return result;
    }

    public static void parseORSResponse(
            String response, Map<CoordinateWrapper, DeliveryPoint> cp,
            Map<Integer,List<DeliveryPoint>> dp, Map<Integer, List<double[]>> dc) {

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();
        JsonArray routes = root.getAsJsonArray("routes");

        for (int i = 0; i < routes.size(); i++) {
            JsonObject route = routes.get(i).getAsJsonObject();
            int driverId = route.get("vehicle").getAsInt();

            List<DeliveryPoint> points = new ArrayList<>();
            List<double[]> coordinates = new ArrayList<>();

            JsonArray steps = route.getAsJsonArray("steps");

            for (int j = 0; j < steps.size(); j++) {
                JsonObject step = steps.get(j).getAsJsonObject();
                String type = step.get("type").getAsString();

                if (type.equals("job")) {
                    JsonArray location = step.getAsJsonArray("location");
                    double[] lat_lon = {location.get(1).getAsDouble(), location.get(0).getAsDouble()};

                    points.add(cp.get(new CoordinateWrapper(lat_lon)));
                    coordinates.add(lat_lon);
                }
            }

            dp.put(driverId, points);
            dc.put(driverId, coordinates);
        }
    }
}
