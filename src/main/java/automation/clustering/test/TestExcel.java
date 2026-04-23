package automation.clustering.test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

class TestExcel {
    public static void main(String[] args) {

        try (FileInputStream file = new FileInputStream("/Users/skywalker/Downloads/22.04 Артур.xlsx")) {
            Workbook workbook = new XSSFWorkbook(file);

            Sheet page = workbook.getSheetAt(0);

            Row row = page.getRow(0);

            Cell cell = row.getCell(6);


            int number = (int) cell.getNumericCellValue();

            System.out.println(number);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}