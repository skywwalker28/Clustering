package automation.clustering.ors;

import automation.clustering.excel.ExcelReadCountDrivers;
import automation.clustering.model.DeliveryPoint;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static automation.clustering.optimization.RouteOptimizationService.API_KEY;
import static automation.clustering.optimization.RouteOptimizationService.filepath;
import static automation.clustering.ors.CreateStringBuilder.getStringBuilder;
import static automation.clustering.ors.CreateStringBuilder.getStringBuilderBMM;


public class BuildORS {
    static final int MAX_POINTS_PER_DRIVER = 8;
    static final int MAX_VEHICLES = 5;
    static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();


    public static String buildORSOptimizationJson(List<double[]> coordinates, List<DeliveryPoint> weights) {
        ExcelReadCountDrivers countDrivers = new ExcelReadCountDrivers();
        int excelCell = countDrivers.getDriverCount(filepath);

        int neededVehicles = excelCell == 0 || excelCell > 3 ?
                Math.min((int) Math.ceil((double) coordinates.size() / MAX_POINTS_PER_DRIVER), MAX_VEHICLES) :
                excelCell;
        System.out.println("neededVehicle: " + neededVehicles);

        StringBuilder jobs = getStringBuilder(coordinates, weights);
        StringBuilder vehicles = getStringBuilderBMM(neededVehicles);

        return """
        {
          "jobs": [%s],
          "vehicles": [%s],
          "options": {
            "min_vehicles": %d,
            "max_vehicles": %d,
            "gzip": false
          }
        }
        """.formatted(jobs, vehicles, neededVehicles, neededVehicles);
    }

    public static String sendORSRequest(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openrouteservice.org/optimization"))
                .header("Authorization", API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("ORS API returned code " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }
}
