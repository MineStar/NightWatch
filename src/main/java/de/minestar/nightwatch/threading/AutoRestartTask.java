package de.minestar.nightwatch.threading;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.concurrent.Task;
import de.minestar.nightwatch.util.DurationUtil;

/**
 * Thread to print warning texts, that the server is restarting in t he future and, after this is done, it will restart the server
 */
public class AutoRestartTask extends Task<Void> {

    private static final String INITIATE_RESTART_COMMAND = "stop";

    private LinkedBlockingQueue<String> commandQueue;
    private LocalDateTime restartTime;
    private List<Duration> restartWarnings;

    public AutoRestartTask(LocalDateTime restartTime, List<Duration> restartWarnings, LinkedBlockingQueue<String> commandQueue) {
        this.restartTime = restartTime;
        this.restartWarnings = restartWarnings;
        // first warning = greatest difference to the restart
        this.restartWarnings.sort(Comparator.reverseOrder());
        this.commandQueue = commandQueue;
    }

    @Override
    protected Void call() throws Exception {
        long progress = 0L;
        long max = restartWarnings.size() + 1L;
        // Sleep until warning time and print the warnings
        for (Duration restartWarning : restartWarnings) {
            // Calculate the time stamp to warn
            LocalDateTime warningTime = restartTime.minus(restartWarning);
            Duration sleepDuration = Duration.between(LocalDateTime.now(), warningTime);
            // Warning time was in the past
            if (sleepDuration.isNegative())
                continue;

            long sleepTime = sleepDuration.toMillis();
            updateProgress(++progress, max);
            updateMessage("Sleep for Warning " + DurationUtil.format(sleepDuration));

            // Sleep until the warning time
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ignore) {
                // Only called when auto restart thread is canceled
            }
            printMessageOnServer("Automatic restart in " + DurationUtil.format(restartWarning));
        }
        // Every warning was printed

        // Calculate the time to sleep until the restart
        Duration sleepDuration = Duration.between(LocalDateTime.now(), restartTime);
        updateProgress(++progress, max);
        updateMessage("Sleep for Restart " + DurationUtil.format(sleepDuration));
        // Sleep until the restart
        // Sleep until the warning time
        try {
            Thread.sleep(sleepDuration.toMillis());
        } catch (InterruptedException ignore) {
            // Only called when auto restart thread is canceled
        }
        // Initiate the restart
        printMessageOnServer("Restart the server");
        commandQueue.add(INITIATE_RESTART_COMMAND);

        return null;
    }

    private void printMessageOnServer(String message) {
        commandQueue.add("say " + message);
    }
}
