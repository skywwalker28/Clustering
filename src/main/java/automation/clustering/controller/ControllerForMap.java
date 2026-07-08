package automation.clustering.controller;

import automation.clustering.excel.CreateFinalRegistry;
import automation.clustering.main.RouteOptimizationStart;
import automation.clustering.model.DeliveryPoint;
import automation.clustering.model.Driver;
import com.google.gson.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static automation.clustering.main.RouteOptimizationStart.filepath;

@CrossOrigin(origins = "*")
@RestController
public class ControllerForMap {

    private final Map<Integer, List<DeliveryPoint>> driverAndPoints = new HashMap<>();
    private final ArrayList<Driver> driverInfo = new ArrayList<>();
    private final RouteOptimizationStart routeService;

    public ControllerForMap(RouteOptimizationStart routeOptimizationService) {
        this.routeService = routeOptimizationService;
    }

    @PostMapping("upload-excel")
    public ResponseEntity<?> uploadAndOptimize(
            @RequestParam("file") MultipartFile file,
            @RequestParam("driverCount") int driverCount,
            @RequestParam("maxPoints") int maxPoints) {
        try {
            String currentDir = System.getProperty("user.dir");
            File targetFile = new File(currentDir, "реестр(на вход).xlsx");

            file.transferTo(targetFile);

            routeService.optimizeAndDisplayRoutes(targetFile.getAbsolutePath(), driverCount, maxPoints);
            return ResponseEntity.ok("view-map");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при обработке файла: " + e.getMessage());
        }
    }

    @GetMapping(value = "view-map", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String viewMap() throws IOException {
        String filename = "map.html";
        Path path = Paths.get(System.getProperty("user.dir"), filename);

        if (!Files.exists(path) || Files.size(path) == 0) {
            return "<h3>Карта еще не сгенерирована. Пожалуйста, загрузите файл.</h3>";
        }

        return Files.readString(path, StandardCharsets.UTF_8);
    }


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
