package automation.clustering.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public class ExcelReadCountDrivers {
    public int getDriverCount(String filepath) {
        int countDrivers = 0;

        try (FileInputStream file = new FileInputStream(filepath)) {
            Workbook workbook = new XSSFWorkbook(file);

            Sheet page = workbook.getSheetAt(0);

            Row row = page.getRow(0);
            Cell cell = row.getCell(17);

            if (cell == null) return countDrivers;

            countDrivers = (int) cell.getNumericCellValue();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return countDrivers;
    }
}
