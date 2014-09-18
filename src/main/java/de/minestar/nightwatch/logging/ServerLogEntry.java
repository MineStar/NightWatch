package de.minestar.nightwatch.logging;

import java.time.LocalDateTime;

/**
 * Represents an instant of a server log containing a message, a time stamp, a source if possible and a log level.
 * 
 */
public class ServerLogEntry {

    private final LocalDateTime time;
    private final String text;
    private final String source;
    private final LogLevel logLevel;

    public ServerLogEntry(LocalDateTime time, String source, LogLevel level, String text) {
        this.time = time;
        this.source = source;
        this.logLevel = level;
        this.text = text;
    }

    /**
     * @return The message of the log
     */
    public String getText() {
        return text;
    }

    /**
     * @return The time stamp of this log entry
     */
    public LocalDateTime getTime() {
        return time;
    }

    /**
     * @return The source of the message, for example the server thread or the mod / plugin
     */
    public String getSource() {
        return source;
    }

    /**
     * @return The severity of the message
     */
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public String toString() {
        return "ServerLogEntry [time=" + time + ", text=" + text + ", source=" + source + ", logLevel=" + logLevel + "]";
    }

}
