import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CSVLoadData {

    private static final String DEFAULT_DATA_DIR = "/Users/oleg/Project/crocotime/cvsTest/data";
    private static AgentActivityWriterExt activityWriter;
    private static Set<String> dataFile;
    private static String CSVFileName;


    public static void main(String[] args) {
        Path queueDir = Paths.get(AgentActivityWriterExt.DEFAULT_AGENT_ACTIVITY_QUEUE_DIR);
        AgentActivityFileWorkerExt fileWorkerExt = new AgentActivityFileWorkerExt(queueDir);
        activityWriter = new AgentActivityWriterExt();
        Map<String, Set> dataActivityFiles = new HashMap<>();

        dataFile = new LinkedHashSet<>();

        try {
            fileWorkerExt.forEachFile((zipFilePath, agentRequestUtcTime, dirTime) -> {
                CSVFileName = dirTime;
                // Сформируем имя cvs файла.
                // Получим название первого файла.
                // Получим название последнего файла.
                //Todo: Make cvs file and write info from zip activity file
                // 1) read zip file and get info
                // 2) create cvs file and write info
                try {
                    dataFile = activityWriter.write(zipFilePath, agentRequestUtcTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dataActivityFiles.put(String.valueOf(zipFilePath.getFileName()), dataFile);

                return AgentActivityFileWorkerExt.ActionResult.CONTINUE;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            createCvs(dataActivityFiles, CSVFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Создадим csv файл для записи активности
     *
     * @param data данные для записи
     * @throws IOException
     */
    private static void createCvs(Map<String, Set> data, String fileName) throws IOException {
        //todo  Получить интервал агрегированной активности для формирования названия файла.
        FileWriter out = new FileWriter(DEFAULT_DATA_DIR + "/" + fileName + ".csv");

        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader("computer_account_id", "time", "program", "window", "url", "tab", "ui_hierarchy")
                .withSkipHeaderRecord()
                .withDelimiter(';')
                .withRecordSeparator("\r\n")
                .withQuoteMode(QuoteMode.MINIMAL)
        )) {
            data.forEach((key, value) -> {
                try {
                    printer.printRecord(value);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

    }

}
