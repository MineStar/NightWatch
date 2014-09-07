package de.minestar.nightwatch.logging.parser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLogEntry;

/**
 * Parse 1.6 forge logs. <br>
 * The format is:
 * <code>yyyy-MM-dd HH:mm:ss [LogLevel] {[Source]}: Message</code>
 * 
 * @author Meldanor
 *
 */
public class Forge16Parser extends LogEntryParser {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_TIME;

	public Forge16Parser() {
		super("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\[.*\\] .*");
	}

	@Override
	public ServerLogEntry parse(LocalDate day, String line) {
		String[] split = line.split(" ", 4);

		String dateString = split[0];
		String timeString = split[1];
		String logLevelString = split[2].substring(1, split[2].length() - 1);
		String rest = split[3];

		String source;

		if (rest.startsWith("[") && rest.indexOf(']') + 2 <= rest.length()) {
			source = rest.substring(1, rest.indexOf(']'));
			rest = rest.substring(rest.indexOf(']') + 2);
		} else
			source = "Unknown";

		day = LocalDate.parse(dateString, DATE_FORMAT);
		LocalTime time = LocalTime.parse(timeString, TIME_FORMAT);

		return new ServerLogEntry(day.atTime(time), source,
				LogLevel.getByName(logLevelString), rest);
	}

}
