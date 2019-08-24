import net.minidev.json.JSONObject;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;

class UserData {
    public final User user;
    private final Computer computer;

    UserData(User user, Computer computer) {
        this.user = user;
        this.computer = computer;
    }

    static <T> T getRequiredValueOf(String name, Class<T> clazz, JSONObject obj) throws Exception {
        T value = getValueOf(name, clazz, obj);
        checkRequired(name, value);
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
        if (object != null && !clazz.isAssignableFrom(object.getClass())) {
//            throw MonitoringExceptionBuilder.buildAgentDataException("Type of \"" + name + "\" must be " + clazz.getSimpleName());
        }
    }

    public static class User {

        final String name;
        final String login;
        final String domain;
        final ZoneId timeZone;
        final Long employeeId;

        User(String name, String login, String domain, String timeZoneId, Integer timezoneSec, Long employeeId) {
            this.name = name;
            this.login = login;
            this.domain = (domain == null || domain.isEmpty()) ? null : domain;

            ZoneId timeZone;
            try {
                timeZone = ZoneId.of(timeZoneId);
            } catch (DateTimeException e) {
                if (timezoneSec != null) {
                    ZoneOffset offset = null;
                    boolean err = false;
                    try {
                        offset = ZoneOffset.ofTotalSeconds(timezoneSec);
                    } catch (DateTimeException e1) {
                        err = true;
                    }
                    timeZone = err ? ZoneId.systemDefault() : ZoneId.ofOffset("UTC", offset);
                } else {
                    timeZone = ZoneId.systemDefault();
                }
            }
            this.timeZone = timeZone;

            this.employeeId = employeeId;
        }

        static User build(JSONObject obj) throws Exception {
            return new User(
                    getRequiredValueOf("name", String.class, obj),
                    getRequiredValueOf("login", String.class, obj),
                    getValueOf("domain", String.class, obj),
                    getRequiredValueOf("timezone", String.class, obj),
                    castToInteger(getValueOf("timezone_sec", Number.class, obj)),
                    castToLong(getValueOf("employee_id", Number.class, obj))
            );
        }


        public static User build(String name, String login, String domain, String timeZoneId) throws Exception {
            return new User(name, login, domain, timeZoneId, null, null);
        }


        private static Long castToLong(Number value) throws Exception {
            if (value == null) {
                return null;
            }

            if (value instanceof Integer) {
                return value.longValue();
            }

            if (value instanceof Long) {
                return (Long) value;
            }

//            throw MonitoringExceptionBuilder.buildAgentDataException("Type of employee_id must be Long.");
            throw new Exception();
        }

        private static Integer castToInteger(Number value) throws Exception {
            if (value == null) {
                return null;
            }

            if (value instanceof Integer) {
                return value.intValue();
            }

//            throw MonitoringExceptionBuilder.buildAgentDataException("Type of timezone_sec must be Integer.");
            throw new Exception();
        }
    }

    public static class Computer {

        public final String name;
        public final String domain;
        public final String workgroup;

        public Computer(String name, String domain, String workgroup) {
            this.name = name;
            this.domain = (domain == null || domain.isEmpty()) ? null : domain;
            this.workgroup = (workgroup == null || workgroup.isEmpty()) ? null : workgroup;
        }

        public static Computer build(JSONObject obj) throws Exception {
            return new Computer(
                    getRequiredValueOf("name", String.class, obj),
                    getValueOf("domain", String.class, obj),
                    getValueOf("workgroup", String.class, obj)
            );
        }

        public static Computer build(String name, String domain, String workgroup) throws Exception {
            return new Computer(name, domain, workgroup);
        }
    }
}
