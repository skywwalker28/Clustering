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

            Row headerRow = originalSheet.getRow(1);

            Map<Integer, Row> orderNumberToRow = new LinkedHashMap<>();
            Row firstDataRow = null;

            for (int i = 2; i <= originalSheet.getLastRowNum(); i++) {
                Row row = originalSheet.getRow(i);
                if (row == null) continue;

                Cell numberCell = row.getCell(1);
                if (numberCell != null && numberCell.getCellType() == CellType.NUMERIC) {
                    int orderNumber = (int) numberCell.getNumericCellValue();
                    orderNumberToRow.put(orderNumber, row);
                    if (firstDataRow == null) {
                        firstDataRow = row;
                    }
                }
            }

            try (XSSFWorkbook newWorkbook = new XSSFWorkbook()) {
                Sheet newSheet = newWorkbook.createSheet("ИМТЭК");

                Row newHeader = newSheet.createRow(0);
                newHeader.setHeightInPoints(30);

                if (headerRow != null) {
                    copyRowWithExactStyle(originalWorkbook, newWorkbook, headerRow, newHeader,
                            -1, null, null, false);
                }

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

                        newRow.setHeightInPoints(65);
                        boolean isFirst = (point == points.get(0));

                        if (originalRow != null) {
                            copyRowWithExactStyle(originalWorkbook, newWorkbook, originalRow, newRow,
                                    point.getNumber(), driverColorHex, currentDriver, isFirst);
                        } else {
                            Row templateRow = (firstDataRow != null) ? firstDataRow : originalSheet.getRow(2);

                            copyRowWithExactStyle(originalWorkbook, newWorkbook,
                                    templateRow, newRow, point.getNumber(),
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

                            Cell cellWeight = newRow.getCell(7);
                            cellWeight.setCellValue(point.getWeightKg());
                        }

                        // --- ИСПРАВЛЕНИЕ ФОРМУЛЫ ---
                        Cell cellG = newRow.getCell(6);
                        if (cellG == null) cellG = newRow.createCell(6);

                        int excelRowNum = newRow.getRowNum() + 1;
                        cellG.setCellFormula("H" + excelRowNum + "/500");
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

                for (int i = 0; i <= 16; i++) {
                    int originalWidth = originalSheet.getColumnWidth(i);
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

                if (i == 13 && isFirst && orderNumber != -1) targetCell.setCellValue(0.375);
                else copyCellValue(sourceCell, targetCell);

                XSSFCellStyle sourceStyle = (XSSFCellStyle) sourceCell.getCellStyle();
                XSSFCellStyle newStyle = targetWorkbook.createCellStyle();

                // Базовое копирование структуры стиля
                newStyle.cloneStyleFrom(sourceStyle);
                newStyle.setDataFormat(sourceStyle.getDataFormat());

                // Применяем специфичные форматы данных ТОЛЬКО к строкам с заказами (не к шапке)
                if (orderNumber != -1) {
                    if (i == 0) {
                        CreationHelper createHelper = targetWorkbook.getCreationHelper();
                        newStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.mm.yyyy"));
                    }
                    if (i == 7) {
                        CreationHelper createHelper = targetWorkbook.getCreationHelper();
                        newStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.00"));
                    }
                    if (i == 13) {
                        CreationHelper createHelper = targetWorkbook.getCreationHelper();
                        newStyle.setDataFormat(createHelper.createDataFormat().getFormat("h:mm"));
                    }
                }

                newStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                newStyle.setWrapText(true);

                XSSFColor sourceFillColor = sourceStyle.getFillForegroundColorColor();
                XSSFColor clonedFill = cloneColor(sourceFillColor);
                if (clonedFill != null) {
                    newStyle.setFillForegroundColor(clonedFill);
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
                XSSFColor clonedFontColor = cloneColor(sourceFontColor);
                if (clonedFontColor != null) {
                    newFont.setColor(clonedFontColor);
                } else {
                    newFont.setColor(sourceFont.getColor());
                }

                newStyle.setFont(newFont);

                // Кастомная закраска ID заказа цветом водителя (только для данных)
                if (i == 1 && orderNumber != -1 && driverColorHex != null) {
                    DataFormat format = targetWorkbook.createDataFormat();
                    newStyle.setDataFormat(format.getFormat("0"));

                    XSSFColor driverColor = createColorFromHex(driverColorHex);
                    newStyle.setFillForegroundColor(driverColor);
                    newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    XSSFFont whiteFont = targetWorkbook.createFont();
                    whiteFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
                    whiteFont.setFontHeight(newFont.getFontHeight());
                    whiteFont.setFontName(newFont.getFontName());
                    whiteFont.setBold(true);
                    newStyle.setFont(whiteFont);
                }

                targetCell.setCellStyle(newStyle);
            }
        }

        // Заполнение данных водителя (только для строк данных)
        if (currentDriver != null && orderNumber != -1) {
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

    private static XSSFColor cloneColor(XSSFColor sourceColor) {
        if (sourceColor == null) return null;

        XSSFColor newColor = new XSSFColor(new byte[]{0, 0, 0}, null);
        if (sourceColor.getRGB() != null) {
            newColor.setRGB(sourceColor.getRGB());
        } else if (sourceColor.getTheme() >= 0) {
            newColor.setTheme(sourceColor.getTheme());
        } else {
            return null;
        }

        if (sourceColor.getTint() != 0.0) {
            newColor.setTint(sourceColor.getTint());
        }
        return newColor;
    }
}