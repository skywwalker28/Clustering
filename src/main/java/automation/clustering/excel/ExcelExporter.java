package automation.clustering.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelExporter {
    public static void exportToExcel(Map<Integer, List<double[]>> driverRoutes, String filePath) throws Exception  {
        System.out.println("\nCreating Excel report: " + filePath);

        Workbook workbook = new XSSFWorkbook();

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle driverStyle = createDriverStyle(workbook);
        CellStyle distanceStyle =createDistanceStyle(workbook);
        CellStyle coordinateStyle = createCoordinateStyle(workbook);

        Sheets.createSummarySheet(workbook, driverRoutes, headerStyle, driverStyle);

        for (Map.Entry<Integer, List<double[]>> entry : driverRoutes.entrySet()) {
            int driverId = entry.getKey();
            List<double[]> points = entry.getValue();

            if (!points.isEmpty()) {
                Sheets.createDriverSheet(workbook, driverId, points, headerStyle, distanceStyle, coordinateStyle);
            }
        }

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            System.out.println("Excel file created successfully");
            System.out.println("Location: " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving Excel file: " + e.getMessage());
            throw e;
        } finally {
            workbook.close();
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setFont(font);
        return style;
    }

    private static CellStyle createDriverStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle createCoordinateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.000000"));
        return style;
    }

    static CellStyle createDistanceStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        Font font = workbook.createFont();
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFont(font);
        return style;
    }
}
