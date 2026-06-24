package automation.clustering.controller;

import automation.clustering.excel.CreateFinalRegistry;
import automation.clustering.model.DeliveryPoint;
import automation.clustering.model.Driver;
import com.google.gson.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

import static automation.clustering.main.RouteOptimizationService.filepath;

@CrossOrigin(origins = "*")
@RestController
public class ControllerForMap {

    private final Map<Integer, List<DeliveryPoint>> driverAndPoints = new HashMap<>();
    private final ArrayList<Driver> driverInfo = new ArrayList<>();

    @PostMapping("/save-routes2")
    public ResponseEntity<?> convertJson(@RequestBody String json) throws Exception {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        JsonArray jsonArrayDriver = jsonObject.getAsJsonArray("driverAssignments");
        JsonArray jsonArrayPoints = jsonObject.getAsJsonArray("drivers");

        for (int i = 0; i < jsonArrayDriver.size(); i++) {
            JsonObject objectPoints = jsonArrayPoints.get(i).getAsJsonObject();

            JsonArray pointsArray = objectPoints.getAsJsonArray("points");

            JsonElement currentDriver = jsonArrayDriver.get(i);
            Driver driversData = new Gson().fromJson(currentDriver, Driver.class);

            List<DeliveryPoint> points = new ArrayList<>();

            for (int j = 0; j < pointsArray.size(); j++) {
                JsonElement currentPoint = pointsArray.get(j);

                DeliveryPoint deliveryPoint = new Gson().fromJson(currentPoint, DeliveryPoint.class);
                points.add(deliveryPoint);
            }

            driverAndPoints.put(driversData.getDriverIndex(), points);
            driverInfo.add(driversData);
        }

        String outputFilePath = System.getProperty("user.home") + "/Downloads/Для_водителей.xlsx";
        CreateFinalRegistry.exportToExcelOriginalFormat(filepath, outputFilePath, driverAndPoints, driverInfo);
        return ResponseEntity.ok("Файл успешно сгенерировался в Загрузках");
    }
}
