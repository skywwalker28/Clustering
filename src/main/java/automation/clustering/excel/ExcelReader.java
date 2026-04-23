package automation.clustering.excel;

import automation.clustering.geocoding.CleanAddress;
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
        System.out.println("\ngoing to method readDeliveryPointsFromExcel...");
        List<DeliveryPoint> points = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                int lastIndex = sheet.getLastRowNum();


                for (int i = 2; i <= lastIndex; i++) {

                    Row row = sheet.getRow(i);
                    if (row == null) {
                        System.out.println("row is null");
                        continue;
                    }

                    Cell addressCell = row.getCell(2);
                    Cell weightCell = row.getCell(5);
                    Cell numberCell = row.getCell(1);


                    if (addressCell == null || weightCell == null) {
                        System.out.println("addressCell is null || weightCell is null");
                        continue;
                    }

                    String rawAddress = addressCell.getStringCellValue().trim();
                    String cleanAddress = CleanAddress.cleanAddress(rawAddress);

                    System.out.println(rawAddress + ", " + cleanAddress);

                    int weight = (int) weightCell.getNumericCellValue();
                    int number = (int) numberCell.getNumericCellValue();

                    points.add(new DeliveryPoint(cleanAddress, weight, number));
                }
            }

        return points;
    }
}
