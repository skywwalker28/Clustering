package automation.clustering.main;

import automation.clustering.algorithm.KMeansAlgorithm;
import automation.clustering.wrapper.CoordinateWrapper;
import automation.clustering.model.DeliveryPoint;
import org.springframework.stereotype.Service;
import java.util.*;

import static automation.clustering.geocoding.ConnectDaData.dotenv;
import static automation.clustering.ors.BuildORS.buildORSOptimizationJson;
import static automation.clustering.ors.BuildORS.sendORSRequest;
import static automation.clustering.path.GetPath.getPathToLatestFile;
import static automation.clustering.geocoding.GeocodingAddresses.getCoordinates;
import static automation.clustering.main.HelperOptimization.getRelationship;
import static automation.clustering.main.HelperOptimization.parseORSResponse;
import static automation.clustering.main.CleanAddress.cleanAddress;
import static automation.clustering.map.RouteMapExporter.exportHtmlMap;
import static automation.clustering.map.RouteMapExporterHandle.exportHtmlMap;
import static automation.clustering.excel.ExcelReader.readDeliveryPointsFromExcel;


@Service
public class RouteOptimizationService {

    public static final String API_KEY = dotenv.get("API_ORS");
    public static final String filepath = getPathToLatestFile();


    Shutdown shutdown;
    RouteOptimizationService(Shutdown shutdown) { this.shutdown = shutdown; }

    public void optimizeAndDisplayRoutes() {

        Map<Integer, List<DeliveryPoint>> driverAndPoints;
        Map<Integer, List<double[]>> driverAndCoordinate = new HashMap<>();

        System.out.println("\n");

        try {
            System.out.println("Excel file: " + filepath);
            List<DeliveryPoint> points = readDeliveryPointsFromExcel(filepath);
            List<double[]> coordinates = getCoordinates(points);

            Map<CoordinateWrapper, DeliveryPoint> coordinateAndPoint = getRelationship(coordinates, points);

/*            String requestJson = buildORSOptimizationJson(coordinates, points);
            String response = sendORSRequest(requestJson);

            if (response == null) {
                exportHtmlMap(points, "map.html");
                return;
            }

            parseORSResponse(response, coordinateAndPoint, driverAndPoints, driverAndCoordinate);*/
            driverAndPoints = KMeansAlgorithm.cluster(points, 3, 10);
            exportHtmlMap(driverAndPoints, "map.html");

            int totalPoints = 0;
            for (Map.Entry<Integer, List<DeliveryPoint>> entry : driverAndPoints.entrySet()) {
                int driverNum = entry.getKey() + 1;
                List<DeliveryPoint> getAddresses = entry.getValue();

                System.out.println("\nВоидетль " + driverNum + ". Всего точек: " + getAddresses.size());

                for (DeliveryPoint current : getAddresses) {
                    totalPoints++;
                    String point = current.getAddress();
                    System.out.println(current.getNumber() + ". " + cleanAddress(point));
                }
            }

            System.out.println("\nВсего распределенно точек: " + totalPoints + "/" + coordinates.size());

        } catch (Exception e) {
            System.err.println("Error in RouteOptimizationService: " + e.getMessage());
            shutdown.stopApplication(1);
        }
    }
}