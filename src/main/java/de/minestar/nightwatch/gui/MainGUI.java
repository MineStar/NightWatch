package de.minestar.nightwatch.gui;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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

public class MainGUI extends Application {

    public static final DateTimeFormatter GERMAN_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    private TabPane serverTabPane;
    private LogTab currentSelectedTab;

    private FilterPane filterPane;

    protected static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {

        this.filterPane = new FilterPane();
        VBox tmp = new VBox(createMenuBar(), new Separator(), filterPane);
        BorderPane bPane = new BorderPane(createTabPane(), tmp, null, null, null);

        filterPane.registerChangeListener((observ, oldValue, newValue) -> currentSelectedTab.applyFilter(newValue));

        if (Core.serverManager.registeredServers().isEmpty())
            filterPane.setDisable(true);
        else
            Core.serverManager.registeredServers().values().forEach(server -> createServerTab(server));

        Scene scene = new Scene(bPane);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/minestar_logo_32.png")));
        stage.setOnCloseRequest(e -> {
            // Prevent closing server manager while one or more server are
            // running
            if (Core.runningServers > 0) {
                Dialogs.create().style(DialogStyle.NATIVE).message("Can't close program while servers are running!").showWarning();
                e.consume();

            } else {
                // Ask if the user really want to close the server
                Action result = Dialogs.create().style(DialogStyle.NATIVE).message("Exit program?").showConfirm();
                if (result != Dialog.Actions.YES)
                    e.consume();
            }
        });
        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(800);
        stage.show();
        MainGUI.stage = stage;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem createServerMenu = new MenuItem("Create Server");
        createServerMenu.setOnAction(e -> onCreateServer());
        createServerMenu.setAccelerator(KeyCombination.keyCombination("CTRL+N"));

        MenuItem openLogMenu = new MenuItem("Open Logfile");
        openLogMenu.setAccelerator(KeyCombination.keyCombination("CTRL+L"));
        openLogMenu.setOnAction(e -> onOpenLogFile());

        MenuItem optionsMenu = new MenuItem("Options");
        optionsMenu.setAccelerator(KeyCombination.keyCombination("CTRL+O"));
        MenuItem closeMenu = new MenuItem("Exit");
        closeMenu.setAccelerator(KeyCombination.keyCombination("ALT+F4"));
        fileMenu.getItems().addAll(createServerMenu, openLogMenu, optionsMenu, closeMenu);

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

    private void onCreateServer() {

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

    private Node createTabPane() {
        this.serverTabPane = new TabPane();

        serverTabPane.getSelectionModel().selectedItemProperty().addListener((observ, oldValue, newValue) -> {
            this.filterPane.setDisable(newValue == null);

            if (newValue == null) {
                return;
            }
            this.currentSelectedTab = (LogTab) newValue;
            // Disable the button bar, when only a log is read
            this.filterPane.bindDateInterval(currentSelectedTab.getServerlog().minDate(), currentSelectedTab.getServerlog().maxDate());
        });

        return serverTabPane;
    }
}
