package automation.clustering.excel;

import automation.clustering.model.DeliveryPoint;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import static automation.clustering.distance.OrsDistance.getRouteDistanceFromAPI;
import static automation.clustering.distance.Calculate.calculateTime;


public class ExcelExporter {
    public static void exportToExcelSingleSheet(
            String filepath,
            Map<Integer, List<DeliveryPoint>> driverAndPoint,
            Map<Integer, List<double[]>> driverAndCoordinate) throws Exception {
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

        for (Map.Entry<Integer, List<DeliveryPoint>> entry : driverAndPoint.entrySet()) {
            int driverId = entry.getKey();
            int driverNumber = driverId + 1;
            List<DeliveryPoint> points = entry.getValue();
            List<double[]> coordinates = driverAndCoordinate.get(driverId);

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
            int totalWeights = points.stream().mapToInt(DeliveryPoint::getWeightKg).sum();

            for (int i = 0; i < points.size(); i++) {
                DeliveryPoint currentPoint = points.get(i);
                Row row = sheet.createRow(currentRow++);

                Cell numberCell = row.createCell(0);
                numberCell.setCellValue(currentPoint.getNumber());
                numberCell.setCellStyle(centerText);

                Cell addressCell = row.createCell(1);
                addressCell.setCellValue(currentPoint.getAddress());
                addressCell.setCellStyle(addressStyle);

                Cell weightPointCell = row.createCell(2);

                weightPointCell.setCellValue(currentPoint.getWeightKg());
                weightPointCell.setCellStyle(centerText);

                List<double[]> segment = (i != 0) ? List.of(coordinates.get(i - 1), coordinates.get(i))
                        : List.of(new double[]{55.592605, 37.747183}, coordinates.get(i));

                double[] distanceAndDuration = getRouteDistanceFromAPI(segment);

                double segmentDistance = distanceAndDuration[0];
                double segmentTime = distanceAndDuration[1];

                totalDistance += distanceAndDuration[0];
                totalTime += segmentTime + 15 + 10;

                Cell segmentCell = row.createCell(3);

                if (segmentDistance / 1000 > 0) segmentCell.setCellValue((int) (segmentDistance / 1000));
                else segmentCell.setCellValue((int) (segmentDistance));
                segmentCell.setCellStyle(centerText);

                Cell timeToPointCell = row.createCell(4);
                timeToPointCell.setCellValue((int) segmentTime + 15 + 10);
                timeToPointCell.setCellStyle(centerText);
            }

            String[] totalHeaders = {"", "", totalWeights + " кг",
                    String.format("%.0f", totalDistance / 1000) + " км", calculateTime(totalTime)};

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


