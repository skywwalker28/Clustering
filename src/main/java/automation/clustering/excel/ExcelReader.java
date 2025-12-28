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
        List<DeliveryPoint> points = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                boolean firstRow = true;
                for(Row row :sheet) {
                    if (firstRow) {
                        firstRow = false;
                        continue;
                    }
                    Cell addressCell = row.getCell(0);
                    Cell weightCell = row.getCell(1);

                    if (addressCell == null || weightCell == null) continue;

                    String rawAddress = row.getCell(0).getStringCellValue().trim();
                    String cleanAddress = CleanAddress.cleanAddress(rawAddress);

                    String rowWeight = weightCell.getStringCellValue().replaceAll("[^0-9]", "");
                    int weight = Integer.parseInt(rowWeight);


                    points.add(new DeliveryPoint(cleanAddress, weight));
                }
            }
        return points;
    }
}
