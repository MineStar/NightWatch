package de.minestar.nightwatch.threading;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Task;
import de.minestar.nightwatch.core.ServerLogEntry;
import de.minestar.nightwatch.server.parser.LogEntryParser;

public class ServerLoggingTask extends Task<Void> {

    private BufferedReader reader;
    private List<ServerLogEntry> entryList;
    private LogEntryParser parser;

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
            Platform.runLater(() -> entryList.add(parser.parse(readLine)));

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
