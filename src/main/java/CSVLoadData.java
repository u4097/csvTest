import java.nio.file.Path;
import java.nio.file.Paths;

public class CSVLoadData {

    private static AgentActivityWriterExt activityWriter;
    private static StringBuilder sbDataFile;


    public static void main(String[] args) {
        Path queueDir = Paths.get(AgentActivityWriterExt.DEFAULT_AGENT_ACTIVITY_QUEUE_DIR);
        AgentActivityFileWorkerExt fileWorkerExt = new AgentActivityFileWorkerExt(queueDir);
        activityWriter = new AgentActivityWriterExt();

        sbDataFile = new StringBuilder();

        try {
            fileWorkerExt.forEachFile((zipFilePath, agentRequestUtcTime, dirTime) -> {
                try {
                    sbDataFile = activityWriter.write(zipFilePath, agentRequestUtcTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return new AgentActivityFileWorkerExt.ActionResult(false, false, sbDataFile);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
