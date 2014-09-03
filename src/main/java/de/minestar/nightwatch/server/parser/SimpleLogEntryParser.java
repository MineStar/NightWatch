package de.minestar.nightwatch.server.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;

import de.minestar.nightwatch.core.ServerLogEntry;
import de.minestar.nightwatch.server.LogLevel;

public class SimpleLogEntryParser implements LogEntryParser {

    @Override
    public ServerLogEntry parse(LocalDate date, String line) {
        return new ServerLogEntry(LocalDateTime.now(), "Server", LogLevel.ALL, line);
    }

}
