import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class CSVLoadData {

    private static final String DEFAULT_DATA_DIR = "/Users/oleg/Project/crocotime/cvsTest/data";
    private static AgentActivityWriterExt activityWriter;

    public static void main(String[] args) {
        Path queueDir = Paths.get(AgentActivityWriterExt.DEFAULT_AGENT_ACTIVITY_QUEUE_DIR);
        AgentActivityFileWorkerExt fileWorkerExt = new AgentActivityFileWorkerExt(queueDir);
        activityWriter = new AgentActivityWriterExt();
        Map<String, Set> dataActivity = new HashMap<>();


        try {
            fileWorkerExt.forEachFile((zipFilePath, agentRequestUtcTime) -> {
                Set<String> data = new LinkedHashSet<>();
                Long computerAccountId = -1L;
                //Todo: Make cvs file and write info from zip activity file
                // 1) read zip file and get info
                // 2) create cvs file and write info
                try {
                    data = activityWriter.write(zipFilePath, agentRequestUtcTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dataActivity.put(String.valueOf(zipFilePath.getFileName()), data);

                return AgentActivityFileWorkerExt.ActionResult.CONTINUE;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            createCvs(dataActivity);
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
    private static void createCvs(Map<String, Set> data) throws IOException {
        FileWriter out = new FileWriter(DEFAULT_DATA_DIR + "/" + "activity.csv");

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
