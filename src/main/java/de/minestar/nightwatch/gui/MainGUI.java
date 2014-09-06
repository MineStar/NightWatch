package de.minestar.nightwatch.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogStyle;
import org.controlsfx.dialog.Dialogs;

import de.minestar.nightwatch.core.Core;
import de.minestar.nightwatch.logging.LogReader;
import de.minestar.nightwatch.logging.ServerLog;
import de.minestar.nightwatch.logging.ServerLogEntry;
import de.minestar.nightwatch.server.ObservedServer;
import de.minestar.nightwatch.server.ServerType;
import de.minestar.nightwatch.threading.BackupTask;

public class MainGUI extends Application {

    public static final DateTimeFormatter GERMAN_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    private TabPane serverTabPane;
    private LogTab currentSelectedTab;

    private FilterPane filterPane;

    private FlowPane buttonsPane;

    private Stage stage;

    private BooleanProperty disableBackupProperty = new SimpleBooleanProperty(false);

    @Override
    public void start(Stage stage) throws Exception {

        this.filterPane = new FilterPane();
        this.buttonsPane = createButtonsPane();
        VBox tmp = new VBox(createMenuBar(), buttonsPane, new Separator(), filterPane);
        this.buttonsPane.setDisable(true);
        BorderPane bPane = new BorderPane(createTabPane(), tmp, null, null, null);

        filterPane.registerChangeListener((observ, oldValue, newValue) -> {
            currentSelectedTab.applyFilter(newValue);
        });

        Core.serverManager.registeredServers().values().forEach(server -> createServerTab(server));

        Scene scene = new Scene(bPane);

        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(800);
        stage.show();
        this.stage = stage;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        Menu subFileMenu = new Menu("Create Server");
        subFileMenu.getItems().addAll(createServerMenus());

        MenuItem openLogMenu = new MenuItem("Open Logfile");
        openLogMenu.setAccelerator(KeyCombination.keyCombination("CTRL+L"));
        openLogMenu.setOnAction(e -> onOpenLogFile());

        MenuItem optionsMenu = new MenuItem("Options");
        optionsMenu.setAccelerator(KeyCombination.keyCombination("CTRL+O"));
        MenuItem closeMenu = new MenuItem("Exit");
        closeMenu.setAccelerator(KeyCombination.keyCombination("ALT+F4"));
        fileMenu.getItems().addAll(subFileMenu, openLogMenu, optionsMenu, closeMenu);

        Menu helpMenu = new Menu("Help");

        MenuItem aboutMenuItem = new MenuItem("About");
        aboutMenuItem.setAccelerator(KeyCombination.keyCombination("F1"));
        helpMenu.getItems().add(aboutMenuItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);

        return menuBar;

    }

    private void onOpenLogFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose log file");
        fileChooser.setInitialDirectory(new File("."));
        File logFile = fileChooser.showOpenDialog(stage);
        if (logFile == null)
            return;

        try {
            LogReader logReader = new LogReader();
            List<ServerLogEntry> logEntries = logReader.readLogFile(logFile);

            LogTab newTab = new LogTab(logFile.getName(), new ServerLog(logEntries));
            newTab.setClosable(true);
            serverTabPane.getTabs().add(newTab);
            serverTabPane.getSelectionModel().select(newTab);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<MenuItem> createServerMenus() {
        List<MenuItem> menus = new ArrayList<>();

        ServerType[] serverTypes = ServerType.values();
        for (int i = 0; i < serverTypes.length; i++) {
            ServerType serverType = serverTypes[i];
            MenuItem createServerMenu = new MenuItem(serverType.getName());
            createServerMenu.setAccelerator(KeyCombination.keyCombination("CTRL+" + (i + 1)));
            createServerMenu.setOnAction(e -> onCreateServer(serverType));
            menus.add(createServerMenu);
        }

        return menus;
    }

    private void onCreateServer(ServerType type) {

        CreateServerDialog dialog = new CreateServerDialog(stage);
        Optional<ObservedServer> result = dialog.startDialog();
        if (!result.isPresent())
            return;

        ObservedServer server = result.get();
        Core.serverManager.registeredServers().put(server.getName().toLowerCase(), server);
        createServerTab(server);
    }

    private void createServerTab(ObservedServer server) {
        ServerLogTab tab = new ServerLogTab(server);
        serverTabPane.getTabs().add(tab);
        serverTabPane.getSelectionModel().select(tab);
    }

    private FlowPane createButtonsPane() {

        FlowPane hbox = new FlowPane(20, 20);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(10));

        Button toggleStatusButton = new Button("Start Server");
        toggleStatusButton.setStyle("-fx-base: #68d188");
        toggleStatusButton.setPrefWidth(100);
        toggleStatusButton.setOnAction(e -> onToggleStatusButton(toggleStatusButton));

        Button openDirButton = new Button("Open Directory");
        openDirButton.setOnAction(e -> onOpenDirAction(openDirButton));
        openDirButton.setPrefWidth(100);

        Button startBackupButton = new Button("Create backup");
        startBackupButton.disableProperty().bind(disableBackupProperty);
        startBackupButton.setOnAction(e -> onStartBackup(startBackupButton));
        startBackupButton.setPrefWidth(100);

        // TODO: Implement them at another release
//        Button restoreBackupButton = new Button("Restore backup");
//        restoreBackupButton.setOnAction(e -> onRestoreBackup(startBackupButton));
//        restoreBackupButton.setPrefWidth(100);

//        Label choiceViewLabel = new Label("Show: ");
//        ChoiceBox<String> choiceView = new ChoiceBox<String>(FXCollections.observableArrayList("Console", "Statistics"));
//        choiceView.getSelectionModel().select(0);

//        hbox.getChildren().addAll(toggleStatusButton, new Separator(Orientation.VERTICAL), openDirButton, new Separator(Orientation.VERTICAL), startBackupButton, restoreBackupButton, new Separator(Orientation.VERTICAL), choiceViewLabel, choiceView);
        hbox.getChildren().addAll(toggleStatusButton, new Separator(Orientation.VERTICAL), openDirButton, new Separator(Orientation.VERTICAL), startBackupButton);
        return hbox;
    }

    private void onStartBackup(Button startBackupButton) {

        if (Core.mainConfig.backupFolder().isEmpty().get()) {

            Action result = Dialogs.create().style(DialogStyle.NATIVE).message("You haven't select a backup folder yet. Please select one!").showConfirm();
            System.out.println(result);
            if (result != Dialog.Actions.YES)
                return;

            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setInitialDirectory(new File("."));
            File backupDir = dirChooser.showDialog(stage);
            if (backupDir != null) {
                Core.mainConfig.backupFolder().set(backupDir.getAbsolutePath());
            }
        }

        BackupTask backupTask = new BackupTask(((ServerLogTab) this.currentSelectedTab).getServer(), new File(Core.mainConfig.backupFolder().get()));
        startBackup(backupTask);

    }
    private void startBackup(BackupTask backupTask) {
        Thread backupThread = new Thread(backupTask, "BackupThread");
        Dialogs.create().style(DialogStyle.NATIVE).showWorkerProgress(backupTask);
        backupTask.exceptionProperty().addListener((observ, oldVal, newVal) -> Dialogs.create().style(DialogStyle.NATIVE).message("Error while creating backup!").showException(newVal));
        backupThread.start();
    }

//    private void onRestoreBackup(Button startBackupButton) {
//        // TODO: Implement backup
//    }

    private void onOpenDirAction(Button openDirButton) {
        try {
            Desktop.getDesktop().open(((ServerLogTab) currentSelectedTab).getServer().getDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onToggleStatusButton(Button button) {
        if (button.getText().equals("Start Server")) {
            onStartServer(button);

            button.setText("Stop Server");
            button.setStyle("-fx-base: #e55852");
        } else {
            onStopServer();
        }

    }

    private void onStartServer(Button button) {
        if (!(this.currentSelectedTab instanceof ServerLogTab))
            throw new RuntimeException("Starting server on non-server-logtab!");

        Action result = Dialogs.create().style(DialogStyle.NATIVE).message("Starting server?").showConfirm();
        if (result != Dialog.Actions.YES)
            return;

        ServerLogTab serverLogTab = ((ServerLogTab) this.currentSelectedTab);
        serverLogTab.setClosable(false);
        serverLogTab.startServer();
        disableBackupProperty.set(true);
        serverLogTab.getServerOverWatchThread().isAlive().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                button.setText("Start Server");
                button.setStyle("-fx-base: #68d188");
                disableBackupProperty.set(false);
                if (serverLogTab.getServer().doAutomaticBackups()) {
                    BackupTask backupTask = new BackupTask(serverLogTab.getServer(), new File(Core.mainConfig.backupFolder().get()));
                    this.startBackup(backupTask);
                }
                serverLogTab.setClosable(true);

            }
        });
    }

    private void onStopServer() {
        if (!(this.currentSelectedTab instanceof ServerLogTab))
            throw new RuntimeException("Stopping server on non-server-logtab!");

        Action result = Dialogs.create().style(DialogStyle.NATIVE).message("Shutdown the Server?").showConfirm();
        if (result != Dialog.Actions.YES)
            return;

        ((ServerLogTab) this.currentSelectedTab).stopServer();
    }

    private Node createTabPane() {
        this.serverTabPane = new TabPane();

        serverTabPane.getSelectionModel().selectedItemProperty().addListener((observ, oldValue, newValue) -> {
            this.currentSelectedTab = (LogTab) newValue;
            // Disable the button bar, when only a log is read
            this.buttonsPane.setDisable(!(currentSelectedTab instanceof ServerLogTab));
            this.filterPane.bindDateInterval(currentSelectedTab.getServerlog().minDate(), currentSelectedTab.getServerlog().maxDate());
        });

        return serverTabPane;
    }
}
