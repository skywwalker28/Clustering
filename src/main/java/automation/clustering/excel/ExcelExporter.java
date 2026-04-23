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
                                                Map<Integer, List<Integer>> driverWeights,
                                                Map<Integer, List<Integer>> pointNumber,
                                                String filepath) throws Exception {
        Workbook workbook = new XSSFWorkbook();

        CellStyle headersStyle = Styles.createHeadersStyle(workbook);
        CellStyle centerText = Styles.createCenterText(workbook);
        CellStyle driverHeaderStyle = Styles.createDriverStyle(workbook);
        CellStyle addressStyle = Styles.createAddressStyle(workbook);
        CellStyle weightStyle = workbook.createCellStyle();
        weightStyle.cloneStyleFrom(driverHeaderStyle);

        Sheet sheet = workbook.createSheet("Delivery Routes");

        int currentRow = 0;

        Row tableHeaderRow = sheet.createRow(currentRow++);
        String[] headers = {"№", "Адрес доставки", "Вес точки (кг)",
                "Дистанция участка (км)", "Время (мин)"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = tableHeaderRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headersStyle);
        }

        for (Map.Entry<Integer, List<double[]>> entry : driverRoutes.entrySet()) {
            int driverId = entry.getKey();
            int driverNumber = driverId + 1;
            List<double[]> points = entry.getValue();
            List<String> addresses = driverAddresses.get(driverId);
            List<Integer> weights = driverWeights.get(driverId);
            List<Integer> number = pointNumber.get(driverId);

            CellStyle driverColor = Styles.createDriverStyle(workbook, driverId);

            if (points.isEmpty()) continue;

            Row driverHeaderRow = sheet.createRow(currentRow++);
            Cell driverCell = driverHeaderRow.createCell(0);
            driverCell.setCellValue("Водитель: " + driverNumber + " (" + (points.size() > 4 ? points.size()
                    + " точек" : points.size() + " точки") + ")");


            driverCell.setCellStyle(driverColor);
            sheet.addMergedRegion(
                    new CellRangeAddress(currentRow - 1, currentRow - 1, 0, 4));


            double totalDistance = 0.0;
            double totalTime = 60;
            int totalWeights = (weights != null) ? weights.stream().mapToInt(Integer::intValue).sum() : 0;

            for (int i = 0; i < points.size(); i++) {
                Row row = sheet.createRow(currentRow++);

                Cell numberCell = row.createCell(0);
                numberCell.setCellValue(number.get(i));
                numberCell.setCellStyle(centerText);

                Cell addressCell = row.createCell(1);
                addressCell.setCellValue(addresses.get(i));
                addressCell.setCellStyle(addressStyle);

                Cell weightPointCell = row.createCell(2);

                assert weights != null;
                weightPointCell.setCellValue(weights.get(i));
                weightPointCell.setCellStyle(centerText);

                List<double[]> segment = (i != 0) ? List.of(points.get(i - 1), points.get(i))
                        : List.of(new double[]{55.592605, 37.747183}, points.get(i));

                double segmentDistance = RouteDistance.getRouteDistance(segment);
                totalDistance += segmentDistance;
                double segmentTime = Styles.calculateSegmentTime(segmentDistance);

                Cell segmentCell = row.createCell(3);
                segmentCell.setCellValue((int) (segmentDistance / 1000));
                segmentCell.setCellStyle(centerText);

                totalTime += segmentTime + 15;

                Cell timeToPointCell = row.createCell(4);
                timeToPointCell.setCellValue(segmentTime);
                timeToPointCell.setCellStyle(centerText);
            }

            String[] totalHeaders = {"", "", totalWeights + " кг",
                    String.format("%.0f", totalDistance / 1000) + " км", Styles.calculateTime(totalTime)};

            Row totalRow = sheet.createRow(currentRow++);
            sheet.addMergedRegion(new CellRangeAddress(totalRow.getRowNum(),
                    totalRow.getRowNum(), 0, 1));

            Cell total = totalRow.createCell(0);
            total.setCellValue("Итог:");
            total.setCellStyle(headersStyle);

            for (int i = 2; i < totalHeaders.length; i++) {
                Cell cell = totalRow.createCell(i);
                cell.setCellValue(totalHeaders[i]);
                cell.setCellStyle(headersStyle);
            }

            currentRow++;
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(filepath)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }
    }
}


