package utils.csv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CsvUtils {

    public static Path buildCsvFile(byte[] csvContent) throws IOException {
        Path csvPath = Files.createTempFile("_temp", ".csv");
        csvPath.toFile();
        Files.copy(new ByteArrayInputStream(csvContent), csvPath, StandardCopyOption.REPLACE_EXISTING);
        return csvPath;
    }

    public static Path buildCsvFile(String csvContent) throws IOException {
        return buildCsvFile(csvContent.getBytes(StandardCharsets.UTF_8));
    }
}
