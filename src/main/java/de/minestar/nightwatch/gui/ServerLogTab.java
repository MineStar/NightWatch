package de.minestar.nightwatch.gui;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;
import de.minestar.nightwatch.core.ServerLogEntry;
import de.minestar.nightwatch.server.LogLevel;

public class ServerLogTab extends Tab {

    private TableView<ServerLogEntry> logTable;
    private List<ServerLogEntry> allLogs;
    
    private boolean hasServer;

    private StringProperty rowCountProperty;
    private StringProperty selectedRowCountProperty;

    public ServerLogTab(String name, List<ServerLogEntry> archivedLogs, boolean hasServer) {
        super(name);
        this.allLogs = archivedLogs;
        this.hasServer = hasServer;
        createContent();
    }

    private void createContent() {
        BorderPane vBox = new BorderPane();
        vBox.setPadding(new Insets(10));
        vBox.setTop(createStatusPane());

        this.logTable = new TableView<>();
        logTable.getStylesheets().add(getClass().getResource("/styles/tableview.css").toExternalForm());
        logTable.getStyleClass().add("log-table");
        logTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Copy selected lines to clipboard
        logTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<ServerLogEntry>) c -> {
            selectedRowCountProperty.setValue(c.getList().size() + "");
            StringBuilder sBuilder = new StringBuilder();
            c.getList().parallelStream().forEachOrdered((ServerLogEntry l) -> {
                sBuilder.append(l.getTime().format(MainGUI.GERMAN_FORMAT)).append(' ').append(l.getLogLevel()).append(' ').append(l.getText()).append('\n');
            });
            ClipboardContent content = new ClipboardContent();
            content.putString(sBuilder.toString());
            Clipboard.getSystemClipboard().setContent(content);

        });

        TableColumn<ServerLogEntry, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(log -> {
            SimpleStringProperty property = new SimpleStringProperty();
            property.setValue(MainGUI.GERMAN_FORMAT.format(log.getValue().getTime()));
            return property;
        });
        logTable.getColumns().add(timeColumn);

        TableColumn<ServerLogEntry, LogLevel> logLevelColumn = new TableColumn<>("LogLevel");
        logLevelColumn.setCellValueFactory(new PropertyValueFactory<>("logLevel"));
        logTable.getColumns().add(logLevelColumn);

        TableColumn<ServerLogEntry, String> textColumn = new TableColumn<>("Text");
        textColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        logTable.getColumns().add(textColumn);

        // Color the rows depending on the log level
        logTable.setRowFactory(new Callback<TableView<ServerLogEntry>, TableRow<ServerLogEntry>>() {
            @Override
            public TableRow<ServerLogEntry> call(TableView<ServerLogEntry> param) {

                return new TableRow<ServerLogEntry>() {
                    @Override
                    protected void updateItem(ServerLogEntry item, boolean empty) {
                        super.updateItem(item, empty);
                        getStyleClass().removeIf(e -> e.startsWith("log-table-cell"));
                        getStyleClass().add("log-table-cell");
                        if (!empty) {
                            switch (item.getLogLevel()) {
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
                        }else {
                            getStyleClass().clear();
                        }
                    }
                };
            }
        });

        // TODO: Set focus on it
        TextField consoleInput = new TextField();
        consoleInput.getStylesheets().add(getClass().getResource("/styles/commandLine.css").toExternalForm());
        consoleInput.getStyleClass().add("command-input");
        consoleInput.textProperty().addListener((observ, oldVal, newVal) -> {
            // TODO: Executing
        });

        logTable.getItems().addListener((ListChangeListener<ServerLogEntry>) c -> rowCountProperty.setValue(c.getList().size() + ""));
        logTable.getItems().addAll(allLogs);

        vBox.setCenter(logTable);
        vBox.setBottom(consoleInput);
        setContent(vBox);
    }

    public ObservableList<ServerLogEntry> getLogList() {
        return logTable.getItems();
    }

    private Node createStatusPane() {

        FlowPane statusPane = new FlowPane(Orientation.HORIZONTAL, 10, 0);
        statusPane.setPadding(new Insets(0, 0, 5, 0));

        Label rowCountText = new Label();
        rowCountProperty = rowCountText.textProperty();

        Label selectedRowCountText = new Label("0");
        selectedRowCountProperty = selectedRowCountText.textProperty();

        statusPane.getChildren().addAll(new Label("Entries:"), rowCountText, new Label("Selected:"), selectedRowCountText);

        return statusPane;
    }
    
    public void applyFilter(Predicate<ServerLogEntry> predicate) {
        logTable.getItems().setAll(allLogs.stream().filter(predicate).collect(Collectors.toList()));
    }
    
    public LocalDateTime firstDate() {
        return this.allLogs.parallelStream().map(ServerLogEntry::getTime).min(LocalDateTime::compareTo).orElse(LocalDateTime.now());
    }
    
    public LocalDateTime lastDate() {
        return this.allLogs.parallelStream().map(ServerLogEntry::getTime).max(LocalDateTime::compareTo).orElse(LocalDateTime.now());
    }
    
    public boolean hasServer () {
        return hasServer;
    }
   
}
