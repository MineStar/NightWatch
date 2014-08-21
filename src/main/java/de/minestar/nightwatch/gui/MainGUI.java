package de.minestar.nightwatch.gui;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.minestar.nightwatch.core.ServerLogEntry;
import de.minestar.nightwatch.server.ServerType;

public class MainGUI extends Application {

    public static final DateTimeFormatter GERMAN_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    private ServerLogTab currentSelectedTab;

    private FilterPane filterPane;

    @Override
    public void start(Stage stage) throws Exception {

        this.filterPane = new FilterPane();
        VBox tmp = new VBox(createMenuBar(), createButtonsPane(), new Separator(), filterPane);
        BorderPane bPane = new BorderPane(createTabPane(), tmp, null, null, null);

        filterPane.setDateInterval(currentSelectedTab.firstDate(), currentSelectedTab.lastDate());
        filterPane.registerChangeListener((observ, oldValue, newValue) -> {
            currentSelectedTab.applyFilter(newValue);
        });
        Scene scene = new Scene(bPane);

        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(800);
        stage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        Menu subFileMenu = new Menu("Create Server");
        subFileMenu.getItems().addAll(createServerMenus());

        MenuItem optionsMenu = new MenuItem("Options");
        optionsMenu.setAccelerator(KeyCombination.keyCombination("CTRL+O"));
        MenuItem closeMenu = new MenuItem("Exit");
        closeMenu.setAccelerator(KeyCombination.keyCombination("ALT+F4"));
        fileMenu.getItems().addAll(subFileMenu, optionsMenu, closeMenu);

        Menu helpMenu = new Menu("Help");

        MenuItem aboutMenuItem = new MenuItem("About");
        aboutMenuItem.setAccelerator(KeyCombination.keyCombination("F1"));
        helpMenu.getItems().add(aboutMenuItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);

        return menuBar;

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
        System.out.println("DEBUG: Create server " + type);
    }

    private Node createButtonsPane() {

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
        startBackupButton.setOnAction(e -> onStartBackup(startBackupButton));
        startBackupButton.setPrefWidth(100);
        Button restoreBackupButton = new Button("Restore backup");
        restoreBackupButton.setOnAction(e -> onRestoreBackup(startBackupButton));
        restoreBackupButton.setPrefWidth(100);

        Label choiceViewLabel = new Label("Show: ");
        ChoiceBox<String> choiceView = new ChoiceBox<String>(FXCollections.observableArrayList("Console", "Statistics"));
        choiceView.getSelectionModel().select(0);

        hbox.getChildren().addAll(toggleStatusButton, new Separator(Orientation.VERTICAL), openDirButton, new Separator(Orientation.VERTICAL), startBackupButton, restoreBackupButton, new Separator(Orientation.VERTICAL), choiceViewLabel, choiceView);

        return hbox;
    }

    private void onRestoreBackup(Button startBackupButton) {
        // TODO: Implement backup
    }

    private void onStartBackup(Button startBackupButton) {
        // TODO: Implement backup
    }

    private void onOpenDirAction(Button openDirButton) {
        try {
            Desktop.getDesktop().open(new File(".."));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onToggleStatusButton(Button button) {
        // Switch phase
        if (button.getText().equals("Start Server")) {
            button.setText("Stop Server");
            button.setStyle("-fx-base: #e55852");
        } else {
            button.setText("Start Server");
            button.setStyle("-fx-base: #68d188");
        }
    }

    private Node createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        // TODO: Just for developing
        ServerLogTab testTab = new ServerLogTab("Test Tab", loadData());

        currentSelectedTab = testTab;
        tabPane.getTabs().add(testTab);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observ, oldValue, newValue) -> {
            currentSelectedTab = (ServerLogTab) newValue;
        });

        return tabPane;
    }

    // Temporary -- Just for debugging
    private List<ServerLogEntry> loadData() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<ServerLogEntry> logs = new ArrayList<>();

        try (BufferedReader bReader = new BufferedReader(new FileReader("src/test/resources/big.txt"))) {
            String line = "";
            String time = "";
            String log = "";
            String level = "";
            while ((line = bReader.readLine()) != null) {
                time = line.substring(0, 19);
                int index = line.indexOf(']');
                if (index == -1) {
                    level = "ALL";
                    log = line.substring(20);
                } else {
                    level = line.substring(21, index);
                    log = line.substring(index + 1);
                }

                if (time == null || log == null)
                    continue;
                logs.add(new ServerLogEntry(LocalDateTime.parse(time, df), level, log));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
