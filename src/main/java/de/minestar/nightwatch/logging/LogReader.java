package de.minestar.nightwatch.logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import de.minestar.nightwatch.logging.parser.IntelligentParser;

public class LogReader {

	private IntelligentParser parser;

	public LogReader() {
		this.parser = new IntelligentParser();
	}

	public List<ServerLogEntry> readLogFile(File logFile) throws IOException {
		List<String> allLines = Files.readAllLines(logFile.toPath());

		List<ServerLogEntry> logEntries = allLines.stream()
				.filter(p -> !p.trim().isEmpty()).map((String line) -> {
					return parser.parse(LocalDate.now(), line);

				}).collect(Collectors.toList());

		return logEntries;
	}
}
