package automation.clustering.optimization;

import automation.clustering.excel.ExcelExporter;
import automation.clustering.excel.ExcelReader;
import automation.clustering.map.RouteMapExporter;
import automation.clustering.model.DeliveryPoint;
import org.springframework.stereotype.Service;
import java.util.*;

import static automation.clustering.json.BuildORS.buildORSOptimizationJson;
import static automation.clustering.path.GetPath.getPathToLatestFile;
import static automation.clustering.json.BuildORS.sendORSRequest;
import static automation.clustering.geocoding.GeocodingAddresses.getCoordinates;
import static automation.clustering.optimization.HelperOptimization.getRelationship;
import static automation.clustering.optimization.HelperOptimization.parseORSResponse;

@Service
public class RouteOptimizationService {

    public static final String API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjRlOD" +
          "I1N2ZkOGU1YzRmZjdiMjgxNTJhYWViZjFkZDY2IiwiaCI6Im11cm11cjY0In0=";

    public static final String filepath = getPathToLatestFile();

    public void optimizeAndDisplayRoutes() {
        System.out.println("\n");

        try {
            System.out.println("Excel file: " + filepath);
            List<DeliveryPoint> points = ExcelReader.readDeliveryPointsFromExcel("/Users/skywalker/Downloads/28.04 розница.xlsx");
            List<double[]> coordinates = getCoordinates(points);

            if (coordinates.isEmpty()) throw new RuntimeException("List coordinates is empty!");
            Map<String, DeliveryPoint> pointsAndCoordinates = getRelationship(coordinates, points);

            String requestJson = buildORSOptimizationJson(coordinates, points);
            String response = sendORSRequest(requestJson);

            Map<Integer, List<DeliveryPoint>> driverAndPoints = new HashMap<>();

            Map<Integer, List<double[]>> driverAndCoordinate = new HashMap<>();

            parseORSResponse(response, pointsAndCoordinates, driverAndPoints, driverAndCoordinate);

            RouteMapExporter.exportHtmlMap(driverAndPoints, "карта_кластеризации.html");

            int totalPoints = 0;
            for (Map.Entry<Integer, List<DeliveryPoint>> entry : driverAndPoints.entrySet()) {
                int driverNum = entry.getKey() + 1;
                List<DeliveryPoint> getAddresses = entry.getValue();

                System.out.println("\nВоидетль " + driverNum + ". Всего точек: " + getAddresses.size());

                for (DeliveryPoint current : getAddresses) {
                    totalPoints++;
                    System.out.println(current.getNumber() + ". " + current.getAddress());
                }
            }

            System.out.println("\nВсего распределенно точек: " + totalPoints + "/" + coordinates.size());

            ExcelExporter.exportToExcelSingleSheet("Реестр(Java).xlsx", driverAndPoints, driverAndCoordinate);

        } catch (Exception e) {
            System.err.println("Error in RouteOptimizationService: " + e.getMessage());
        }
    }
}