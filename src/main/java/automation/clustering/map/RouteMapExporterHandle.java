package automation.clustering.map;

import automation.clustering.model.DeliveryPoint;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static automation.clustering.main.CleanAddress.cleanAddress;

public class RouteMapExporterHandle {

    private static final double[] BMM_COORDS = {37.747183, 55.592605};
    private static final double AVG_SPEED = 35.0;
    private static final int UNLOADING_TIME = 10;
    private static final int MANEUVER_TIME = 5;
    private static final double DISTANCE_COEFFICIENT = 1.2;

    private static final String[][] DRIVER_INFO_LIST = {
            {"Ниссан В771АР 550", "Харламов Станислав Андреевич", "8 (901) 761-5771", "ИП Лихолетова Оксана", "7500"},
            {"Sollers Argo В601НТ977", "Ординарцев Владимир Александрович", "8(977)315-6815", "ООО Имтэк", "0.00"},
            {"Sollers Argo", "Оганесян Давид Миранович. Паспорт 4622 620024. " +
                    "Выдан ГУ МВД России по Моск области, 02.07.2022", "8(917)579-0990", "Имтэк", "0.00"}
    };

    public static void exportHtmlMap(List<DeliveryPoint> allPointsList, String fileName) {
        try {
            String allPointsJson = generatePointsJson(allPointsList);
            String driverInfoJson = generateDriverJson();

            String templatePath = "src/main/resources/map-template-handle.html";
            String htmlContent = Files.readString(Paths.get(templatePath), StandardCharsets.UTF_8);

            htmlContent = htmlContent
                    .replace("__BMM_COORDS__", "[" + formatDouble(BMM_COORDS[0]) + ", "
                            + formatDouble(BMM_COORDS[1]) + "]")
                    .replace("__DISTANCE_COEFFICIENT__", String.valueOf(DISTANCE_COEFFICIENT))
                    .replace("__AVG_SPEED__", String.valueOf(AVG_SPEED))
                    .replace("__UNLOADING_TIME__", String.valueOf(UNLOADING_TIME))
                    .replace("__MANEUVER_TIME__", String.valueOf(MANEUVER_TIME))
                    .replace("__DRIVER_INFO_LIST__", driverInfoJson)
                    .replace("__ALL_POINTS__", allPointsJson);

            Files.writeString(Paths.get(fileName), htmlContent, StandardCharsets.UTF_8);
            System.out.println("🗺️ Карта успешно создана: " + fileName);

        } catch (IOException e) {
            System.err.println("Ошибка работы с файлом: " + e.getMessage());
        }
    }

    private static String generatePointsJson(List<DeliveryPoint> points) {
        if (points == null || points.isEmpty()) return "[]";
        return points.stream().map(p -> String.format(
                "{\"coords\": [%s, %s], \"num\": %d, \"weight\": %d, \"address\": \"%s\"}",
                formatDouble(p.getLon()), formatDouble(p.getLat()), p.getNumber(), p.getWeightKg(),
                escapeJsString(cleanAddress(p.getAddress()))
        )).collect(Collectors.joining(",", "[", "]"));
    }

    private static String generateDriverJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < DRIVER_INFO_LIST.length; i++) {
            sb.append(i > 0 ? "," : "").append(String.format(
                    "{\"vehicleNumber\": \"%s\", \"driverName\": \"%s\", \"phone\": \"%s\", \"carrier\":" +
                            " \"%s\", \"tariff\": \"%s\"}",
                    escapeJsString(DRIVER_INFO_LIST[i][0]), escapeJsString(DRIVER_INFO_LIST[i][1]),
                    escapeJsString(DRIVER_INFO_LIST[i][2]), escapeJsString(DRIVER_INFO_LIST[i][3]),
                    escapeJsString(DRIVER_INFO_LIST[i][4])
            ));
        }
        return sb.append("]").toString();
    }

    private static String escapeJsString(String s) {
        return (s == null) ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String formatDouble(double value) {
        return String.format("%.6f", value).replace(",", ".");
    }
}