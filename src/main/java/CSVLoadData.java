import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class CSVLoadData {

    private static final String DEFAULT_DATA_DIR = "/Users/oleg/Project/crocotime/cvsTest/data";
    private static AgentActivityWriterExt activityWriter;
    private static Set<String> dataFile;
    private static StringBuilder sbDataFile;
    private static String CSVFileName;


    public static void main(String[] args) {
        Path queueDir = Paths.get(AgentActivityWriterExt.DEFAULT_AGENT_ACTIVITY_QUEUE_DIR);
        AgentActivityFileWorkerExt fileWorkerExt = new AgentActivityFileWorkerExt(queueDir);
        activityWriter = new AgentActivityWriterExt();

        sbDataFile = new StringBuilder();

        try {
            fileWorkerExt.forEachFile((zipFilePath, agentRequestUtcTime, dirTime) -> {
                CSVFileName = dirTime;
                try {
                    sbDataFile.append(activityWriter.write(zipFilePath, agentRequestUtcTime));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return AgentActivityFileWorkerExt.ActionResult.CONTINUE;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            createCvs(sbDataFile, CSVFileName);
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
    private static void createCvs(StringBuilder data, String fileName) throws IOException {
        File file = new File(DEFAULT_DATA_DIR + "/" + fileName + ".csv");

        try {
            FileUtils.writeStringToFile(file, data.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
