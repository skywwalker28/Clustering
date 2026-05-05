package automation.clustering.optimization;

import automation.clustering.wrapper.CoordinateWrapper;
import automation.clustering.excel.ExcelExporter;
import automation.clustering.excel.ExcelReader;
import automation.clustering.model.DeliveryPoint;
import org.springframework.stereotype.Service;
import java.util.*;

import static automation.clustering.geocoding.ConnectDaData.dotenv;
import static automation.clustering.ors.BuildORS.buildORSOptimizationJson;
import static automation.clustering.path.GetPath.getPathToLatestFile;
import static automation.clustering.ors.BuildORS.sendORSRequest;
import static automation.clustering.geocoding.GeocodingAddresses.getCoordinates;
import static automation.clustering.optimization.HelperOptimization.getRelationship;
import static automation.clustering.optimization.HelperOptimization.parseORSResponse;
import static automation.clustering.optimization.CleanAddress.cleanAddress;
import static automation.clustering.map.RouteMapExporter.exportHtmlMap;

@Service
public class RouteOptimizationService {

    public static final String API_KEY = dotenv.get("API_ORS");
    public static final String filepath = getPathToLatestFile();

    public void optimizeAndDisplayRoutes() {

        Map<Integer, List<DeliveryPoint>> driverAndPoints = new HashMap<>();
        Map<Integer, List<double[]>> driverAndCoordinate = new HashMap<>();

        System.out.println("\n");

        try {
            System.out.println("Excel file: " + filepath);
            List<DeliveryPoint> points = ExcelReader.readDeliveryPointsFromExcel(filepath);
            List<double[]> coordinates = getCoordinates(points);

            Map<CoordinateWrapper, DeliveryPoint> coordinateAndPoint = getRelationship(coordinates, points);

            String requestJson = buildORSOptimizationJson(coordinates, points);
            String response = sendORSRequest(requestJson);

            parseORSResponse(response, coordinateAndPoint, driverAndPoints, driverAndCoordinate);
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
            ExcelExporter.exportToExcelSingleSheet("Реестр(Java).xlsx", driverAndPoints, driverAndCoordinate);

        } catch (Exception e) {
            System.err.println("Error in RouteOptimizationService: " + e.getMessage());
        }
    }
}