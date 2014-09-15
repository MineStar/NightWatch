package de.minestar.nightwatch.threading;

import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import de.minestar.nightwatch.core.Core;
import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLog;
import de.minestar.nightwatch.logging.ServerLogEntry;
import de.minestar.nightwatch.server.ObservedServer;

public class ServerOverwatchThread extends Task<Void> {

    private ObservedServer server;
    private Process serverProcess;
    private ServerCommandTask commandTask;
    private ServerLoggingTask logTask;

    private BooleanProperty isAlive;
    private ServerLog serverLog;
    private LinkedBlockingQueue<String> commandQueue;

    public ServerOverwatchThread(ObservedServer server, ServerLog serverLog, LinkedBlockingQueue<String> commandQueue) {
        this.server = server;
        this.serverLog = serverLog;
        this.commandQueue = commandQueue;
        this.isAlive = new SimpleBooleanProperty(false);
    }

    @Override
    protected Void call() throws Exception {
        start();
        while (!isCancelled()) {
            overwatch();
            Thread.sleep(100);
        }
        return null;
    }

    private void start() throws Exception {
        ProcessBuilder processBuilder = server.createProcess();
        this.serverLog.add(new ServerLogEntry(LocalDateTime.now(), "Nightwatch GUI", LogLevel.INFO, "Starting server. Parameters are " + processBuilder.command()));

        this.serverProcess = processBuilder.start();

        this.logTask = new ServerLoggingTask(serverProcess.getInputStream(), serverLog);
        Thread loggingTaskThread = new Thread(logTask, this.server.getName() + "_LoggingTask");

        this.commandTask = new ServerCommandTask(serverProcess.getOutputStream(), commandQueue);
        Thread commandTaskThread = new Thread(commandTask, this.server.getName() + "_CommandTask");

        loggingTaskThread.start();
        commandTaskThread.start();

        Core.runningServers = Core.runningServers + 1;
        this.isAlive.set(true);
    }

    private void overwatch() {
        if (serverProcess.isAlive()) {
            return;
        } else {
            this.logTask.cancel();
            this.commandTask.cancel();

            Platform.runLater(() -> isAlive.set(false));
            Core.runningServers = Core.runningServers - 1;
            this.cancel();
        }
    }

    public BooleanProperty isAlive() {
        return isAlive;
    }

    public void kill() {
        this.serverProcess.destroy();
    }

}
