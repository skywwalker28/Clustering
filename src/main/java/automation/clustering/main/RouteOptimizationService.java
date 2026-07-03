package automation.clustering.main;

import automation.clustering.model.DeliveryPoint;
import automation.clustering.wrapper.CoordinateWrapper;
import org.springframework.stereotype.Service;
import java.util.*;

import static automation.clustering.geocoding.ConnectDaData.dotenv;
import static automation.clustering.main.HelperOptimization.getRelationship;
import static automation.clustering.main.HelperOptimization.parseORSResponse;
import static automation.clustering.ors.BuildORS.buildORSOptimizationJson;
import static automation.clustering.ors.BuildORS.sendORSRequest;
import static automation.clustering.path.GetPath.getPathToLatestFile;
import static automation.clustering.geocoding.GeocodingAddresses.getCoordinates;
import static automation.clustering.map.RouteMapExporter.exportHtmlMap;
import static automation.clustering.excel.ExcelReader.readDeliveryPointsFromExcel;
import static automation.clustering.algorithm.KMeansAlgorithm.cluster;


@Service
public class RouteOptimizationService {

    public static final String API_KEY = dotenv.get("API_ORS");
    public static final String filepath = getPathToLatestFile();


    Shutdown shutdown;
    RouteOptimizationService(Shutdown shutdown) { this.shutdown = shutdown; }

    public void optimizeAndDisplayRoutes() {

        Map<Integer, List<DeliveryPoint>> driverAndPoints = new HashMap<>();
        int[] totalPoints = new int[1];

        try {
            System.out.println("Excel file: " + filepath);
            List<DeliveryPoint> points = readDeliveryPointsFromExcel(filepath);
            List<double[]> coordinates = getCoordinates(points);

            Map<CoordinateWrapper, DeliveryPoint> coordinateAndPoint = getRelationship(coordinates, points);

            if (points.size() >= 50) {
                String requestJson = buildORSOptimizationJson(coordinates, points);
                String response = sendORSRequest(requestJson);

                if (response == null) {
                    exportHtmlMap(driverAndPoints, "map.html");
                    return;
                }

                parseORSResponse(response, coordinateAndPoint, driverAndPoints, totalPoints);

            } else driverAndPoints = cluster(points, 10, 2, totalPoints);


            System.out.println("\nВсего распределенно точек: " + totalPoints[0] + "/" + coordinates.size());
            exportHtmlMap(driverAndPoints, "map.html");
            System.out.println("Распределено на " + driverAndPoints.size() + " водителей\n");

        } catch (Exception e) {
            System.err.println("Ошибка в RouteOptimizationService: " + e.getMessage());
            shutdown.stopApplication(1);
        }
    }
}