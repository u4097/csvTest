import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CSVLoadData {

    private static AgentActivityWriterExt activityWriter;

    public static void main(String[] args) {
        Path queueDir = Paths.get(AgentActivityWriterExt.DEFAULT_AGENT_ACTIVITY_QUEUE_DIR);
        AgentActivityFileWorkerExt fileWorkerExt = new AgentActivityFileWorkerExt(queueDir);
        activityWriter = new AgentActivityWriterExt();


        try {
            AgentActivityFileWorkerExt.Action action = (zipFilePath, agentRequestUtcTime) -> {
                Long computerAccountId = -1L;
                //Todo: Make cvs file and write info from zip activity file
                // 1) read zip file and get info
                // 2) create cvs file and write info
                try {
                    activityWriter.write(zipFilePath, agentRequestUtcTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new AgentActivityFileWorkerExt.ActionResult(true, false);
            };
            fileWorkerExt.forEachFile(action);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
