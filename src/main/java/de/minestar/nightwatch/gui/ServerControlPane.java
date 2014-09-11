package de.minestar.nightwatch.gui;

import java.awt.Desktop;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import de.minestar.nightwatch.core.Core;
import de.minestar.nightwatch.server.ObservedServer;
import de.minestar.nightwatch.threading.BackupTask;
import de.minestar.nightwatch.threading.RestoreBackupTask;

public class ServerControlPane extends FlowPane {

    private static final String ICON_BUTTON_START = "start.png";
    private static final String ICON_BUTTON_SHUTDOWN = "shutdown.png";
    private static final String ICON_BUTTON_STOP = "stop.png";
    private static final String ICON_BUTTON_OPENDIR = "openfolder.png";
    private static final String ICON_BUTTON_CREATE_BACKUP = "backup.png";
    private static final String ICON_BUTTON_LOAD_BACKUP = "loadbackup.png";
    private static final String ICON_BUTTON_SETTINGS = "settings.png";

    private Button startButton;
    private Button shutdownButton;
    private Button stopButton;

    private Button openDirButton;

    private Button createBackupButton;
    private Button restoreBackupButton;

    private Button settingsButton;

    private BooleanProperty isRunning;

    public ServerControlPane(ServerLogTab parent) {
        super(20, 20);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(10));
        this.getStylesheets().add(getClass().getResource("/styles/controlPanel.css").toExternalForm());

        this.isRunning = new SimpleBooleanProperty(false);

        getChildren().addAll(createServerStatusButtons(parent));
        getChildren().add(new Separator(Orientation.VERTICAL));
        getChildren().addAll(createBackupButtons(parent));
        getChildren().add(new Separator(Orientation.VERTICAL));
        getChildren().add(createOpenDirectoryButton(parent));
        getChildren().add(createSettingsButton(parent));

    }

    private List<Node> createServerStatusButtons(final ServerLogTab parent) {
        this.startButton = new Button("Start", loadIcon(ICON_BUTTON_START));
        this.startButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        this.startButton.getStyleClass().add("startButton");
        this.startButton.setOnAction(e -> onStartServer(parent));
        this.startButton.disableProperty().bind(isRunning);

        this.shutdownButton = new Button("Shutdown", loadIcon(ICON_BUTTON_SHUTDOWN));
        this.shutdownButton.getStyleClass().add("shutdownButton");
        this.shutdownButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        this.shutdownButton.setOnAction(e -> onShutdownServer(parent));
        this.shutdownButton.disableProperty().bind(isRunning.not());

        this.stopButton = new Button("Stop", loadIcon(ICON_BUTTON_STOP));
        this.stopButton.getStyleClass().add("stopButton");
        this.stopButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        this.stopButton.disableProperty().bind(isRunning.not());
        this.stopButton.setOnAction(e -> onStopServer(parent));

        return Arrays.asList(startButton, shutdownButton, stopButton);

    }

    private void onStartServer(ServerLogTab parent) {

        Action result = Dialogs.create().style(DialogStyle.NATIVE).message("Starting server?").showConfirm();
        if (result != Dialog.Actions.YES)
            return;
        startServer(parent);
    }

    private void startServer(ServerLogTab serverLogTab) {
        serverLogTab.setClosable(false);
        serverLogTab.startServer();
        this.isRunning.set(true);
        serverLogTab.getServerOverWatchThread().isAlive().addListener((observ, oldVal, newVal) -> {
            if (newVal == false) {
                this.isRunning.set(false);
                ObservedServer server = serverLogTab.getServer();
                if (server.doAutomaticBackups()) {
                    BackupTask backupTask = new BackupTask(server, new File(Core.mainConfig.backupFolder().get()));
                    // wait for backup task has ended to initiate eventual
                    // restart
                    if (server.doAutoRestarts()) {
                        backupTask.setOnSucceeded(e -> {
                            // Have to run it synchronous
                            Platform.runLater(() -> initiateRestart(serverLogTab));
                        });
                    }
                    this.startBackup(backupTask);
                } else if (server.doAutoRestarts()) {
                    initiateRestart(serverLogTab);

                }
                serverLogTab.setClosable(true);
            } else {
                return;
            }
        });
    }

    private void onShutdownServer(ServerLogTab parent) {
        Action result = Dialogs.create().style(DialogStyle.NATIVE).message("Safely shutting server down?").showConfirm();
        if (result != Dialog.Actions.YES)
            return;

        parent.shutdownServer();
    }

    private void onStopServer(ServerLogTab parent) {
        Action result = Dialogs.create().style(DialogStyle.NATIVE).message("Warning: Possible loose of data!\nStop the server process(unsafe!)?").showConfirm();
        if (result != Dialog.Actions.YES)
            return;

        parent.shutdownServer();
    }

    private void initiateRestart(ServerLogTab serverLogTab) {

        RestartDialog di = new RestartDialog(MainGUI.stage);
        Action show = di.show();
        if (show == Dialog.Actions.OK) {
            startServer(serverLogTab);
            // do restart
        } else if (show == Dialog.Actions.CANCEL) {
            // Do no restart
            return;

        }
    }

    private Node createOpenDirectoryButton(ServerLogTab parent) {

        this.openDirButton = new Button("Folder", loadIcon(ICON_BUTTON_OPENDIR));
        this.openDirButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        this.openDirButton.setOnAction(event -> {
            try {
                Desktop.getDesktop().open(parent.getServer().getDirectory());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return openDirButton;

    }

    private List<Node> createBackupButtons(ServerLogTab parent) {
        this.createBackupButton = new Button("Create Backup", loadIcon(ICON_BUTTON_CREATE_BACKUP));
        this.createBackupButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        this.createBackupButton.setOnAction(e -> onStartBackup(parent));
        this.createBackupButton.disableProperty().bind(isRunning);

        this.restoreBackupButton = new Button("Restore Backup", loadIcon(ICON_BUTTON_LOAD_BACKUP));
        this.restoreBackupButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        this.restoreBackupButton.setOnAction(e -> onStartRestoreBackup(parent));
        this.restoreBackupButton.disableProperty().bind(isRunning);

        return Arrays.asList(createBackupButton, restoreBackupButton);
    }

    private void onStartBackup(ServerLogTab parent) {

        if (Core.mainConfig.backupFolder().isEmpty().get()) {

            Action result = Dialogs.create().style(DialogStyle.NATIVE).message("You haven't select a backup folder yet. Please select one!").showConfirm();
            System.out.println(result);
            if (result != Dialog.Actions.YES)
                return;

            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setInitialDirectory(new File("."));
            File backupDir = dirChooser.showDialog(MainGUI.stage);
            if (backupDir != null) {
                Core.mainConfig.backupFolder().set(backupDir.getAbsolutePath());
            }
        }

        BackupTask backupTask = new BackupTask(parent.getServer(), new File(Core.mainConfig.backupFolder().get()));
        startBackup(backupTask);

    }

    private void startBackup(BackupTask backupTask) {
        Thread backupThread = new Thread(backupTask, "BackupThread");
        Dialogs.create().style(DialogStyle.NATIVE).showWorkerProgress(backupTask);
        backupTask.exceptionProperty().addListener((observ, oldVal, newVal) -> Dialogs.create().style(DialogStyle.NATIVE).message("Error while creating backup!").showException(newVal));
        backupThread.start();
    }

    private void onStartRestoreBackup(ServerLogTab parent) {
        Action result = Dialogs.create().style(DialogStyle.NATIVE).message("This will delete ALL files in the server folder!").showWarning();
        if (result != Dialog.Actions.OK)
            return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(Core.mainConfig.backupFolder().get()));
        File backupFile = fileChooser.showOpenDialog(MainGUI.stage);
        if (backupFile == null)
            return;

        ObservedServer server = parent.getServer();
        // Check if the backup was from this server
        if (!backupFile.getName().startsWith(server.getName())) {
            result = Dialogs.create().style(DialogStyle.NATIVE).message("This may be not a backup of this server\n" + backupFile.getName() + "\nAre you sure to use this?!").showConfirm();
            if (result != Dialog.Actions.OK)
                return;
        }

        // Start restoration of backup
        RestoreBackupTask restoreBackupTask = new RestoreBackupTask(server.getDirectory(), backupFile);
        Dialogs.create().style(DialogStyle.NATIVE).showWorkerProgress(restoreBackupTask);
        restoreBackupTask.exceptionProperty().addListener((observ, oldVal, newVal) -> Dialogs.create().style(DialogStyle.NATIVE).message("Error while restoring backup!").showException(newVal));

        Thread restoreBackupThread = new Thread(restoreBackupTask, "RestoreBackupThread");
        restoreBackupThread.start();

        restoreBackupTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                Dialogs.create().style(DialogStyle.NATIVE).message("Backup restored").showInformation();;
            });
        });
    }

    private Node createSettingsButton(ServerLogTab parent) {
        this.settingsButton = new Button("Settings", loadIcon(ICON_BUTTON_SETTINGS));
        this.settingsButton.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        this.settingsButton.setOnAction(e -> onOpenServerSettings(parent));
        this.settingsButton.disableProperty().bind(isRunning);

        return settingsButton;
    }

    private void onOpenServerSettings(ServerLogTab parent) {
        // Start option dialog
        ServerOptionsDialog dia = new ServerOptionsDialog(MainGUI.stage, parent.getServer());
        Optional<ObservedServer> result = dia.startDialog();
        // User has canceled the dialog
        if (!result.isPresent())
            return;

        // Update the server
        ObservedServer updatedServer = result.get();
        Core.serverManager.registeredServers().replace(parent.getServer().getName().toLowerCase(), updatedServer);
        parent.getServer().update(updatedServer);
        parent.setText(updatedServer.getName());
    }

    private Node loadIcon(String iconName) {
        return new ImageView(new Image(getClass().getResourceAsStream("/icons/" + iconName)));
    }

}
