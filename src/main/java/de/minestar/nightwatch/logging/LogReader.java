package de.minestar.nightwatch.logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleObjectProperty;
import de.minestar.nightwatch.logging.parser.LogEntryParser;
import de.minestar.nightwatch.logging.parser.Version1710Parser;

public class LogReader {

    public LogReader() {

    }

    public List<ServerLogEntry> readLogFile(File logFile) throws IOException {
        List<String> allLines = Files.readAllLines(logFile.toPath());
        LogEntryParser parser = new Version1710Parser();

        // Dirty hack to avoid java error variable must be final
        final SimpleObjectProperty<ServerLogEntry> lastEntry = new SimpleObjectProperty<>();

        List<ServerLogEntry> logEntries = allLines.stream().filter(p -> !p.trim().isEmpty()).map((String line) -> {

            try {
                ServerLogEntry entry = parser.parse(LocalDate.now(), line);
                lastEntry.set(entry);
                return entry;
            } catch (ParseException e) {
                if (isExceptionWithInvalidFormat(line)) {
                    return new ServerLogEntry(lastEntry.get().getTime(), lastEntry.get().getSource(), lastEntry.get().getLogLevel(), line);
                } else {
                    return new ServerLogEntry(LocalDateTime.now(), "UNKNOWN", LogLevel.SEVERE, line);
                }
            }

        }).collect(Collectors.toList());

        return logEntries;
    }

    private boolean isExceptionWithInvalidFormat(String line) {
        return line.contains("Exception") || line.trim().startsWith("at ");
    }
}
