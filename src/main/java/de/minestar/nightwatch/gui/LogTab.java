package de.minestar.nightwatch.gui;

import java.time.LocalDateTime;
import java.util.function.Predicate;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLog;
import de.minestar.nightwatch.logging.ServerLogEntry;

public class LogTab extends Tab {

    protected TableView<ServerLogEntry> logTable;
    protected ServerLog serverlog;
    protected Predicate<ServerLogEntry> currentFilter;

    // TODO: Reimplement
//    private StringProperty rowCountProperty;
//    private StringProperty selectedRowCountProperty;

    public LogTab(String name, ServerLog serverLog) {
        super(name);
        this.serverlog = serverLog;
        this.currentFilter = (e -> true);
        createContent();
        this.logTable.getItems().addAll(serverLog.unmodifielableEntries());
        this.setClosable(true);
    }

    private void createContent() {
        BorderPane Pane = new BorderPane();
        Pane.setPadding(new Insets(10));
        Pane.setTop(createTop());
//        vBox.setTop(createStatusPane());

        this.logTable = new TableView<>();
        logTable.getStylesheets().add(getClass().getResource("/styles/tableview.css").toExternalForm());
        logTable.getStyleClass().add("log-table");
        logTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Copy selected lines to clipboard
        logTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<ServerLogEntry>) c -> {
//            selectedRowCountProperty.setValue(c.getList().size() + ""); // TODO: Reimplement
            StringBuilder sBuilder = new StringBuilder();
            c.getList().parallelStream().forEachOrdered((ServerLogEntry l) -> {
                sBuilder.append(l.getTime().format(MainGUI.GERMAN_FORMAT)).append(' ').append(l.getLogLevel()).append(' ').append(l.getText()).append('\n');
            });
            ClipboardContent content = new ClipboardContent();
            content.putString(sBuilder.toString());
            Clipboard.getSystemClipboard().setContent(content);

        });

        TableColumn<ServerLogEntry, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setSortable(false);
        timeColumn.setCellValueFactory(log -> {
            SimpleStringProperty property = new SimpleStringProperty();
            property.setValue(MainGUI.GERMAN_FORMAT.format(log.getValue().getTime()));
            return property;
        });
        logTable.getColumns().add(timeColumn);

        TableColumn<ServerLogEntry, String> sourceColumn = new TableColumn<>("Source");
        sourceColumn.setSortable(false);
        sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        logTable.getColumns().add(sourceColumn);

        TableColumn<ServerLogEntry, LogLevel> logLevelColumn = new TableColumn<>("LogLevel");
        logLevelColumn.setSortable(false);
        logLevelColumn.setCellValueFactory(new PropertyValueFactory<>("logLevel"));
        logTable.getColumns().add(logLevelColumn);

        TableColumn<ServerLogEntry, String> textColumn = new TableColumn<>("Text");
        textColumn.setSortable(false);
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
                        } else {
                            getStyleClass().clear();
                        }
                    }
                };
            }
        });
        // TODO: Reimplement
//        logTable.getItems().addListener((ListChangeListener<ServerLogEntry>) c -> rowCountProperty.setValue(c.getList().size() + ""));

        Pane.setCenter(logTable);
        Pane.setBottom(createBottom());
        setContent(Pane);
    }

    protected Node createTop() {
        return null;
    }

    protected Node createBottom() {
        return null;
    }
    // TODO: Reimplement
//    private Node createStatusPane() {
//
//        FlowPane statusPane = new FlowPane(Orientation.HORIZONTAL, 10, 0);
//        statusPane.setPadding(new Insets(0, 0, 5, 0));
//
//        Label rowCountText = new Label();
//        rowCountProperty = rowCountText.textProperty();
//
//        Label selectedRowCountText = new Label("0");
//        selectedRowCountProperty = selectedRowCountText.textProperty();
//
//        statusPane.getChildren().addAll(new Label("Entries:"), rowCountText, new Label("Selected:"), selectedRowCountText);
//
//        return statusPane;
//    }

    public void applyFilter(Predicate<ServerLogEntry> predicate) {

        this.currentFilter = predicate;
        logTable.getItems().setAll(serverlog.applyFilter(predicate));
    }

    public ServerLog getServerlog() {
        return serverlog;
    }

    public LocalDateTime getMinDate() {
        return this.serverlog.minDate().get();
    }

    public LocalDateTime getMaxDate() {
        return this.serverlog.maxDate().get();
    }
}
