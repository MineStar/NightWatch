package de.minestar.nightwatch.threading;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;

import javafx.concurrent.Task;
import de.minestar.nightwatch.logging.ServerLog;
import de.minestar.nightwatch.logging.parser.IntelligentParser;

public class ServerLoggingTask extends Task<Void> {

    private BufferedReader reader;
    private ServerLog log;

    private IntelligentParser parser;

    public ServerLoggingTask(InputStream serverOutput, ServerLog log) {
        this.reader = new BufferedReader(new InputStreamReader(serverOutput));
        this.log = log;
        this.parser = new IntelligentParser();
    }

    @Override
    protected Void call() throws Exception {
        while (!isCancelled()) {
            String readLine = this.reader.readLine();
            if (readLine == null)
                break;
            log.add(parser.parse(LocalDate.now(), readLine));
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
