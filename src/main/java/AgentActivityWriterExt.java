import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.UrlUtils;
import utils.csv.CsvParser;
import utils.csv.CsvRecord;
import utils.csv.CsvSettings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AgentActivityWriterExt {

    public static final String DEFAULT_AGENT_ACTIVITY_QUEUE_DIR = "/Users/oleg/Project/crocotime/cvsTest/data/agent_activity_queue";
    public final static ArrayList<String> SUPPORTED_DATA_VERSIONS = Stream.of("1.0.0").collect(Collectors.toCollection(ArrayList::new));
    private static final String DEFAULT_DATA_DIR = "/Users/oleg/Project/crocotime/cvsTest/data";
    private final static Logger log = LoggerFactory.getLogger(AgentActivityWriterExt.class);
    private Instant agentRequestUtcTime;
    private Path activityZipFile;
    private long computerAccountId;
    private long windowActivityDataId;

    public AgentActivityWriterExt() {
    }

    private static void convertRecord(CsvRecord source, WinRecord destination, Supplier<Path> filePathSupplier) throws Exception {
        checkCellCountIntoRow(AgentDataManifest.WindowSwitchingCsv.COLUMN_COUNT, source, filePathSupplier);

        destination.time = convertCellToLong(AgentDataManifest.WindowSwitchingCsv.COLUMN_TIME, source, filePathSupplier);
        if (destination.time < 0) {
//            throw MonitoringExceptionBuilder.buildAgentDataException("Time less than 0", source);
        }

        destination.program = StringUtils.stripToNull(source.get(AgentDataManifest.WindowSwitchingCsv.COLUMN_PROGRAM));
        destination.window = StringUtils.stripToNull(source.get(AgentDataManifest.WindowSwitchingCsv.COLUMN_WINDOW));
        destination.url = UrlUtils.trimBrowserUrl(source.get(AgentDataManifest.WindowSwitchingCsv.COLUMN_URL));
        destination.uiHierarchy = StringUtils.stripToNull(source.get(AgentDataManifest.WindowSwitchingCsv.COLUMN_UI_HIERARCHY));

        if (destination.program == null && (destination.window != null || destination.url != null || destination.uiHierarchy != null)) {
//            throw MonitoringExceptionBuilder.Csv.emptyValue(AgentDataManifest.WindowSwitchingCsv.COLUMN_PROGRAM, filePathSupplier.get(), source);
        }
    }

    private static long convertCellToLong(int columnNumber, CsvRecord row, Supplier<Path> filePathSupplier) throws Exception {
        try {
            return Long.parseLong(row.get(columnNumber));
        } catch (NumberFormatException e) {
//            throw MonitoringExceptionBuilder.Csv.invalidTypeValue(columnNumber, e, filePathSupplier.get(), row);
            throw new Exception();
        }
    }

    private static void checkCellCountIntoRow(int expectedCount, CsvRecord row, Supplier<Path> filePathSupplier) throws Exception {
        if (row.size() != expectedCount) {
//            throw MonitoringExceptionBuilder.Csv.notEnoughCellCount(filePathSupplier.get(), row);
        }
    }

    private static CsvParser buildCsvParser(ZipFile zipFile, String csvFileName) throws IOException {
        Reader reader = new BufferedReader(new InputStreamReader(buildZipStream(zipFile, csvFileName), StandardCharsets.UTF_8));

        try {
            return new CsvParser(reader, CsvSettings.DEFAULT);
        } catch (Throwable e) {
            try {
                reader.close();
            } catch (IOException ignore) {
            }
            throw e;
        }
    }

    private static InputStream buildZipStream(ZipFile zipFile, String fileName) throws IOException {
        ZipArchiveEntry entry = zipFile.getEntry(fileName);
        if (entry == null) {
            throw new FileNotFoundException(fileName);
        }
        return zipFile.getInputStream(entry);
    }

    /**
     * Создадим csv файл для записи активности
     *
     * @param data данные для записи
     * @throws IOException
     */
    private static void createCvs(Set<String> data) throws IOException {
        FileWriter out = new FileWriter(DEFAULT_DATA_DIR + "/" + "activity.csv");

        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                .withHeader("computer_account_id", "time", "program", "window", "url", "tab", "ui_hierarchy")
//                .withSkipHeaderRecord()
                .withDelimiter(';')
                .withRecordSeparator("\r\n")
                .withEscape('\\')
                .withQuoteMode(QuoteMode.NONE)
        )) {
            data.forEach((value) -> {
                try {
                    printer.printRecord(value);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    /**
     * Получаем данные текущего сотрудника из манифеста (computer_account_id)
     *
     * @param activityZipFile     zip файл с активностью
     * @param agentRequestUtcTime время активности в секундах
     * @throws Exception
     */
    public void write(Path activityZipFile, Instant agentRequestUtcTime) throws Exception {
        //Создадим cvs файл
        Set<String> data = new LinkedHashSet<>();

        this.activityZipFile = activityZipFile;
        this.agentRequestUtcTime = agentRequestUtcTime;

        try (ZipFile zipFile = new ZipFile(activityZipFile.toFile())) {

            this.computerAccountId = writeUserData(zipFile);
            this.windowActivityDataId = writeWindowData(zipFile);


            // Получаем computer_account_id из manifest.json
//                    computerAccount = writeUserData(zipFile);
//                    String computer_account_id = "5";

            //Получаем данные расширенного мониторинга из window_switching.json
            // Время:
            String time = "1234567";
            // Название программы:
            String program = "program";
            // Название окна:
            String window = "Window Title";
            // Url адрес (для Web приложений)
            String url = "http://google.com";
            // Название вкладки:
            String tab = "Tab Title";
            // строка в JSON формате, содержащая иерархию UI
            String ui_hierarchy = "{ui hierarchy}";

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append(String.valueOf(computerAccountId))
                    .append(';')
                    .append(time)
                    .append(';')
                    .append(program)
                    .append(';')
                    .append(window)
                    .append(';')
                    .append(url)
                    .append(';')
                    .append(ui_hierarchy);

            data.add(stringBuilder.toString());

            createCvs(data);

            try {
                zipFile.close();
            } catch (IOException e) {
                log.error("ZipFile.close of activity failed, " + activityZipFile, e);
            }
        } catch (IOException e) {
            log.debug(e.getLocalizedMessage());
//            throw MonitoringExceptionBuilder.buildAgentDataException(e);
        }

    }

    private Long writeUserData(ZipFile zipFile) throws Exception {
        AgentDataManifest manifest = null;
        try (InputStream stream = buildZipStream(zipFile, AgentDataManifest.FILE_NAME)) {
            manifest = AgentDataManifest.build(new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(stream));
        } catch (ParseException | IOException e) {
//            throw MonitoringExceptionBuilder.buildAgentDataException(e);
        }

        if (manifest.user.employeeId != null) {
//            log.warn("EmployeeId = {} not exists, activity by {} ignored.", manifest.user.employeeId, manifest.user.login);
//            return null;
        }

//        final ComputerReadable computer = ensureComputer(manifest.computer, manifest.agent);
//        return ensureComputerAccount(manifest.user, computer);
        return 1L;
    }

    private Long writeWindowData(ZipFile zipFile) throws Exception {
        WindowActivityData windowActivityData = null;
        try (InputStream stream = buildZipStream(zipFile, WindowActivityData.FILE_NAME)) {
            windowActivityData = WindowActivityData.build(new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(stream));
        } catch (ParseException | IOException e) {
            throw new Exception(e);
        }

        return 1L;
    }

    private static class WinRecord {

        int computerAccountId;
        long time;
        String program;
        String window;
        String url;
        String tab;
        String uiHierarchy;

        boolean isEnd() {
            return program == null && window == null && url == null && uiHierarchy == null;
        }
    }

}
