package automation.clustering.excel;

import automation.clustering.distance.RouteDistance;
import org.apache.poi.ss.usermodel.*;

import java.util.List;
import java.util.Map;
import java.io.FileOutputStream;

public class Sheets {
    public static void createSummarySheet(Workbook workbook, Map<Integer, List<double[]>> driverRoutes,
                                          CellStyle headerStyle, CellStyle driverStyle) {
        Sheet sheet = workbook.createSheet("Summary");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Driver", "Vehicle ID", "Points",
                "Distance (km)", "Avg Distance per Point (km)", "Status"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        double totalDistanceAll = 0;
        int totalPointsAll = 0;
        CellStyle distanceStyle = ExcelExporter.createDistanceStyle(workbook);

        for (Map.Entry<Integer, List<double[]>> entry : driverRoutes.entrySet()) {
            int vehicleId = entry.getKey();
            int driverNumber = vehicleId + 1;
            List<double[]> points = entry.getValue();
            int pointCount = points.size();

            double distance = 0;
            try {
                distance = RouteDistance.getRouteDistance(points);
            } catch (Exception e) {
                System.err.println("Error calculating distance for driver: " + driverNumber);
            }

            Row row = sheet.createRow(rowNum++);

            Cell cell0 = row.createCell(0);
            cell0.setCellValue("Driver " + driverNumber);
            cell0.setCellStyle(driverStyle);

            row.createCell(1).setCellValue(vehicleId);
            row.createCell(2).setCellValue(pointCount);

            Cell cell3 = row.createCell(3);
            cell3.setCellValue(distance / 1000);
            cell3.setCellStyle(distanceStyle);

            if (pointCount > 1) {
                double avgDistance = (distance / 1000) / (pointCount - 1);
                row.createCell(4).setCellValue(String.format("%.2f", avgDistance));
            } else {
                row.createCell(4).setCellValue("N/A");
            }

            String status = pointCount >= 5 ? "Optimal" : pointCount >= 3 ? "Good" : "Light";
            row.createCell(5).setCellValue(status);

            totalDistanceAll += distance;
            totalPointsAll += pointCount;
        }

        Row totalRow = sheet.createRow(rowNum + 1);
        totalRow.createCell(0).setCellValue("TOTAL");
        totalRow.createCell(2).setCellValue(totalPointsAll);

        Cell totalDistance = totalRow.createCell(3);
        totalDistance.setCellValue(totalDistanceAll / 1000);
        totalDistance.setCellStyle(distanceStyle);

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }


    public static void createDriverSheet(Workbook workbook, int driverId, List<double[]> points,
                                              CellStyle headerStyle, CellStyle distanceStyle,
                                              CellStyle coordinateStyle) {

        int driverNumber = driverId + 1;
        String sheetName = "Driver " + driverNumber;
        if (sheetName.length() > 31) {
            sheetName = sheetName.substring(0, 31);
        }

        Sheet sheet = workbook.createSheet(sheetName);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Driver " + driverNumber + " - Detailed Route");
        titleCell.setCellStyle(headerStyle);

        Row headerRow = sheet.createRow(2);
        String[] headers = {"#", "Latitude", "Longitude",
                "Segment Distance (km)", "Cumulative Distance (km)", "Notes"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        double cumulativeDistance = 0;

        for (int i = 0; i < points.size(); i++) {
            int rowNum = i + 3;
            Row row = sheet.createRow(rowNum);

            double[] point = points.get(i);

            row.createCell(0).setCellValue(i + 1);

            Cell latCell = row.createCell(1);
            latCell.setCellValue(point[0]);
            latCell.setCellStyle(coordinateStyle);

            Cell lonCell = row.createCell(2);
            lonCell.setCellValue(point[1]);
            lonCell.setCellStyle(coordinateStyle);

            if (i > 0) {
                try {
                    double[] prevPoint = points.get(i - 1);
                    List<double[]> segment = List.of(prevPoint, point);
                    double segmentDistance = RouteDistance.getRouteDistance(segment);

                    Cell segCell = row.createCell(3);
                    segCell.setCellValue(segmentDistance / 1000);
                    segCell.setCellStyle(distanceStyle);

                    cumulativeDistance += segmentDistance;

                    Cell cumCell = row.createCell(4);
                    cumCell.setCellValue(cumulativeDistance / 1000);
                    cumCell.setCellStyle(distanceStyle);
                } catch (Exception e) {
                    row.createCell(3).setCellValue("Error");
                    row.createCell(4).setCellValue("Error");
                }

            } else {
                row.createCell(3).setCellValue("Start");
                row.createCell(4).setCellValue(0);
            }
        }

        Row totalRow = sheet.createRow(points.size() + 4);
        totalRow.createCell(0).setCellValue("TOTAL");

        Cell totalCell = totalRow.createCell(4);
        totalCell.setCellValue(cumulativeDistance / 1000);
        totalCell.setCellStyle(distanceStyle);

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        sheet.createFreezePane(0, 3);
    }
}
