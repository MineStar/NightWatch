package de.minestar.nightwatch.logging.parser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLogEntry;

/**
 * Parse cauldron for 1.6 logs printing on the console<br>
 * The format is: <code>23:48:24 [LogLevel] {[Source]} Message</code>
 * 
 * @author Meldanor
 *
 */
public class Cauldron16ConsoleOutputParser extends LogEntryParser {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_TIME;

    public Cauldron16ConsoleOutputParser() {
        super("\\d{2}:\\d{2}:\\d{2} \\[.+\\] .*");
    }

    @Override
    public ServerLogEntry parse(LocalDate day, String line) {
        String[] split = line.split(" ", 3);

        String timeString = split[0];
        String logLevelString = split[1].substring(1, split[1].length() - 1);
        String rest = split[2];

        String source;

        if (rest.startsWith("[") && rest.indexOf(']') + 2 <= rest.length()) {
            source = rest.substring(1, rest.indexOf(']'));
            rest = rest.substring(rest.indexOf(']') + 2);
        } else
            source = "Unknown";

        LocalTime time = LocalTime.parse(timeString, TIME_FORMAT);

        return new ServerLogEntry(day.atTime(time), source, LogLevel.getByName(logLevelString), rest);
    }

}
