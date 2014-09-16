package de.minestar.nightwatch.gui.dialog;

import javafx.stage.Stage;
import de.minestar.nightwatch.server.ObservedJava7Server;
import de.minestar.nightwatch.server.ObservedServer;

public class ServerOptionsDialog extends CreateServerDialog {

    public ServerOptionsDialog(Stage stage, ObservedServer currentServer) {
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
    }
}
