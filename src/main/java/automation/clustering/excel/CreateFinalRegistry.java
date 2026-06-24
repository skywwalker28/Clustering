package automation.clustering.excel;

import automation.clustering.model.DeliveryPoint;
import automation.clustering.model.Driver;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class CreateFinalRegistry {

    private static final String[] DRIVER_COLORS = {"#00aa00", "#ff0000", "#0000ff", "#ff6600", "#9900cc"};

    public static void exportToExcelOriginalFormat(
            String inputFilePath,
            String outputFilePath,
            Map<Integer, List<DeliveryPoint>> driverAndPoints,
            ArrayList<Driver> driverInfo) throws Exception {

        ZipSecureFile.setMinInflateRatio(0.001);

        try (FileInputStream fis = new FileInputStream(inputFilePath);
             XSSFWorkbook originalWorkbook = new XSSFWorkbook(fis)) {

            Sheet originalSheet = originalWorkbook.getSheetAt(0);
            Row headerRow = originalSheet.getRow(0);
            Map<Integer, Row> orderNumberToRow = new LinkedHashMap<>();

            for (int i = 1; i <= originalSheet.getLastRowNum(); i++) {
                Row row = originalSheet.getRow(i);
                if (row == null) continue;

                Cell numberCell = row.getCell(1);
                if (numberCell != null && numberCell.getCellType() == CellType.NUMERIC) {
                    int orderNumber = (int) numberCell.getNumericCellValue();
                    orderNumberToRow.put(orderNumber, row);
                }
            }

            try (XSSFWorkbook newWorkbook = new XSSFWorkbook()) {
                Sheet newSheet = newWorkbook.createSheet("ИМТЭК");

                Row newHeader = newSheet.createRow(0);
                newHeader.setHeightInPoints(30);
                copyRowWithExactStyle(originalWorkbook, newWorkbook, headerRow, newHeader,
                        -1, null, null, false);

                int targetRowNum = 1;
                List<int[]> driverRanges = new ArrayList<>();

                List<Integer> sortedDriverIds = new ArrayList<>(driverAndPoints.keySet());
                Collections.sort(sortedDriverIds);

                int driverColorIndex = 0;
                int driverIndex = 0;
                for (int driverId : sortedDriverIds) {
                    List<DeliveryPoint> points = driverAndPoints.get(driverId);
                    if (points == null || points.isEmpty()) continue;

                    String driverColorHex = DRIVER_COLORS[driverColorIndex % DRIVER_COLORS.length];
                    driverColorIndex++;

                    Driver currentDriver = driverInfo.get(driverIndex++);

                    int startRow = targetRowNum;
                    for (DeliveryPoint point : points) {
                        Row originalRow = orderNumberToRow.get(point.getNumber());

                        Row newRow = newSheet.createRow(targetRowNum++);

                        // --- ЗАДАЕМ ФИКСИРОВАННУЮ ВЫСОТУ СТРОКИ ---
                        // Стандартная высота в Excel обычно около 15. Мы ставим 45 для просторности.
                        newRow.setHeightInPoints(65);

                        boolean isFirst = (point == points.get(0));

                        if (originalRow != null)
                            copyRowWithExactStyle(originalWorkbook, newWorkbook, originalRow, newRow,
                                    point.getNumber(), driverColorHex, currentDriver, isFirst);
                        else {
                            copyRowWithExactStyle(originalWorkbook, newWorkbook,
                                    originalSheet.getRow(1), newRow, point.getNumber(),
                                    driverColorHex, currentDriver, isFirst);

                            for (int c = 1; c < 10; c++) {
                                Cell cell = newRow.getCell(c);
                                if (cell != null) {
                                    cell.setCellValue("");
                                }
                            }

                            Cell cellNum = newRow.getCell(1);
                            cellNum.setCellValue(point.getNumber());

                            Cell cellAddress = newRow.getCell(2);
                            cellAddress.setCellValue(point.getAddress());

                            Cell cellPallets = newRow.getCell(6);
                            cellPallets.setBlank();
                            cellPallets.setCellValue((double) point.getWeightKg() / 500);

                            Cell cellWeight = newRow.getCell(7);
                            cellWeight.setCellValue(point.getWeightKg());
                        }
                    }

                    int endRow = targetRowNum - 1;
                    driverRanges.add(new int[]{startRow, endRow});
                }

                for (int[] range : driverRanges) {
                    if (range[1] - range[0] >= 1) {
                        for (int i = 10; i <= 16; i++) {
                            newSheet.addMergedRegion(new CellRangeAddress(range[0], range[1], i, i));
                        }
                    }
                }

                // Умеренная ширина колонок, чтобы текст не был слишком сжат
                for (int i = 0; i <= 16; i++) {
                    int originalWidth = originalSheet.getColumnWidth(i);
                    // Расширяем колонку с адресом (индекс 2), чтобы туда влезало больше текста на строку
                    if (i == 2) {
                        newSheet.setColumnWidth(i, Math.max(originalWidth, 45 * 256));
                    } else if (i >= 10 && i <= 12) {
                        newSheet.setColumnWidth(i, Math.max(originalWidth, 25 * 256));
                    } else {
                        newSheet.setColumnWidth(i, Math.max(originalWidth, 18 * 256));
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                    newWorkbook.write(fos);
                }

                System.out.println("Excel сохранён (высокие ячейки с выравниванием): " + outputFilePath);
            }
        }
    }

    private static void copyRowWithExactStyle(XSSFWorkbook sourceWorkbook,
                                              XSSFWorkbook targetWorkbook,
                                              Row source,
                                              Row target,
                                              int orderNumber,
                                              String driverColorHex,
                                              Driver currentDriver,
                                              boolean isFirst) {

        for (int i = 0; i < source.getLastCellNum(); i++) {
            Cell sourceCell = source.getCell(i);

            if (sourceCell != null) {
                Cell targetCell = target.createCell(i);

                if (i == 13 && isFirst) targetCell.setCellValue(0.375);
                else copyCellValue(sourceCell, targetCell);

                XSSFCellStyle sourceStyle = (XSSFCellStyle) sourceCell.getCellStyle();
                XSSFCellStyle newStyle = targetWorkbook.createCellStyle();

                newStyle.cloneStyleFrom(sourceStyle);
                newStyle.setDataFormat(sourceStyle.getDataFormat());

                // --- ЦЕНТРИРУЕМ ТЕКСТ ПО ВЕРТИКАЛИ И РАЗРЕШАЕМ ПЕРЕНОС ---
                newStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                newStyle.setWrapText(true); // Чтобы длинные адреса переносились на новую строку внутри ячейки

                XSSFColor sourceFillColor = sourceStyle.getFillForegroundColorColor();
                if (sourceFillColor != null) {
                    XSSFColor newFillColor = new XSSFColor(sourceFillColor.getRGB());
                    newFillColor.setTint(sourceFillColor.getTint());
                    newStyle.setFillForegroundColor(newFillColor);
                    newStyle.setFillPattern(sourceStyle.getFillPattern());
                }

                XSSFFont sourceFont = sourceWorkbook.getFontAt(sourceStyle.getFontIndex());
                XSSFFont newFont = targetWorkbook.createFont();

                newFont.setBold(sourceFont.getBold());
                newFont.setFontHeight(sourceFont.getFontHeight());
                newFont.setFontName(sourceFont.getFontName());
                newFont.setItalic(sourceFont.getItalic());
                newFont.setStrikeout(sourceFont.getStrikeout());
                newFont.setTypeOffset(sourceFont.getTypeOffset());
                newFont.setUnderline(sourceFont.getUnderline());

                XSSFColor sourceFontColor = sourceFont.getXSSFColor();
                if (sourceFontColor != null) {
                    XSSFColor newFontColor = new XSSFColor(sourceFontColor.getRGB());
                    newFontColor.setTint(sourceFontColor.getTint());
                    newFont.setColor(newFontColor);
                } else {
                    newFont.setColor(sourceFont.getColor());
                }

                newStyle.setFont(newFont);

                if (i == 1 && orderNumber != -1 && driverColorHex != null) {
                    DataFormat format = targetWorkbook.createDataFormat();
                    newStyle.setDataFormat(format.getFormat("0"));

                    XSSFColor driverColor = createColorFromHex(driverColorHex);
                    newStyle.setFillForegroundColor(driverColor);
                    newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    XSSFFont whiteFont = targetWorkbook.createFont();
                    whiteFont.setColor(
                            new XSSFColor(new byte[]{
                                    (byte) 255,
                                    (byte) 255,
                                    (byte) 255},
                                    null
                            ));
                    whiteFont.setFontHeight(newFont.getFontHeight());
                    whiteFont.setFontName(newFont.getFontName());
                    newStyle.setFont(whiteFont);
                }

                if (i == 13) {
                    CreationHelper createHelper = targetWorkbook.getCreationHelper();
                    newStyle.setDataFormat(createHelper.createDataFormat().getFormat("h:mm"));
                }

                targetCell.setCellStyle(newStyle);
            }
        }

        if (currentDriver != null) {
            Cell vehicleTC = target.getCell(10);
            if (vehicleTC == null) vehicleTC = target.createCell(10);
            vehicleTC.setCellValue(currentDriver.getDriverData().getVehicleNumber());

            Cell fio = target.getCell(11);
            if (fio == null) fio = target.createCell(11);
            fio.setCellValue(currentDriver.getDriverData().getDriverName());

            Cell phone = target.getCell(12);
            if (phone == null) phone = target.createCell(12);
            phone.setCellValue(currentDriver.getDriverData().getPhone());

            Cell carrier = target.getCell(15);
            if (carrier == null) carrier = target.createCell(15);
            carrier.setCellValue(currentDriver.getDriverData().getCarrier());

            Cell tarrif = target.getCell(16);
            if (tarrif == null) tarrif = target.createCell(16);
            tarrif.setCellValue(currentDriver.getDriverData().getTariff());
        }
    }

    private static void copyCellValue(Cell source, Cell target) {
        switch (source.getCellType()) {
            case STRING:
                target.setCellValue(source.getStringCellValue());
                break;
            case NUMERIC:
                target.setCellValue(source.getNumericCellValue());
                break;
            case BOOLEAN:
                target.setCellValue(source.getBooleanCellValue());
                break;
            case FORMULA:
                target.setCellFormula(source.getCellFormula());
                break;
            default:
                target.setCellValue("");
        }
    }

    private static XSSFColor createColorFromHex(String hexColor) {
        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);
        return new XSSFColor(new byte[]{(byte) r, (byte) g, (byte) b}, null);
    }
}