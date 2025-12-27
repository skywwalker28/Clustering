package automation.clustering.excel;

import org.apache.poi.ss.usermodel.*;
import java.awt.Color;
import org.apache.poi.xssf.usermodel.XSSFColor;

public class Styles {

    static CellStyle createHeaderStyle(Workbook workbook) {
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

    static CellStyle createDriverStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    static CellStyle createAddressStyleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
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

    static CellStyle createTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.0"));
        Font font = workbook.createFont();
        font.setColor(IndexedColors.DARK_RED.getIndex());
        style.setFont(font);
        return style;
    }

    static double calculateEstimatedTime(double totalDistanceMeters) {
        double averageSpeedKm = 40.0;
        double timePerPointMinutes = 5.0;

        double distanceKm = totalDistanceMeters / 1000;
        double drivingTimeHours = distanceKm / averageSpeedKm;
        double deliveryTimeHours = (3 * timePerPointMinutes) / 60.0;

        return Math.round((drivingTimeHours + deliveryTimeHours) * 10.0) / 10.0;
    }

    static double calculateSegmentTime(double segmentDistanceMeters) {
        double averageSpeedKm = 40.0;
        double distanceKm = segmentDistanceMeters / 1000;
        return Math.round((distanceKm / averageSpeedKm) * 10.0) / 10.0;
    }

    public static XSSFColor createColor(String hexColor) {
        Color awtColor = Color.decode(hexColor);
        byte[] rgb = new byte[]{(byte) awtColor.getRed(), (byte) awtColor.getGreen(), (byte) awtColor.getBlue()};
        return new XSSFColor(rgb, null);
    }
}
