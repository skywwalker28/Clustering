//package automation.clustering.controller;
//
//import automation.clustering.excel.CreateFinalRegistry;
//import automation.clustering.model.DeliveryPoint;
//import org.springframework.core.io.InputStreamResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static automation.clustering.optimization.RouteOptimizationService.filepath;
//
//@CrossOrigin(origins = "*")
//@RestController
//public class RouteController {
//
//    private Map<Integer, List<DeliveryPoint>> currentDriverPoints;
//    private ArrayList<String[]> currentDriverInfo;
//
//    @PostMapping("/save-routes")
//    public ResponseEntity<?> saveRoutes(@RequestBody Map<String, Object> data) throws Exception {
//        List<Map<String, Object>> driversData = (List<Map<String, Object>>) data.get("drivers");
//        List<Map<String, Object>> assignments = (List<Map<String, Object>>) data.get("driverAssignments");
//
//        currentDriverPoints = new HashMap<>();
//        currentDriverInfo = new ArrayList<>();
//
//        for (Map<String, Object> assignment : assignments) {
//            int driverId = (int) assignment.get("driverIndex");
//            Map<String, Object> driverData = (Map<String, Object>) assignment.get("driverData");
//
//            String[] driverInfo = new String[] {
//                    (String) driverData.get("vehicleNumber"),
//                    (String) driverData.get("driverName"),
//                    (String) driverData.get("phone"),
//                    (String) driverData.get("carrier"),
//                    (String) driverData.get("tariff")
//            };
//
//            currentDriverInfo.add(driverInfo);
//
//            Map<String, Object> driverRoute = driversData.get(driverId);
//            List<Map<String, Object>> points = (List<Map<String, Object>>) driverRoute.get("points");
//
//            List<DeliveryPoint> deliveryPoints = new ArrayList<>();
//            for (Map<String, Object> point : points) {
//                DeliveryPoint dp = new DeliveryPoint(null, 0, 0);
//                dp.setNumber((Integer) point.get("num"));
//                dp.setWeightKg((Integer) point.get("weight"));
//                dp.setAddress((String) point.get("address"));
//                List<Double> coords = (List<Double>) point.get("coords");
//                dp.setLon(coords.get(0));
//                dp.setLat(coords.get(1));
//                deliveryPoints.add(dp);
//            }
//
//            currentDriverPoints.put(driverId, deliveryPoints);
//        }
//
//        String userHome = System.getProperty("user.home");
//        String downloadsPath = userHome + "/Downloads/Для_водителей.xlsx";
//
//        CreateFinalRegistry.exportToExcelOriginalFormat(filepath, downloadsPath,
//                currentDriverPoints, currentDriverInfo);
//
//        return ResponseEntity.ok(Map.of("success", true));
//    }
//
//    @GetMapping("/download-excel")
//    public ResponseEntity<Resource> downloadExcel() throws IOException {
//        String userHome = System.getProperty("user.home");
//        String filePath = userHome + "/Downloads/Для_водителей.xlsx";
//        File file = new File(filePath);
//
//        if (!file.exists()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Path path = Paths.get(filePath);
//        InputStreamResource resource = new InputStreamResource(Files.newInputStream(path));
//
//        String encodedFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8)
//                .replaceAll("\\+", "%20");
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
//                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//                .body(resource);
//    }
//}
