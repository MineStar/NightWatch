package de.minestar.nightwatch.threading;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import de.minestar.nightwatch.core.ServerLogEntry;
import de.minestar.nightwatch.server.ObservedServer;
import de.minestar.nightwatch.server.parser.Version1710Parser;

public class ServerOverwatchThread extends Task<Void> {

    private ObservedServer server;
    private Process serverProcess;
    private ServerCommandTask commandTask;
    private ServerLoggingTask logTask;

    private BooleanProperty isAlive;
    private List<ServerLogEntry> logList;
    private LinkedBlockingQueue<String> commandQueue;

    public ServerOverwatchThread(ObservedServer server, List<ServerLogEntry> logList, LinkedBlockingQueue<String> commandQueue) {
        this.server = server;
        this.logList = logList;
        this.commandQueue = commandQueue;
        this.isAlive = new SimpleBooleanProperty(false);
    }

    @Override
    protected Void call() throws Exception {
        start();
        while (!isCancelled()) {
            overwatch();
            Thread.sleep(1000);
        }
        return null;
    }

    private void start() throws Exception {
        this.serverProcess = server.createProcess().start();
        // TODO: User have to choose, which parser is correct
        this.logTask = new ServerLoggingTask(serverProcess.getInputStream(), logList, new Version1710Parser());
        Thread loggingTaskThread = new Thread(logTask, this.server.getName() + "_LoggingTask");

        this.commandTask = new ServerCommandTask(serverProcess.getOutputStream(), commandQueue);
        Thread commandTaskThread = new Thread(commandTask, this.server.getName() + "_CommandTask");

        loggingTaskThread.start();
        commandTaskThread.start();

        this.isAlive.set(true);
    }

    private void overwatch() {
        if (serverProcess.isAlive()) {
            return;
        } else {
            this.logTask.cancel();
            this.commandTask.cancel();

            isAlive.set(false);
            this.cancel();
        }
    }

    public BooleanProperty isAlive() {
        return isAlive;
    }

}
