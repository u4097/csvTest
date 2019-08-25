import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class WindowActivityData {

    static final String FILE_NAME = "window_switching.json";
    private final List<WindowActivityElement> windowActivityElement;


    public WindowActivityData(List<WindowActivityElement> windowActivityElement) {
        this.windowActivityElement = windowActivityElement;
    }

    public static WindowActivityData build(Object jsonObj) throws Exception {
        checkInstanceOf(FILE_NAME, JSONObject.class, jsonObj);
        final JSONArray jsonArray = (JSONArray) jsonObj;
        checkRequired(FILE_NAME, jsonArray);

        return new WindowActivityData(WindowActivityElement.build(jsonArray));
    }

    static <T> T getRequiredValueOf(String name, Class<T> clazz, JSONObject obj) throws Exception {
        T value = getValueOf(name, clazz, obj);
        checkRequired(name, value);
        return value;
    }

    static <T> List<T> getRequiredValueOf(String name, Class<T> clazz, JSONArray obj) throws Exception {
        List<T> value = getValueOf(name, clazz, obj);
        checkRequired(name, value);
        return value;
    }

    static <T> List<T> getValueOf(String name, Class<T> clazz, JSONArray obj) throws Exception {
        List value = obj;
//        checkInstanceOf(name, clazz, value);
        return value;
    }

    static <T> T getValueOf(String name, Class<T> clazz, JSONObject obj) throws Exception {
        Object value = obj.get(name);
        checkInstanceOf(name, clazz, value);
        return (T) value;
    }

    static <T> void checkRequired(String name, T value) throws Exception {
        if (value == null) {
//            throw MonitoringExceptionBuilder.buildAgentDataException("Value of \"" + name + "\" must be not null.");
        }
    }

    static void checkInstanceOf(String name, Class<?> clazz, Object object) throws Exception {
    }

    public List<WindowActivityElement> getWindowActivityElement() {
        return windowActivityElement;
    }

    public static class WindowActivity {

        final AppInfo appInfo;
        //        final List<UiHierarchy> uiHierarchy;
        final String uiHierarchyJson;


/*
        WindowActivity(AppInfo appInfo, List<UiHierarchy> uiHierarchy) {
            this.appInfo = appInfo;
            this.uiHierarchy = uiHierarchy;

        }
*/

        WindowActivity(AppInfo appInfo, String uiHierarchyJson) {
            this.appInfo = appInfo;
            this.uiHierarchyJson = uiHierarchyJson;

        }

        static WindowActivity build(JSONObject obj) throws Exception {
            return new WindowActivity(
                    AppInfo.build((JSONObject) obj.get("app_info")),
                    UiHierarchy.buildJson((JSONArray) obj.get("ui_hierarchy")));
        }

/*
        static WindowActivity build(JSONObject obj) throws Exception {
            return new WindowActivity(
                    AppInfo.build((JSONObject) obj.get("app_info")),
                    UiHierarchy.build((JSONArray) obj.get("ui_hierarchy")));
        }
*/


/*
        public static WindowActivity build(AppInfo appInfo, List<UiHierarchy> uiHierarchy) throws Exception {
            return new WindowActivity(appInfo, uiHierarchy);
        }
*/
    }

    public static class WindowActivityElement {

        final Integer time;
        final WindowActivity windowActivities;


        WindowActivityElement(Integer time, WindowActivity windowActivities) {
            this.time = time;
            this.windowActivities = windowActivities;

        }

        static List<WindowActivityElement> build(JSONArray obj) throws Exception {
            List<WindowActivityElement> windowActivityElements = new ArrayList<>();
            WindowActivityElement windowActivityElement;

            for (int i = 0; i < obj.size(); i++) {
                Integer time = (Integer) ((JSONObject) obj.get(i)).get("time");
                JSONObject window_activity = (JSONObject) ((JSONObject) obj.get(i)).get("window_activity");
                windowActivityElement =
                        new WindowActivityElement(time, WindowActivity.build(window_activity));
                windowActivityElements.add(windowActivityElement);
            }
            return windowActivityElements;
        }

    }

    public static class AppInfo {

        final String program_name;
        final String window_name;
        final String url;
        final String tab;


        AppInfo(String program, String window, String url, String tab) {
            this.program_name = program;
            this.window_name = window;
            this.url = url;
            this.tab = tab;

        }

        static AppInfo build(JSONObject obj) throws Exception {
            return new AppInfo(
                    getValueOf("program_name", String.class, obj),
                    getValueOf("window_name", String.class, obj),
                    getValueOf("url", String.class, obj),
                    getValueOf("tab", String.class, obj)
            );
        }

    }


    public static class UiHierarchy {

        final String name;
        final String ctrl;
        final String cls;


        UiHierarchy(String program, String window, String url) {
            this.name = program;
            this.ctrl = window;
            this.cls = url;

        }

        static List<UiHierarchy> build(JSONArray obj) throws Exception {
            List<UiHierarchy> uiHierarchies = new ArrayList<>();

            for (int i = 0; i < obj.size(); i++) {
                uiHierarchies.add(
                        new UiHierarchy(
                                getValueOf("name", String.class, (JSONObject) obj.get(i)),
                                getValueOf("ctrl", String.class, (JSONObject) obj.get(i)),
                                getValueOf("cls", String.class, (JSONObject) obj.get(i))
                        ));
            }
            return uiHierarchies;
        }

        static String buildJson(JSONArray obj) {
            return obj.toJSONString();
        }

        public static UiHierarchy build(String program, String window, String url) throws Exception {
            return new UiHierarchy(program, window, url);
        }

        public String getName() {
            return name;
        }

        public String getCtrl() {
            return ctrl;
        }

        public String getCls() {
            return cls;
        }
    }


}
