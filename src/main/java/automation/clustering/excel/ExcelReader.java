package automation.clustering.excel;

import automation.clustering.model.DeliveryPoint;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {
    public static List<DeliveryPoint> readDeliveryPointsFromExcel(String filePath) throws Exception {
        List<DeliveryPoint> points = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastIndex = sheet.getLastRowNum();

            for (int i = 1; i <= lastIndex; i++) {

                Row row = sheet.getRow(i);
                if (row == null) {
                    System.out.println("row is null");
                    continue;
                }

                Cell numberCell = row.getCell(1);
                Cell addressCell = row.getCell(2);
                Cell weightCell = row.getCell(7);


                if (addressCell == null || weightCell == null) {
                    System.out.println("addressCell is null || weightCell is null");
                    continue;
                }

                String rawAddress = addressCell.getStringCellValue().trim();

                int weight = (int) weightCell.getNumericCellValue();
                int number = (int) numberCell.getNumericCellValue();

                points.add(new DeliveryPoint(rawAddress, weight, number));
            }
        }

        return points;
    }
}
