package de.minestar.nightwatch.gui;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
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

import org.controlsfx.dialog.Dialog.Actions;
import org.controlsfx.dialog.Dialogs;

import de.minestar.nightwatch.core.Core;
import de.minestar.nightwatch.gui.dialog.CreateServerDialog;
import de.minestar.nightwatch.gui.dialog.DialogsUtil;
import de.minestar.nightwatch.gui.dialog.GeneralOptionsDialog;
import de.minestar.nightwatch.logging.LogReader;
import de.minestar.nightwatch.logging.ServerLog;
import de.minestar.nightwatch.logging.ServerLogEntry;
import de.minestar.nightwatch.server.ObservedMinecraftServer;

public class MainGUI extends Application {

    private static final String APPLICATION_ICON = "/icons/minestar_logo_32.png";

    public static final DateTimeFormatter GERMAN_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.GERMAN);

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

        stage.getIcons().add(new Image(getClass().getResourceAsStream(APPLICATION_ICON)));
        stage.setOnCloseRequest(e -> {
            // Prevent closing server manager while one or more server are
            // running
            if (Core.runningServers > 0) {
                DialogsUtil.createOkCancelDialog("Can't close progam while servers are running!").showWarning();
                e.consume();
            } else {
                // Ask if the user really want to close the server
                Dialogs dialog = DialogsUtil.createOkCancelDialog("Exit program?");
                if (dialog.showConfirm() != Actions.OK) {
                    e.consume();
                }
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
        optionsMenu.setOnAction(e -> onOpenOption());

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

    private void onOpenOption() {
        GeneralOptionsDialog optionsDialog = new GeneralOptionsDialog(stage);
        optionsDialog.show();
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
            Core.logger.error("Opening log file {}", logFile.toString());
            Core.logger.catching(e);
        }

    }

    private void onCreateServer() {

        CreateServerDialog dialog = new CreateServerDialog(stage);
        Optional<ObservedMinecraftServer> result = dialog.startDialog();
        if (!result.isPresent())
            return;

        ObservedMinecraftServer server = result.get();
        Core.serverManager.registeredServers().put(server.getName().toLowerCase(), server);
        createServerTab(server);
    }

    private void createServerTab(ObservedMinecraftServer server) {
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
