package automation.clustering.excel;

import automation.clustering.model.DeliveryPoint;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {
    public static List<DeliveryPoint> readDeliveryPointsFromExcel(String filePath, int[] allPoints) throws Exception {
        ZipSecureFile.setMinInflateRatio(0.001);
        List<DeliveryPoint> points = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row firstRow = sheet.getRow(0);
            int lastIndex = sheet.getLastRowNum();

            int i;
            Cell firstCell = firstRow.getCell(0);

            if (firstCell == null || firstCell.getCellType() == CellType.BLANK) i = 2;
            else i = 1;

            for (; i <= lastIndex; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    System.out.println("row is null");
                    continue;
                }

                Cell numberCell = row.getCell(1);
                Cell addressCell = row.getCell(2);
                Cell weightCell = row.getCell(7);

                if (numberCell == null || addressCell == null || weightCell == null) continue;

                String rawAddress = addressCell.getStringCellValue().trim();

                int weight = (int) weightCell.getNumericCellValue();
                int number = (int) numberCell.getNumericCellValue();

                points.add(new DeliveryPoint(rawAddress, weight, number));
                allPoints[0]++;
            }
        }

        return points;
    }
}
