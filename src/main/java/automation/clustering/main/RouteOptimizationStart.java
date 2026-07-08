package automation.clustering.main;

import automation.clustering.model.DeliveryPoint;
import automation.clustering.wrapper.CoordinateWrapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import java.util.*;

import static automation.clustering.main.HelperOptimization.getRelationship;
import static automation.clustering.main.HelperOptimization.parseORSResponse;
import static automation.clustering.ors.BuildORS.buildORSOptimizationJson;
import static automation.clustering.ors.BuildORS.sendORSRequest;
import static automation.clustering.geocoding.GeocodingAddresses.getCoordinates;
import static automation.clustering.map.RouteMapExporter.exportHtmlMap;
import static automation.clustering.excel.ExcelReader.readDeliveryPointsFromExcel;
import static automation.clustering.algorithm.KMeansAlgorithm.cluster;


@Service
public class RouteOptimizationStart {
    public static final Dotenv dotenv = Dotenv.load();
    public static String filepath;

    public void optimizeAndDisplayRoutes(String filepath, int driverCount, int maxPoints) {

        RouteOptimizationStart.filepath = filepath;
        Map<Integer, List<DeliveryPoint>> driverAndPoints = new HashMap<>();

        try {
            int[] allPoints = {0};
            System.out.println("Excel file: " + filepath);
            List<DeliveryPoint> points = readDeliveryPointsFromExcel(filepath, allPoints);
            List<double[]> coordinates = getCoordinates(points);

            Map<CoordinateWrapper, DeliveryPoint> coordinateAndPoint = getRelationship(coordinates, points);

            if (points.size() >= 50) {
                String requestJson = buildORSOptimizationJson(coordinates, points);
                String response = sendORSRequest(requestJson);
                parseORSResponse(response, coordinateAndPoint, driverAndPoints);
            } else driverAndPoints = cluster(points, maxPoints, driverCount);


            System.out.println("\nВсего распределенно точек: " + coordinates.size() + "/" + allPoints[0]);
            exportHtmlMap(driverAndPoints, "map.html");
            System.out.println("Распределено на " + driverAndPoints.size() + " водителей\n");

        } catch (Exception e) {
            System.err.println("Ошибка в RouteOptimizationService: " + e.getMessage());
        }
    }
}