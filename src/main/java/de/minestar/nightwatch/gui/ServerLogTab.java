package de.minestar.nightwatch.gui;

import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import de.minestar.nightwatch.core.Core;
import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLog;
import de.minestar.nightwatch.logging.ServerLogEntry;
import de.minestar.nightwatch.server.ObservedServer;
import de.minestar.nightwatch.threading.ServerOverwatchThread;

public class ServerLogTab extends LogTab {

    private ObservedServer server;
    private ServerOverwatchThread serverOverWatchThread;
    private LinkedBlockingQueue<String> commandQueue;

    public ServerLogTab(ObservedServer server) {
        super(server.getName(), new ServerLog());
        this.getServerlog().entries().addListener((ListChangeListener<ServerLogEntry>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    Platform.runLater(() -> {
                        this.logTable.getItems().addAll(c.getAddedSubList().stream().filter(currentFilter).collect(Collectors.toList()));
                    });
                }
            }
        });

        this.server = server;

        this.setOnCloseRequest(e -> {
            Dialogs builder = Dialogs.create().style(DialogStyle.NATIVE);
            builder = builder.message("Do you want to stop controlling the server " + server.getName() + " ?");
            Action result = builder.showConfirm();
            if (result != Dialog.Actions.YES) {
            } else {
                Core.serverManager.registeredServers().remove(server.getName().toLowerCase());
            }
        });
    }

    @Override
    protected Node createBottom() {

        TextField consoleInput = new TextField();
        consoleInput.getStylesheets().add(getClass().getResource("/styles/commandLine.css").toExternalForm());
        consoleInput.getStyleClass().add("command-input");
        consoleInput.setOnAction(e -> {
            this.commandQueue.add(consoleInput.getText());
            this.serverlog.entries().add(new ServerLogEntry(LocalDateTime.now(), "Console", LogLevel.ALL, consoleInput.getText()));
            consoleInput.clear();
        });

        return consoleInput;
    }

    @Override
    protected Node createTop() {
        return new ServerControlPane(this);
    }

    public void startServer() {
        this.commandQueue = new LinkedBlockingQueue<>();
        this.serverOverWatchThread = new ServerOverwatchThread(this.server, this.serverlog.entries(), this.commandQueue);
        Thread thread = new Thread(this.serverOverWatchThread, this.server.getName() + "_Overwatch");
        thread.start();
    }

    public void shutdownServer() {
        this.commandQueue.add("stop");
        this.serverOverWatchThread = null;
        this.commandQueue = null;
    }

    protected void stopServer() {
        this.serverOverWatchThread.hardStop();
    }

    public ObservedServer getServer() {
        return server;
    }

    public ServerOverwatchThread getServerOverWatchThread() {
        return serverOverWatchThread;
    }
}
