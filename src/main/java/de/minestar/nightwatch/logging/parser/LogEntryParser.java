package de.minestar.nightwatch.logging.parser;

import java.time.LocalDate;
import java.util.regex.Pattern;

import de.minestar.nightwatch.logging.ServerLogEntry;

public abstract class LogEntryParser {

	private Pattern acceptedFormat;

	protected LogEntryParser(String acceptedFormatRegex) {
		this.acceptedFormat = Pattern.compile(acceptedFormatRegex);
	}

	public boolean accepts(String line) {
		return acceptedFormat.matcher(line).matches();
	}

	public abstract ServerLogEntry parse(LocalDate day, String line);

}
