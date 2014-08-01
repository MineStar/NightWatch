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
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import de.minestar.nightwatch.server.LogLevel;
import de.minestar.nightwatch.server.ServerType;

public class MainGUI extends Application {

    private DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TableView<Log> table;

    private List<Log> allLogs;

    @Override
    public void start(Stage stage) throws Exception {
        this.allLogs = loadData();

        VBox tmp = new VBox(createMenuBar(), createButtonsPane(), new Separator(), createFilterPane());
        BorderPane bPane = new BorderPane(createTabPane(), tmp, null, null, null);

        Scene scene = new Scene(bPane);

        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(800);
        stage.show();
    }

    // Temporary -- Just for debugging
    private List<Log> loadData() {
        ArrayList<Log> logs = new ArrayList<>();

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
                logs.add(new Log(time, level, log));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
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

    }

    private void onStartBackup(Button startBackupButton) {

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

        tabPane.getTabs().addAll(createTab("Test Tab 1"));
        return tabPane;
    }

    private static final DateTimeFormatter GERMAN_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    private Node createFilterPane() {

        FlowPane filterPane = new FlowPane(10, 10);
        filterPane.getStylesheets().add(getClass().getResource("/styles/datetextfield.css").toExternalForm());
        filterPane.setAlignment(Pos.CENTER_LEFT);
        filterPane.setPadding(new Insets(10));

        LocalDateTime firstDate = allLogs.parallelStream().map(Log::getTime).min(LocalDateTime::compareTo).orElse(LocalDateTime.now());
        LocalDateTime lastDate = allLogs.parallelStream().map(Log::getTime).max(LocalDateTime::compareTo).orElse(LocalDateTime.now());
        TextField startDateTextField = new TextField(firstDate.format(GERMAN_FORMAT));
        Button resetStartDateButton = new Button(null, new ImageView(new Image(getClass().getResourceAsStream("/icons/revert16.png"))));
        resetStartDateButton.setOnAction(e -> {
            startDateTextField.setText(firstDate.format(GERMAN_FORMAT));
        });

        TextField endDateTextField = new TextField(lastDate.format(GERMAN_FORMAT));
        Button resetEndDateButton = new Button(null, new ImageView(new Image(getClass().getResourceAsStream("/icons/revert16.png"))));
        resetEndDateButton.setOnAction(e -> {
            endDateTextField.setText(lastDate.format(GERMAN_FORMAT));
        });

        startDateTextField.textProperty().addListener((observ, oldValue, newValue) -> {
            try {
                LocalDateTime startDate = LocalDateTime.parse(newValue, GERMAN_FORMAT);
                onIntervalSet(startDate, LocalDateTime.parse(endDateTextField.getText(), GERMAN_FORMAT));
            } catch (Exception e) {
                startDateTextField.setText(oldValue);
                startDateTextField.getStyleClass().add("date-textfield-error");
                // Remove the red background after 2 seconds
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> startDateTextField.getStyleClass().remove("date-textfield-error"));
                        t.cancel();
                    }
                }, 2000);
            }
        });
        endDateTextField.textProperty().addListener((observ, oldValue, newValue) -> {
            try {
                LocalDateTime endDate = LocalDateTime.parse(newValue, GERMAN_FORMAT);
                onIntervalSet(LocalDateTime.parse(startDateTextField.getText(), GERMAN_FORMAT), endDate);
            } catch (Exception e) {
                endDateTextField.setText(oldValue);
                endDateTextField.getStyleClass().add("date-textfield-error");
                // Remove the red background after 2 seconds
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> endDateTextField.getStyleClass().remove("date-textfield-error"));
                        t.cancel();
                    }
                }, 2000);
            }
        });

        startDateTextField.setPrefWidth(120);
        endDateTextField.setPrefWidth(120);

        ChoiceBox<LogLevel> minimumlogLevel = new ChoiceBox<LogLevel>(FXCollections.observableArrayList(LogLevel.values()));
        minimumlogLevel.setPrefWidth(100);
        minimumlogLevel.getSelectionModel().select(0);
        minimumlogLevel.valueProperty().addListener((observ, oldValue, newValue) -> {
            onLogLevelSet(newValue);
        });

        TextField filterText = new TextField();
        filterText.setPrefWidth(200);
        filterText.textProperty().addListener((value, oldValue, newValue) -> onFilterText(value, oldValue, newValue));

        filterPane.getChildren().addAll(new Label("From"), startDateTextField, resetStartDateButton, new Label("To"), endDateTextField, resetEndDateButton, new Separator(Orientation.VERTICAL), new Label("Level >= "), minimumlogLevel, new Label("Search Text"), filterText);

        return filterPane;
    }

    private void onLogLevelSet(LogLevel minimumLevel) {
        filterTableContent(e -> minimumLevel.compareTo(e.logLevel) <= 0);
    }

    private void onIntervalSet(LocalDateTime start, LocalDateTime end) {
        filterTableContent(e -> (e.time.isEqual(end) || e.time.isBefore(end)) && (e.time.isAfter(start) || e.time.isEqual(start)));
    }

    private void filterTableContent(Predicate<Log> filterOperation) {
        table.getItems().setAll(allLogs.stream().filter(filterOperation).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    private Tab createTab(String name) {
        Tab tab = new Tab(name);

        BorderPane vBox = new BorderPane();
        vBox.setPadding(new Insets(10));
        vBox.setTop(createStatusPane());

        TableView<Log> table = new TableView<Log>();
        table.getStylesheets().add(getClass().getResource("/styles/tableview.css").toExternalForm());
        table.getStyleClass().add("log-table");
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Copy selected lines to clipboard
        table.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Log>) c -> {
            selectedRowCountProperty.setValue(c.getList().size() + "");
            StringBuilder sBuilder = new StringBuilder();
            c.getList().parallelStream().forEachOrdered((Log l) -> {
                sBuilder.append(l.time.format(GERMAN_FORMAT)).append(' ').append(l.logLevel).append(' ').append(l.text).append('\n');
            });
            ClipboardContent content = new ClipboardContent();
            content.putString(sBuilder.toString());
            Clipboard.getSystemClipboard().setContent(content);

        });

        TableColumn<Log, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(log -> {
            SimpleStringProperty property = new SimpleStringProperty();
            property.setValue(GERMAN_FORMAT.format(log.getValue().time));
            return property;
        });

        TableColumn<Log, LogLevel> logLevelColumn = new TableColumn<>("LogLevel");
        logLevelColumn.setCellValueFactory(new PropertyValueFactory<>("logLevel"));

        TableColumn<Log, String> textColumn = new TableColumn<>("Text");
        textColumn.setCellValueFactory(new PropertyValueFactory<>("text"));

        // Color the rows depending on the log level
        table.setRowFactory(new Callback<TableView<Log>, TableRow<Log>>() {
            @Override
            public TableRow<Log> call(TableView<Log> param) {

                return new TableRow<Log>() {
                    @Override
                    protected void updateItem(Log item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            getStyleClass().removeIf(e -> e.startsWith("log-table-cell"));
                            getStyleClass().add("log-table-cell");
                            switch (item.logLevel) {
                                case CONFIG :
                                    getStyleClass().add("log-table-cell-config");
                                    break;
                                case FINE :
                                    getStyleClass().add("log-table-cell-fine");
                                    break;
                                case FINER :
                                    getStyleClass().add("log-table-cell-finesr");
                                    break;
                                case FINEST :
                                    getStyleClass().add("log-table-cell-finest");
                                    break;
                                case INFO :
                                    getStyleClass().add("log-table-cell-info");
                                    break;
                                case WARNING :
                                    getStyleClass().add("log-table-cell-warning");
                                    break;
                                case SEVERE :
                                    getStyleClass().add("log-table-cell-severe");
                                    break;
                                default :
                                    break;

                            }
                        }
                    }
                };
            }
        });

        table.getColumns().addAll(timeColumn, logLevelColumn, textColumn);

        // TODO: Set focus on it
        TextField consoleInput = new TextField();
        consoleInput.getStylesheets().add(getClass().getResource("/styles/commandLine.css").toExternalForm());
        consoleInput.getStyleClass().add("command-input");

        table.getItems().addListener((ListChangeListener<Log>) c -> rowCountProperty.setValue(c.getList().size() + ""));
        table.getItems().addAll(allLogs);

        vBox.setCenter(table);
        vBox.setBottom(consoleInput);
        tab.setContent(vBox);

        this.table = table;
        return tab;
    }

    private StringProperty rowCountProperty;
    private StringProperty selectedRowCountProperty;

    private Node createStatusPane() {

        FlowPane statusPane = new FlowPane(Orientation.HORIZONTAL, 10, 0);
        statusPane.setPadding(new Insets(0, 0, 5, 0));

        Label rowCountText = new Label();
        rowCountProperty = rowCountText.textProperty();

        Label selectedRowCountText = new Label("0");
        selectedRowCountProperty = selectedRowCountText.textProperty();

        statusPane.getChildren().addAll(new Label("Rows:"), rowCountText, new Label("SelectedRows:"), selectedRowCountText);

        return statusPane;
    }
    private void onFilterText(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        filterTableContent(e -> e.text.contains(newValue));
    }

    protected class Log {
        private final LocalDateTime time;
        private final String text;
        private final LogLevel logLevel;

        public Log(String time, String logLevel, String text) {
            this.time = LocalDateTime.parse(time, df);
            this.logLevel = LogLevel.getByName(logLevel);
            this.text = text;

        }

        public String getText() {
            return text;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public LogLevel getLogLevel() {
            return logLevel;
        }

        @Override
        public String toString() {
            return "Log [time=" + time + ", text=" + text + ", logLevel=" + logLevel + "]";
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

}
