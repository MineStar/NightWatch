package de.minestar.nightwatch.logging.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLogEntry;

/**
 * Parse 1.7 minecraft logs. <br>
 * The format is: <code>HH:mm:ss [Source/LogLevel]: Message</code>
 * 
 * @author Meldanor
 *
 */
public class Version1710Parser extends LogEntryParser {

	private static final int TIME_POSITION = 1;
	private static final int SOURCE_LOGLEVEL_POSITION = 3;
	private static final int TEXT_POSITION = 4;

	private DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_TIME;

	public Version1710Parser() {
		super("\\[\\d{2}:\\d{2}:\\d{2}] \\[.+\\/.+\\]: .*");
	}

	// Splitpattern. Regex = [\[\]] - split at brackets
	private Pattern SPLIT_PATTERN = Pattern.compile("[\\[\\]]");

	@Override
	public ServerLogEntry parse(LocalDate date, String line) {

		String[] split = SPLIT_PATTERN.split(line, 5);
		String timeString = split[TIME_POSITION];

		// Source and loglevel are in one split
		int separator = split[SOURCE_LOGLEVEL_POSITION].lastIndexOf('/');
		String sourceString = split[SOURCE_LOGLEVEL_POSITION].substring(0,
				separator);
		String logLevelString = split[SOURCE_LOGLEVEL_POSITION]
				.substring(separator + 1);

		LocalDateTime timestamp = LocalTime.parse(timeString, TIME_FORMAT)
				.atDate(date);
		LogLevel logLevel = LogLevel.getByName(logLevelString);
		// Skipping ': ' of text string
		String textString = split[TEXT_POSITION].substring(2);

		return new ServerLogEntry(timestamp, sourceString, logLevel, textString);
	}
}
