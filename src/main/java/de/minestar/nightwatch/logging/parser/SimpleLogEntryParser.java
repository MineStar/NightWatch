package de.minestar.nightwatch.logging.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;

import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLogEntry;

public class SimpleLogEntryParser implements LogEntryParser {

    @Override
    public ServerLogEntry parse(LocalDate date, String line) {
        return new ServerLogEntry(LocalDateTime.now(), "Server", LogLevel.ALL, line);
    }

}
