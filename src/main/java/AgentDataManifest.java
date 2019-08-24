import net.minidev.json.JSONObject;

public class AgentDataManifest extends UserData {

    static final String FILE_NAME = "manifest.json";
    private final String version;
    private final Agent agent;

    private AgentDataManifest(String version, User user, Computer computer, Agent agent) {
        super(user, computer);
        this.version = version;
        this.agent = agent;
    }

    public static AgentDataManifest build(Object jsonObj) throws Exception {
        checkInstanceOf(FILE_NAME, JSONObject.class, jsonObj);
        final JSONObject obj = (JSONObject) jsonObj;
        checkRequired(FILE_NAME, obj);

        return new AgentDataManifest(
                getRequiredValueOf("version", String.class, obj),
                User.build(getRequiredValueOf("user", JSONObject.class, obj)),
                Computer.build(getRequiredValueOf("computer", JSONObject.class, obj)),
                Agent.build(getRequiredValueOf("agent", JSONObject.class, obj)));
    }

    static final class WindowSwitchingCsv {

        static final String FILE_NAME = "window_switching.csv";

        static final int COLUMN_COUNT = 6;

        static final int COLUMN_ACCOUNT_ID = 0;
        static final int COLUMN_TIME = 1;
        static final int COLUMN_PROGRAM = 2;
        static final int COLUMN_WINDOW = 3;
        static final int COLUMN_URL = 4;
        static final int COLUMN_UI_HIERARCHY = 5;

        private WindowSwitchingCsv() {
        }
    }

    static final class HidPeriodsCsv {

        static final String HARDWARE_FILE_NAME = "hardware_hid_periods.csv";
        static final String INJECTED_FILE_NAME = "injected_hid_periods.csv";

        static final int COLUMN_COUNT = 2;

        static final int COLUMN_BEGIN_TIME = 0;
        static final int COLUMN_END_TIME = 1;

        private HidPeriodsCsv() {
        }
    }

    public static class Agent {

        final String version;

        Agent(String version) {
            this.version = version;
        }

        static Agent build(JSONObject obj) throws Exception {
            return new Agent(getValueOf("version", String.class, obj));
        }

        public static Agent build(String version) throws Exception {
            return new Agent(version);
        }
    }
}