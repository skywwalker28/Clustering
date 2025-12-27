package automation.clustering.excel;

import automation.clustering.distance.RouteDistance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    private static final String[] DRIVER_COLORS = {"#FF0000", "#0000FF", "#00FF00", "#FFA500", "#800080"};

    public static void exportToExcelSingleSheet(Map<Integer, List<double[]>> driverRoutes,
                                                Map<Integer, List<String>> driverAddresses,
                                                Map<Integer, List<Integer>> driverWeights,
                                                String filepath) throws Exception {
        Workbook workbook = new XSSFWorkbook();

        CellStyle headerStyle = Styles.createHeaderStyle(workbook);
        CellStyle driverHeaderStyle = Styles.createDriverStyle(workbook);
        CellStyle addressStyle = Styles.createAddressStyleStyle(workbook);
        CellStyle distanceStyle = Styles.createDistanceStyle(workbook);
        CellStyle timeStyle = Styles.createTimeStyle(workbook);
        CellStyle weightStyle = workbook.createCellStyle();
        weightStyle.cloneStyleFrom(driverHeaderStyle);

        Sheet sheet = workbook.createSheet("Delivery Routes");

        int currentRow = 0;

        for (Map.Entry<Integer, List<double[]>> entry : driverRoutes.entrySet()) {
            int driverId = entry.getKey();
            int driverNumber = driverId + 1;
            List<double[]> points = entry.getValue();
            List<String> addresses = driverAddresses.get(driverId);
            List<Integer> weights = driverWeights.get(driverId);

            if (points.isEmpty()) continue;

            // цвет водителя
            String hexColor = DRIVER_COLORS[driverId % DRIVER_COLORS.length];
            XSSFColor driverColor = Styles.createColor(hexColor);

            // Заголовок водителя
            Row driverHeaderRow = sheet.createRow(currentRow++);
            Cell driverCell = driverHeaderRow.createCell(0);
            driverCell.setCellValue("Водитель: " + driverNumber);

            XSSFCellStyle driverCellStyle = (XSSFCellStyle) workbook.createCellStyle();
            driverCellStyle.cloneStyleFrom(driverHeaderStyle);
            driverCellStyle.setFillForegroundColor(driverColor);
            driverCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            driverCell.setCellStyle(driverCellStyle);
            sheet.addMergedRegion(
                    new CellRangeAddress(currentRow - 1, currentRow -1, 0, 7));

            // Инфо по водителю
            Row infoRow = sheet.createRow(currentRow++);
            double totalDistance = RouteDistance.getRouteDistance(points);
            double estimatedTime = Styles.calculateEstimatedTime(totalDistance);

            int totalWeights = (weights != null) ? weights.stream().mapToInt(Integer::intValue).sum() : 0;
            double fillPercent = Math.min((double) totalWeights / 550 * 100, 100);

            infoRow.createCell(0).setCellValue("Кол-во точек:");
            infoRow.createCell(1).setCellValue(points.size());
            infoRow.createCell(2).setCellValue("Общая дистанция:");
            Cell distanceCell = infoRow.createCell(3);
            distanceCell.setCellValue(totalDistance / 1000);
            distanceCell.setCellStyle(distanceStyle);
            infoRow.createCell(4).setCellValue("км");
            infoRow.createCell(5).setCellValue("Суммарный вес:");
            Cell weightCell = infoRow.createCell(6);
            weightCell.setCellValue(totalWeights + " кг (" + String.format("%.0f", fillPercent) + "%)");
            weightCell.setCellStyle(weightStyle);
            infoRow.createCell(7).setCellValue("");

            // Таблица маршрута
            Row tableHeaderRow = sheet.createRow(currentRow++);
            String[] headers = {"№", "Адрес доставки", "Вес точки (кг)", "Дистанция участка (км)",
                    "Накопленная дистанция (км)", "Примерное время до точки"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = tableHeaderRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            double cumulativeDistance = 0;
            double cumulativeTime = 0;

            for (int i = 0; i < points.size(); i++) {
                Row row = sheet.createRow(currentRow++);
                row.createCell(0).setCellValue(i + 1);

                Cell addressCell = row.createCell(1);
                addressCell.setCellValue(addresses.get(i));
                addressCell.setCellStyle(addressStyle);

                Cell weightPointCell = row.createCell(2);
                weightPointCell.setCellValue(weights.get(i));

                List<double[]> segment = (i != 0) ? List.of(points.get(i - 1), points.get(i))
                        : List.of(new double[]{55.592605, 37.747183}, points.get(i));

                double segmentDistance = RouteDistance.getRouteDistance(segment);
                double segmentTime = Styles.calculateSegmentTime(segmentDistance);

                Cell segmentCell = row.createCell(3);
                segmentCell.setCellValue(segmentDistance / 1000);
                segmentCell.setCellStyle(distanceStyle);

                cumulativeDistance += segmentDistance;
                cumulativeTime += segmentTime;

                Cell cumDistCell = row.createCell(4);
                cumDistCell.setCellValue(cumulativeDistance / 1000);
                cumDistCell.setCellStyle(distanceStyle);

                Cell timeToPointCell = row.createCell(5);
                timeToPointCell.setCellValue(cumulativeTime);
                timeToPointCell.setCellStyle(timeStyle);
            }

            currentRow++;
        }

        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(filepath)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }
    }
}
