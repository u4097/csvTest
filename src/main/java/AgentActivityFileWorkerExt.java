import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class AgentActivityFileWorkerExt {

    public static final String CORRUPTED_FILE_DOT_EXTENSION = ".corrupted";
    public static final int MAX_SORTED_DIR_COUNT = 10;
    private static final String FILE_EXTENSION = "zip";
    private static final String FILE_DOT_EXTENSION = "." + FILE_EXTENSION;
    private static final Comparator<Path> SORTING_COMPARATOR = Comparator.comparing(Path::getFileName);
    private static final DirectoryStream.Filter<Path> FILE_FILTER = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path entry) {
            return !Files.isDirectory(entry) && entry.toString().endsWith(FILE_DOT_EXTENSION);
        }
    };
    private final Path activityQueueDir;

    public AgentActivityFileWorkerExt(Path activityQueueDir) {
        this.activityQueueDir = activityQueueDir;
    }

    private static String parseActivityTime(Path filePath) {
        return filePath.getFileName().toString();
    }

    private static Instant parseTime(Path filePath) {
        String name = FilenameUtils.getBaseName(filePath.getFileName().toString());
        int beginPos = name.lastIndexOf('_');
        if (beginPos == -1) {
            return null;
        }

        try {
            return Instant.ofEpochMilli(Long.parseLong(name.substring(++beginPos)));
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    private static int getFirstDirectories(Path activityQueueDir, ArrayList<Path> paths, int maxCount, Path start) throws IOException {
        paths.clear();
        paths.ensureCapacity(maxCount);

        int totalCount = 0;
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(activityQueueDir)) {
            for (Path entry : dirStream) {
                int result = checkSegmentWithActivity(entry);
                if (result == -1) {
                    try {
                        Files.delete(entry);
                    } catch (IOException ignore) {
                    }
                    continue;
                } else if (result == 0 || (start != null && SORTING_COMPARATOR.compare(entry, start) < 1)) {
                    continue;
                }

                ++totalCount;
                int pos = Collections.binarySearch(paths, entry, SORTING_COMPARATOR);
                if (pos < 0) {
                    pos = -pos - 1;
                }

                if (pos >= maxCount) {
                    continue;
                }
                if (paths.size() == maxCount) {
                    paths.remove(maxCount - 1);
                }

                paths.add(pos, entry);
            }
        } catch (DirectoryIteratorException e) {
            throw e.getCause();
        }

        return totalCount - paths.size();
    }

    /**
     * @return -1 if segmentDir is empty; 0 if segmentDir not contains correct activity files; 1 if segmentDir contains correct activity files.
     */
    private static int checkSegmentWithActivity(Path segmentDir) throws IOException {
        try (DirectoryStream<Path> fileStream = Files.newDirectoryStream(segmentDir)) {
            Iterator<Path> i = fileStream.iterator();
            if (!i.hasNext()) {
                return -1;
            }

            do {
                if (FILE_FILTER.accept(i.next())) {
                    return 1;
                }
            } while (i.hasNext());
        } catch (NotDirectoryException | NoSuchFileException | AccessDeniedException ignore) {
            // do nothing
        } catch (DirectoryIteratorException e) {
            throw e.getCause();
        }

        return 0;
    }

    private static String buildDirName(Instant date, Duration segmentationPeriod) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(date, ZoneOffset.UTC);

        StringBuilder builder = new StringBuilder().
                append(dateTime.getYear()).append('.').
                append(String.format("%02d", dateTime.getMonthValue())).append('.').
                append(String.format("%02d", dateTime.getDayOfMonth()));

        int minutes = (int) segmentationPeriod.toMinutes();
        if (minutes < 60) {
            int multiplicity = lowerNearestDivisor(60, minutes);

            builder.append(' ').
                    append(String.format("%02d", dateTime.getHour())).append('_').
                    append(String.format("%02d", round(dateTime.getMinute(), multiplicity)));
        } else if (minutes < 24 * 60) {
            int multiplicity = lowerNearestDivisor(24, minutes / 60);

            builder.append(' ').append(String.format("%02d", round(dateTime.getHour(), multiplicity)));
        }

        return builder.toString();
    }

    private static int lowerNearestDivisor(int target, int maxValue) {
        for (; maxValue > 1; --maxValue) {
            if ((target % maxValue) == 0) {
                return maxValue;
            }
        }
        return 1;
    }

    private static int round(int target, int multiplicity) {
        return target - (target % multiplicity);
    }

    public boolean existsFiles() throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(activityQueueDir)) {
            for (Path entry : dirStream) {
                if (checkSegmentWithActivity(entry) == 1) {
                    return true;
                }
            }
        } catch (DirectoryIteratorException e) {
            throw e.getCause();
        }

        return false;
    }

    public int getFileCount() throws Exception {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(activityQueueDir)) {
            int count = 0;
            for (Path entry : dirStream) {
                try (DirectoryStream<Path> fileStream = Files.newDirectoryStream(entry, FILE_FILTER)) {
                    for (Path ignored : fileStream) {
                        ++count;
                    }
                } catch (DirectoryIteratorException | NotDirectoryException | NoSuchFileException | AccessDeniedException ignore) {
                }
            }
            return count;
        }
    }

    public void forEachFile(Action action) throws Exception {
        try {
            ArrayList<Path> paths = new ArrayList<>();
            boolean isMore;
            Path dir = null;
            do {
                isMore = getFirstDirectories(activityQueueDir, paths, MAX_SORTED_DIR_COUNT, dir) != 0;
                String activityStartTime = parseActivityTime(paths.get(0));
                String activityEndTime = parseActivityTime(paths.get(paths.size()-1));
                String activityTime = activityStartTime + "-" + activityEndTime;
                for (int i = 0; i < paths.size(); ++i) {
                    dir = paths.get(i);

                    try (DirectoryStream<Path> fileStream = Files.newDirectoryStream(dir, FILE_FILTER)) {
                        for (Path filePath : fileStream) {
                            ActionResult result = action.apply(filePath, parseTime(filePath), activityTime);
                            if (result.markFileAsCorrupted) {
                                try {
                                    Path newPath = filePath.resolveSibling(filePath.getFileName().toString() + CORRUPTED_FILE_DOT_EXTENSION);
                                    Files.move(filePath, newPath);
                                } catch (FileAlreadyExistsException ignore) {
                                    Path newPath = Files.createTempFile(filePath.getParent(), filePath.getFileName().toString() + ".", CORRUPTED_FILE_DOT_EXTENSION);
                                    Files.move(filePath, newPath, StandardCopyOption.REPLACE_EXISTING);
                                }
                            }

                            if (result.interrupt) {
                                return;
                            }
                        }
                    }
                }
            } while (isMore);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Path createTempFile() throws IOException {
        return Files.createTempFile(activityQueueDir, "temp_", ".tmp");
    }

    @FunctionalInterface
    public interface Action {

        ActionResult apply(Path zipFilePath, Instant agentRequestUtcTime, String dirTime);
    }

    public static class ActionResult {

        public static final ActionResult CONTINUE = new ActionResult(false, false);

        public final boolean interrupt;
        public final boolean markFileAsCorrupted;

        public ActionResult(boolean interrupt, boolean markFileAsCorrupted) {
            this.interrupt = interrupt;
            this.markFileAsCorrupted = markFileAsCorrupted;
        }
    }
}
