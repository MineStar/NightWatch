package de.minestar.nightwatch.logging.parser;

import java.time.LocalDate;
import java.time.LocalTime;

import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLogEntry;

/**
 * Parse nothing, interprets everything as the text
 * 
 * @author Meldanor
 *
 */
public class DefaultEntryParser extends LogEntryParser {

    public DefaultEntryParser() {
        super("[\\s\\S]*");
    }

    @Override
    public ServerLogEntry parse(LocalDate day, String line) {
        return new ServerLogEntry(day.atTime(LocalTime.now()), "Unknown", LogLevel.ALL, line);
    }

}
