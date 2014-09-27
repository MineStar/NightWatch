package de.minestar.nightwatch.threading;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import de.minestar.nightwatch.core.Core;
import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLog;
import de.minestar.nightwatch.logging.ServerLogEntry;
import de.minestar.nightwatch.server.ObservedMinecraftServer;

/**
 * Master thread to observe the minecraft server and its status. It will have some sub threads to communicate with the server, the
 * {@link ServerCommandTask} and {@link ServerLoggingTask}. This thread itself will only kick off the other threads and overwatch the process, but do
 * not handle the communication.
 */
public class ServerOverwatchThread extends Task<Void> {

    private static final long CHECK_INTERVALL_MS = 100L;

    private ObservedMinecraftServer server;
    private Process serverProcess;
    private ServerCommandTask commandTask;
    private ServerLoggingTask logTask;
    private Optional<AutoRestartTask> restartTask;

    private BooleanProperty isAlive;
    private ServerLog serverLog;
    private LinkedBlockingQueue<String> commandQueue;

    /**
     * Construct a thread to observe the server. The thread is running as long the server process is alive. It will cancel itself all subthreads, if
     * the process is terminated.
     * 
     * @param server
     *            The server to observe.
     * @param serverLog
     *            The log to write the System.out and System.err of the observed server.
     * @param commandQueue
     *            The queue to submit commands to the observed server.
     */
    public ServerOverwatchThread(ObservedMinecraftServer server, ServerLog serverLog, LinkedBlockingQueue<String> commandQueue) {
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
            Thread.sleep(CHECK_INTERVALL_MS);
        }
        return null;
    }

    private void start() throws Exception {
        // Prepare the server process to start
        ProcessBuilder processBuilder = server.createProcess();
        // Submit message to log, that the server is starting
        this.serverLog.add(new ServerLogEntry(LocalDateTime.now(), "Nightwatch GUI", LogLevel.INFO, "Starting server. Parameters are " + processBuilder.command()));

        this.serverProcess = processBuilder.start();

        // Create helper tasks

        // Task for reading the output of the server
        this.logTask = new ServerLoggingTask(serverProcess.getInputStream(), serverLog);
        logTask.exceptionProperty().addListener((observ, oldVal, newVal) -> {
            Core.logger.error("Reading server output from {} failed", server.getName());
            Core.logger.catching(newVal);
        });

        Thread loggingTaskThread = new Thread(logTask, this.server.getName() + "_LoggingTask");

        // Task for writing into the input of the server and add ability to submit commands
        this.commandTask = new ServerCommandTask(serverProcess.getOutputStream(), commandQueue);
        commandTask.exceptionProperty().addListener((observ, oldVal, newVal) -> {
            Core.logger.error("Write commands to server {} failed", server.getName());
            Core.logger.catching(newVal);
        });
        Thread commandTaskThread = new Thread(commandTask, this.server.getName() + "_CommandTask");

        // If the server is doing automatic restarts on a certain, given interval, it will also creates a third thread to sleep until restart and run
        // the restart command
        if (server.doAutoRestarts()) {
            startAutoRestartTask();
        } else {
            this.restartTask = Optional.empty();
        }

        loggingTaskThread.start();
        commandTaskThread.start();

        // As long servers are running, the GUI cannot be closed by the server.
        Core.serverManager.getRunningServers().increment();
        Platform.runLater(() -> this.isAlive.set(true));
    }

    private void startAutoRestartTask() {
        List<LocalTime> restartTimes = server.getRestartTimes();
        // Do auto restarts without having restart times? Don't play with the sheriff!
        if (restartTimes.isEmpty())
            return;

        LocalTime currenTime = LocalTime.now();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime closestRestartTime = LocalDateTime.MAX;
        Duration closestDuration = Duration.between(now, closestRestartTime);
        // Find the closest date time to current time
        for (LocalTime localTime : restartTimes) {
            LocalDateTime restartTime;
            if (localTime.isBefore(currenTime))
                restartTime = localTime.atDate(tomorrow);
            else
                restartTime = localTime.atDate(today);
            Duration currentDuration = Duration.between(now, restartTime);
            if (currentDuration.compareTo(closestDuration) < 0) {
                closestRestartTime = restartTime;
                closestDuration = currentDuration;
            }
        }

        AutoRestartTask autoRestartTask = new AutoRestartTask(closestRestartTime, server.getWarningIntervals(), this.commandQueue);
        // Add exception handler
        autoRestartTask.exceptionProperty().addListener((observ, oldVal, newVal) -> {
            Core.logger.error("Autorestart for {} failed", server.getName());
            Core.logger.catching(newVal);
        });
        this.restartTask = Optional.of(autoRestartTask);
        Thread autoRestartThread = new Thread(autoRestartTask, server.getName() + "_AutoRestartThread");
        autoRestartThread.start();
    }

    private void overwatch() {
        if (serverProcess.isAlive()) {
            return;
        } else {
            this.logTask.cancel();
            this.commandTask.cancel();
            this.restartTask.ifPresent(e -> e.cancel(true));

            Platform.runLater(() -> isAlive.set(false));
            Core.serverManager.getRunningServers().decrement();
            this.cancel();
        }
    }

    /**
     * @return <code>true</code>if, and only if, the server process is alive. You can register a listener to this, it is thread safe.
     */
    public ReadOnlyBooleanProperty isAlive() {
        return ReadOnlyBooleanProperty.readOnlyBooleanProperty(isAlive);
    }

    /**
     * Kill the server process and terminates this thread. Warning! There is no controlled and safe shutdown of the server using the kill!
     */
    public void kill() {
        this.serverProcess.destroy();
    }

}
