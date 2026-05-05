package automation.clustering.excel;

import org.apache.poi.ss.usermodel.*;
import java.awt.Color;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

public class Styles {

    private static final String[] DRIVER_COLORS = {"#FF0000", "#0000FF", "#00FF00", "#FFA500", "#800080"};

    static Font boldText(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        return font;
    }

    static CellStyle createHeadersStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);

        style.setFont(boldText(workbook));

        return style;
    }

    static CellStyle createCenterText(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);

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

    static CellStyle createAddressStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(true);
        return style;
    }

    static CellStyle createDriverStyle(Workbook workbook, int driverId) {
        String hexColor = DRIVER_COLORS[driverId % DRIVER_COLORS.length];
        XSSFColor driverColor = Styles.createColor(hexColor);

        XSSFCellStyle driverCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        driverCellStyle.cloneStyleFrom(createDriverStyle(workbook));
        driverCellStyle.setFillForegroundColor(driverColor);
        driverCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return driverCellStyle;
    }


    public static XSSFColor createColor(String hexColor) {
        Color awtColor = Color.decode(hexColor);
        byte[] rgb = new byte[]{(byte) awtColor.getRed(), (byte) awtColor.getGreen(), (byte) awtColor.getBlue()};
        return new XSSFColor(rgb, null);
    }
}
