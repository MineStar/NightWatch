package de.minestar.nightwatch.gui.dialog;

import java.time.Duration;
import java.time.LocalTime;

import javafx.stage.Stage;
import de.minestar.nightwatch.server.ObservedJava7Server;
import de.minestar.nightwatch.server.ObservedServer;
import de.minestar.nightwatch.util.DurationUtil;

public class EditServerDialog extends CreateServerDialog {

    public EditServerDialog(Stage stage, ObservedServer currentServer) {
        super(stage);
        this.autoBackup.set(currentServer.doAutoBackupOnShutdown());
        this.autoRestart.set(currentServer.doAutoRestartOnShutdown());
        this.maxMemory.set(currentServer.getMaxMemory());
        this.minMemory.set(currentServer.getMinMemory());

        this.isJava7.set(currentServer instanceof ObservedJava7Server);
        if (this.isJava7.get())
            this.permGenSize.set(((ObservedJava7Server) currentServer).getPermGenSize());

        this.serverFile.set(currentServer.getServerFile());
        this.serverName.set(currentServer.getName());
        this.vmOptions.set(currentServer.getVmOptions());

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
