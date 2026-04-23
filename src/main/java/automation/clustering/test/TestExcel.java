package automation.clustering.test;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

class TestExcel {


    public static void main(String[] args) {
        String test = System.getProperty("user.home") + "/Downloads";
        File downloadDir = new File(test);

        File[] excelFiles = downloadDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".xlsx") || name.toLowerCase().endsWith(".xls"));

        Optional<File> latestFile = Arrays.stream(excelFiles).max(Comparator.comparingLong(File::lastModified));


        System.out.println(latestFile.get().getAbsolutePath());
    }
}