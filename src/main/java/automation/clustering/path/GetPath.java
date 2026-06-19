package automation.clustering.path;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public class GetPath {
    public static String getPathToLatestFile() {
        String startPath = System.getProperty("user.home") + "/Downloads";
        File downloadsDir = new File(startPath);

        File[] allExcelFiles = downloadsDir.listFiles((dir, name) ->
                name.endsWith(".xlsx") || name.endsWith(".xls"));

        if (allExcelFiles == null || allExcelFiles.length == 0) {
            throw new RuntimeException("don't find file in Downloads");
        }

        Optional<File> latestFile = Arrays
                .stream(allExcelFiles)
                .max(Comparator.comparingLong(File::lastModified));
        return latestFile.get().getAbsolutePath();
    }
}