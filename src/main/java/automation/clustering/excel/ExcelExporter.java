package automation.clustering.excel;

import automation.clustering.distance.RouteDistance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    public static void exportToExcelSingleSheet(Map<Integer, List<double[]>> driverRoutes,
                                                Map<Integer, List<String>> driverAddresses,
                                                String filepath) throws Exception {
        Workbook workbook = new XSSFWorkbook();

        CellStyle headerStyle = Styles.createHeaderStyle(workbook);
        CellStyle driverHeaderStyle = Styles.createDriverStyle(workbook);
        CellStyle addressStyle = Styles.createAddressStyleStyle(workbook);
        CellStyle distanceStyle = Styles.createDistanceStyle(workbook);
        CellStyle timeStyle = Styles.createTimeStyle(workbook);

        Sheet sheet = workbook.createSheet("Delivery Routes");

        int currentRow = 0;

        for (Map.Entry<Integer, List<double[]>> entry : driverRoutes.entrySet()) {
            int driverId = entry.getKey();
            int driverNumber = driverId + 1;
            List<double[]> points = entry.getValue();
            List<String> addresses = driverAddresses.get(driverId);

            if (points.isEmpty()) continue;

            Row driverHeaderRow = sheet.createRow(currentRow++);
            Cell driverCell = driverHeaderRow.createCell(0);
            driverCell.setCellValue("Водитель: " + driverNumber);
            driverCell.setCellStyle(driverHeaderStyle);

            sheet.addMergedRegion(
                    new CellRangeAddress(currentRow - 1, currentRow -1, 0, 4));

            Row infoRow = sheet.createRow(currentRow++);

            double totalDistance = RouteDistance.getRouteDistance(points);
            double estimatedTime = Styles.calculateEstimatedTime(totalDistance);

            infoRow.createCell(0).setCellValue("Кол-во точек:");
            infoRow.createCell(1).setCellValue(points.size());
            infoRow.createCell(2).setCellValue("Общая дистанция:");
            Cell distanceCell = infoRow.createCell(3);
            distanceCell.setCellValue(totalDistance / 1000);
            distanceCell.setCellStyle(distanceStyle);
            infoRow.createCell(4).setCellValue("км");

            infoRow.createCell(5).setCellValue("Примерное время:");

            Cell timeCell = infoRow.createCell(6);
            timeCell.setCellValue(estimatedTime);
            timeCell.setCellStyle(timeStyle);
            infoRow.createCell(7).setCellValue("часов");

            Row tableHeaderRow = sheet.createRow(currentRow++);
            String[] headers = {"№", "Адрес доставки", "Дистанция участка (км)",
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
                if (addresses != null && i < addresses.size()) {
                    addressCell.setCellValue(addresses.get(i));
                } else {
                    addressCell.setCellValue("Адрес не указан!");
                }
                addressCell.setCellStyle(addressStyle);

                    try {
                        List<double[]> segment = (i != 0) ? List.of(points.get(i - 1), points.get(i))
                                : List.of(new double[]{55.592605, 37.747183}, points.get(i));

                        double segmentDistance = RouteDistance.getRouteDistance(segment);
                        double segmentTime = Styles.calculateSegmentTime(segmentDistance);

                        Cell segmentCell = row.createCell(2);
                        segmentCell.setCellValue(segmentDistance / 1000);
                        segmentCell.setCellStyle(distanceStyle);

                        cumulativeDistance += segmentDistance;
                        cumulativeTime += segmentTime;

                        Cell cumDistCell = row.createCell(3);
                        cumDistCell.setCellValue(cumulativeDistance / 1000);
                        cumDistCell.setCellStyle(distanceStyle);

                        Cell timeToPointCell = row.createCell(4);
                        timeToPointCell.setCellValue(cumulativeTime);
                        timeToPointCell.setCellStyle(timeStyle);
                    } catch (Exception e) {
                        row.createCell(2).setCellValue("Ошибка");
                        row.createCell(3).setCellValue("Ошибка");
                        row.createCell(4).setCellValue("Ошибка");
                    }

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
