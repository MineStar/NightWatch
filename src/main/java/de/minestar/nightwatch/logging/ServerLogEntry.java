package de.minestar.nightwatch.logging;

import java.time.LocalDateTime;

public class ServerLogEntry {

	private final LocalDateTime time;
	private final String text;
	private final String source;
	private final LogLevel logLevel;

	public ServerLogEntry(LocalDateTime time, String source, LogLevel level,
			String text) {
		this.time = time;
		this.source = source;
		this.logLevel = level;
		this.text = text;
	}

	public ServerLogEntry(LocalDateTime time, String source, String logLevel,
			String text) {
		this.time = time;
		this.source = source;
		this.logLevel = LogLevel.getByName(logLevel);
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public String getSource() {
		return source;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	@Override
	public String toString() {
		return "ServerLogEntry [time=" + time + ", text=" + text + ", source="
				+ source + ", logLevel=" + logLevel + "]";
	}

}
