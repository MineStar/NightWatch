package de.minestar.nightwatch.logging.parser;

import java.text.ParseException;
import java.time.LocalDate;

import de.minestar.nightwatch.logging.ServerLogEntry;

public interface LogEntryParser {

    public ServerLogEntry parse(LocalDate date, String line) throws ParseException;
}
