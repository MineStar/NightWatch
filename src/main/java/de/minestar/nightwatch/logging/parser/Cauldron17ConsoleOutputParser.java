package de.minestar.nightwatch.logging.parser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLogEntry;

/**
 * Parse cauldron logs printing on the console<br>
 * The format is: <code>[23:48:24] [LogLevel] {[Source]}: Message</code>
 * 
 * @author Meldanor
 *
 */
public class Cauldron17ConsoleOutputParser extends LogEntryParser {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_TIME;

    public Cauldron17ConsoleOutputParser() {
        super("\\[\\d{2}:\\d{2}:\\d{2} .+\\]: .*");
    }

    @Override
    public ServerLogEntry parse(LocalDate day, String line) {
        String[] split = line.split(" ", 3);
        String timeString = split[0].substring(1);
        String logLevelString = split[1].substring(0, split[1].length() - 2);
        String sourceString = "Unknown";
        String textString = split[2];

        return new ServerLogEntry(LocalTime.parse(timeString, TIME_FORMAT).atDate(day), sourceString, LogLevel.getByName(logLevelString), textString);
    }

}
