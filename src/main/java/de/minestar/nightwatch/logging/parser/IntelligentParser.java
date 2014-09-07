package de.minestar.nightwatch.logging.parser;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import de.minestar.nightwatch.logging.ServerLogEntry;

public class IntelligentParser {

	private List<LogEntryParser> parser;
	private LogEntryParser defaultParser;

	// TODO: Reorder the list so, that the first parser is the parser with the
	// highest chance to accept

	public IntelligentParser() {
		this.parser = Arrays.asList(new Version1710Parser(),
				new Forge16Parser(), new Cauldron17ConsoleOutputParser(),
				new Cauldron16ConsoleOutputParser());
		this.defaultParser = new DefaultEntryParser();
	}

	public ServerLogEntry parse(LocalDate day, String line) {

		if (line.trim().isEmpty())
			return defaultParser.parse(day, line);

		for (LogEntryParser newLogEntryParser : parser) {
			if (newLogEntryParser.accepts(line)) {
				return newLogEntryParser.parse(day, line);
			}
		}

		return defaultParser.parse(day, line);
	}

}
