package automation.clustering.map;

import automation.clustering.model.DeliveryPoint;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static automation.clustering.main.CleanAddress.cleanAddress;

public class RouteMapExporter {

    private static final String[] COLORS = {"#00aa00", "#ff0000", "#0000ff", "#ff6600", "#9900cc"};
    private static final double[] BMM_COORDS = {37.747183, 55.592605};
    private static final double AVG_SPEED = 40.0;
    private static final int UNLOADING_TIME = 15;
    private static final int MANEUVER_TIME = 10;
    private static final double DISTANCE_COEFFICIENT = 1.3;

    private static final String[][] DRIVER_INFO_LIST = {
            {"Ниссан В771АР 550", "Харламов Станислав Андреевич", "8 (901) 761-5771", "ИП Лихолетова Оксана", "7500"},
            {"Sollers Argo В601НТ977", "Ординарцев Владимир Александрович", "8(977)315-6815", "ООО Имтэк", "0.00"},
            {"Sollers Argo Н689НЕ977", "Оганесян Давид Миранович. Паспорт 4622 620024. Выдан ГУ МВД России по Моск области, " +
                    "02.07.2022", "8(917)579-0990", "Имтэк", "0.00"},
            {"-", "-", "-", "-", "-", "0.00"}
    };

    private static String escapeJsString(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("'", "\\'");
    }

    private static String formatDouble(double value) {
        return String.format("%.6f", value).replace(",", ".");
    }

    private static String loadTemplate() throws Exception {
        return Files.readString(Paths.get("src/main/resources/map-template.html"));
    }

    public static void exportHtmlMap(
            Map<Integer, List<DeliveryPoint>> driverAndPoints,
            String fileName
    ) {
        try {
            String template = loadTemplate();

            // Заменяем bmm координаты
            String bmmCoords = "[" + formatDouble(BMM_COORDS[0]) + ", " + formatDouble(BMM_COORDS[1]) + "]";
            template = template.replace("__BMM_COORDS__", bmmCoords);
            template = template.replace("__DISTANCE_COEFFICIENT__", String.valueOf(DISTANCE_COEFFICIENT));
            template = template.replace("__AVG_SPEED__", String.valueOf(AVG_SPEED));
            template = template.replace("__UNLOADING_TIME__", String.valueOf(UNLOADING_TIME));
            template = template.replace("__MANEUVER_TIME__", String.valueOf(MANEUVER_TIME));

            StringBuilder driverInfoJson = new StringBuilder("[");
            for (int i = 0; i < DRIVER_INFO_LIST.length; i++) {
                if (i > 0) driverInfoJson.append(",");
                driverInfoJson.append("{")
                        .append("\"vehicleNumber\": \"").append(escapeJsString(DRIVER_INFO_LIST[i][0])).append("\", ")
                        .append("\"driverName\": \"").append(escapeJsString(DRIVER_INFO_LIST[i][1])).append("\", ")
                        .append("\"phone\": \"").append(escapeJsString(DRIVER_INFO_LIST[i][2])).append("\", ")
                        .append("\"carrier\": \"").append(escapeJsString(DRIVER_INFO_LIST[i][3])).append("\", ")
                        .append("\"tariff\": \"").append(escapeJsString(DRIVER_INFO_LIST[i][4])).append("\"")
                        .append("}");
            }

            StringBuilder allPointJson = new StringBuilder("[");
            boolean first = true;
            for (List<DeliveryPoint> route : driverAndPoints.values()) {
                if (route == null) continue;

                for (DeliveryPoint point : route) {
                    if (!first) {
                        allPointJson.append(",");
                    }
                    first = false;

                    allPointJson.append("{")
                            .append("\"num\": ").append(point.getNumber()).append(",")
                            .append("\"coords\":[")
                            .append(formatDouble(point.getLon())).append(",")
                            .append(formatDouble(point.getLat())).append("],")
                            .append("\"weight\":").append(point.getWeightKg()).append(",")
                            .append("\"address\":\"").append(escapeJsString(point.getAddress())).append("\"")
                            .append("}");
                }

                allPointJson.append("]");
                template = template.replace("__ALL_POINTS__", allPointJson.toString());
            }

            driverInfoJson.append("]");
            template = template.replace("__DRIVER_INFO_LIST__", driverInfoJson.toString());

            StringBuilder driversData = new StringBuilder("[");
            int driverIndex = 0;
            int totalDrivers = 0;
            for (Map.Entry<Integer, List<DeliveryPoint>> entry : driverAndPoints.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) totalDrivers++;
            }

            for (Map.Entry<Integer, List<DeliveryPoint>> entry : driverAndPoints.entrySet()) {
                List<DeliveryPoint> route = entry.getValue();
                if (route == null || route.isEmpty()) continue;

                String color = COLORS[driverIndex % COLORS.length];
                int driverNumber = driverIndex + 1;

                driversData.append("{");
                driversData.append("name: \"Водитель ").append(driverNumber).append("\", ");
                driversData.append("color: \"").append(color).append("\", ");
                driversData.append("points: [");

                for (DeliveryPoint point : route) {
                    String address = cleanAddress(point.getAddress());
                    address = escapeJsString(address);
                    driversData.append("{")
                            .append("coords: [").append(formatDouble(point.getLon())).append(", ")
                            .append(formatDouble(point.getLat())).append("], ")
                            .append("num: ").append(point.getNumber()).append(", ")
                            .append("weight: ").append(point.getWeightKg()).append(", ")
                            .append("address: \"").append(address).append("\"")
                            .append("},");
                }
                if (!route.isEmpty()) {
                    driversData.deleteCharAt(driversData.length() - 1);
                }
                driversData.append("]");
                driversData.append("}");
                if (driverIndex < totalDrivers - 1) driversData.append(",");
                driverIndex++;
            }
            driversData.append("]");
            template = template.replace("__DRIVERS_DATA__", driversData.toString());

            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(template);
            }

            System.out.println("Карта создана, название: " + fileName);

        } catch (Exception e) {
            System.err.println("Ошибка в создании карты: " + e.getMessage());
        }
    }
}