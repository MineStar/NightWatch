package de.minestar.nightwatch.server.parser;

import java.text.ParseException;
import java.time.LocalDate;

import de.minestar.nightwatch.core.ServerLogEntry;

public interface LogEntryParser {

    public ServerLogEntry parse(LocalDate date, String line) throws ParseException;
}
