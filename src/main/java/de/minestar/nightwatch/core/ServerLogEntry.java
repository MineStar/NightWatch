package de.minestar.nightwatch.core;

import java.time.LocalDateTime;

import de.minestar.nightwatch.server.LogLevel;

public class ServerLogEntry {

    private final LocalDateTime time;
    private final String text;
    private final LogLevel logLevel;

    public ServerLogEntry(LocalDateTime time, LogLevel level, String text) {
        this.time = time;
        this.logLevel = level;
        this.text = text;
    }

    public ServerLogEntry(LocalDateTime time, String logLevel, String text) {
        this.time = time;
        this.logLevel = LogLevel.getByName(logLevel);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public String toString() {
        return "Log [time=" + time + ", text=" + text + ", logLevel=" + logLevel + "]";
    }

}
