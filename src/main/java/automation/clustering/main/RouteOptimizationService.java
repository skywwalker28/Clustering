package automation.clustering.main;

import automation.clustering.model.DeliveryPoint;
import org.springframework.stereotype.Service;
import java.util.*;

import static automation.clustering.geocoding.ConnectDaData.dotenv;
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

        Map<Integer, List<DeliveryPoint>> driverAndPoints;


        System.out.println("\n");

        try {
            System.out.println("Excel file: " + filepath);
            List<DeliveryPoint> points = readDeliveryPointsFromExcel(filepath);
            List<double[]> coordinates = getCoordinates(points);
            int[] totalPoints = new int[1];
            driverAndPoints = cluster(points, 10, 3, totalPoints);


            exportHtmlMap(driverAndPoints, "map.html");
            System.out.println("\nВсего распределенно точек: " + totalPoints[0] + "/" + coordinates.size());
            System.out.println("Распределено на " + driverAndPoints.size() + " водителей");

        } catch (Exception e) {
            System.err.println("Error in RouteOptimizationService: " + e.getMessage());
            shutdown.stopApplication(1);
        }
    }
}