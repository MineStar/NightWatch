package de.minestar.nightwatch.gui.dialog;

import java.time.Duration;
import java.time.LocalTime;

import javafx.stage.Stage;
import de.minestar.nightwatch.server.ObservedMinecraftServer;
import de.minestar.nightwatch.util.DurationUtil;

public class EditServerDialog extends CreateServerDialog {

    public EditServerDialog(Stage stage, ObservedMinecraftServer currentServer) {
        super(stage);

        this.serverName.set(currentServer.getName());
        this.serverFile.set(currentServer.getServerFile());

        this.autoBackup.set(currentServer.doBackupOnShutdown());
        this.autoRestart.set(currentServer.doRestartOnShutdown());

        this.minMemory.set(currentServer.getMinHeapSize());
        this.maxMemory.set(currentServer.getMaxHeapSize());

        this.isJava7.set(currentServer.useJava7());
        this.permGenSize.set(currentServer.getMaxPermGen());

        this.vmOptions.set(currentServer.getOtherVmOptions());

        if (currentServer.doAutoRestarts()) {
            this.doAutoRestarts.set(true);
            StringBuilder sBuilder = new StringBuilder();
            for (LocalTime restartTime : currentServer.getRestartTimes()) {
                sBuilder.append(restartTime.toString()).append(System.lineSeparator());
            }

            this.restartTimes.set(sBuilder.toString());

            sBuilder = new StringBuilder();
            for (Duration warningInterval : currentServer.getWarningIntervals()) {
                sBuilder.append(DurationUtil.format(warningInterval)).append(System.lineSeparator());
            }
            this.restartWarnings.set(sBuilder.toString());
        }

    }
}
