package de.minestar.nightwatch.threading;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Task;
import de.minestar.nightwatch.logging.ServerLogEntry;
import de.minestar.nightwatch.logging.parser.IntelligentParser;

public class ServerLoggingTask extends Task<Void> {

    private BufferedReader reader;
    private List<ServerLogEntry> entryList;
//    private LogEntryParser parser;
//
//    private static final LogEntryParser VERSION17Parser = new SimpleLogEntryParser();
//    private static final LogEntryParser FORGE16Parser = new Forge16Parser();
//    private static final LogEntryParser SIMPLEPARSER = new SimpleLogEntryParser();

    private IntelligentParser parser;

    public ServerLoggingTask(InputStream serverOutput, List<ServerLogEntry> entries) {
        this.reader = new BufferedReader(new InputStreamReader(serverOutput));
        this.entryList = entries;
        this.parser = new IntelligentParser();
    }

    @Override
    protected Void call() throws Exception {
        while (!isCancelled()) {
            String readLine = this.reader.readLine();
            if (readLine == null)
                break;
            Platform.runLater(() -> {
                entryList.add(parser.parse(LocalDate.now(), readLine));
//                ServerLogEntry entry = null;
//                try {
//                    entry = VERSION17Parser.parse(LocalDate.now(), readLine);
//                } catch (ParseException e) {
//                    try {
//                        entry = FORGE16Parser.parse(LocalDate.now(), readLine);
//                    } catch (ParseException e1) {
//                        try {
//
//                            entry = SIMPLEPARSER.parse(LocalDate.now(), readLine);
//                        } catch (Exception e2) {
//                            // Simple Parser never throws exception
//                        }
//
//                    }
//                }
//                entryList.add(entry);
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
