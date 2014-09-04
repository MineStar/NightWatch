package de.minestar.nightwatch.threading;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Task;
import de.minestar.nightwatch.logging.ServerLogEntry;
import de.minestar.nightwatch.logging.parser.LogEntryParser;
import de.minestar.nightwatch.logging.parser.SimpleLogEntryParser;

public class ServerLoggingTask extends Task<Void> {

    private BufferedReader reader;
    private List<ServerLogEntry> entryList;
    private LogEntryParser parser;

    private static final LogEntryParser SIMPLEPARSER = new SimpleLogEntryParser();

    public ServerLoggingTask(InputStream serverOutput, List<ServerLogEntry> entries, LogEntryParser parser) {
        this.reader = new BufferedReader(new InputStreamReader(serverOutput));
        this.entryList = entries;
        this.parser = parser;
    }

    @Override
    protected Void call() throws Exception {
        while (!isCancelled()) {
            String readLine = this.reader.readLine();
            if (readLine == null)
                break;
            Platform.runLater(() -> {
                ServerLogEntry entry = null;
                try {
                    entry = parser.parse(LocalDate.now(), readLine);
                } catch (ParseException e) {
                    try {
                        entry = SIMPLEPARSER.parse(LocalDate.now(), readLine);
                    } catch (ParseException e1) {
                        // Simple Parser never throws exception
                    }
                }
                entryList.add(entry);
            });

        }
        return null;
    }

    @Override
    protected void cancelled() {
        try {
            this.reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
