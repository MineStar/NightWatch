package de.minestar.nightwatch.gui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLogEntry;

public class FilterPane extends FlowPane {

    private DateTimeTextField fromDateTextField;
    private DateTimeTextField toDateTextField;

    private LocalDateTime minDate;
    private LocalDateTime maxDate;

    private LogFilter logFilter;

    public FilterPane() {
        super(10, 10);
        this.logFilter = new LogFilter();
        createGUI();
    }

    private void createGUI() {
        this.getStylesheets().add(getClass().getResource("/styles/datetextfield.css").toExternalForm());
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(10));

        this.fromDateTextField = new DateTimeTextField(MainGUI.GERMAN_FORMAT);
        fromDateTextField.setPrefWidth(120);

        fromDateTextField.dateTimeProperty().addListener((observ, oldValue, newValue) -> {
            logFilter.setToDateFilter(p -> p.getTime().isEqual(newValue) || p.getTime().isAfter(newValue));
        });

        this.toDateTextField = new DateTimeTextField(MainGUI.GERMAN_FORMAT);
        toDateTextField.setPrefWidth(120);

        toDateTextField.dateTimeProperty().addListener((observ, oldValue, newValue) -> {
            logFilter.setToDateFilter(p -> p.getTime().isEqual(newValue) || p.getTime().isBefore(newValue));
        });

        Button resetStartDateButton = new Button(null, new ImageView(new Image(getClass().getResourceAsStream("/icons/revert16.png"))));
        resetStartDateButton.setOnAction(e -> {
            fromDateTextField.setText(minDate.format(MainGUI.GERMAN_FORMAT));
        });

        Button resetEndDateButton = new Button(null, new ImageView(new Image(getClass().getResourceAsStream("/icons/revert16.png"))));
        resetEndDateButton.setOnAction(e -> {
            toDateTextField.setText(maxDate.format(MainGUI.GERMAN_FORMAT));
        });

        ChoiceBox<LogLevel> minimumlogLevel = new ChoiceBox<LogLevel>(FXCollections.observableArrayList(LogLevel.values()));
        minimumlogLevel.setPrefWidth(100);
        minimumlogLevel.getSelectionModel().select(0);
        minimumlogLevel.valueProperty().addListener((observ, oldValue, newValue) -> {
            logFilter.setMinimumLevelFilter(p -> newValue.compareTo(p.getLogLevel()) <= 0);
        });

        TextField filterText = new TextField();
        filterText.setPrefWidth(200);
        filterText.textProperty().addListener((value, oldValue, newValue) -> {
            logFilter.setContainsTextFilter(p -> p.getText().contains(newValue));
        });

        this.getChildren().addAll(new Label("From"), fromDateTextField, resetStartDateButton, new Label("To"), toDateTextField, resetEndDateButton, new Separator(Orientation.VERTICAL), new Label("Level >= "), minimumlogLevel, new Label("Search Text"), filterText);

    }

    public void bindDateInterval(ReadOnlyObjectProperty<LocalDateTime> minBinding, ReadOnlyObjectProperty<LocalDateTime> maxBinding) {
        this.minDate = minBinding.get();
        this.fromDateTextField.setText(minDate.format(MainGUI.GERMAN_FORMAT));
        this.maxDate = maxBinding.get();
        this.toDateTextField.setText(maxDate.format(MainGUI.GERMAN_FORMAT));
        minBinding.addListener((observ, oldVal, newVal) -> {
            this.minDate = newVal;
            this.fromDateTextField.setText(newVal.format(MainGUI.GERMAN_FORMAT));

        });
        maxBinding.addListener((observ, oldVal, newVal) -> {
            this.maxDate = newVal;
            this.toDateTextField.setText(newVal.format(MainGUI.GERMAN_FORMAT));
        });
    }
    public class LogFilter implements Predicate<ServerLogEntry>, ObservableValue<LogFilter> {

        private List<ChangeListener<? super LogFilter>> observers;

        private Predicate<ServerLogEntry> fromDateFilter;
        private Predicate<ServerLogEntry> toDateFilter;
        private Predicate<ServerLogEntry> containsTextFilter;
        private Predicate<ServerLogEntry> minimumLevelFilter;

        public LogFilter() {
            fromDateFilter = toDateFilter = containsTextFilter = minimumLevelFilter = (p) -> true;
            observers = new ArrayList<>();
        }

        public void setFromDateFilter(Predicate<ServerLogEntry> fromDateFilter) {
            this.fromDateFilter = fromDateFilter;
            informObservers();
        }

        public void setToDateFilter(Predicate<ServerLogEntry> toDateFilter) {
            this.toDateFilter = toDateFilter;
            informObservers();
        }

        public void setContainsTextFilter(Predicate<ServerLogEntry> containsTextFilter) {
            this.containsTextFilter = containsTextFilter;
            informObservers();
        }

        public void setMinimumLevelFilter(Predicate<ServerLogEntry> minimumLevelFilter) {
            this.minimumLevelFilter = minimumLevelFilter;
            informObservers();
        }

        @Override
        public boolean test(ServerLogEntry t) {
            return fromDateFilter.and(toDateFilter).and(containsTextFilter).and(minimumLevelFilter).test(t);
        }

        @Override
        public void addListener(ChangeListener<? super LogFilter> listener) {
            this.observers.add(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super LogFilter> listener) {
            this.observers.remove(listener);
        }

        private void informObservers() {
            this.observers.forEach(e -> e.changed(logFilter, this, this));
        }

        @Override
        public void addListener(InvalidationListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LogFilter getValue() {
            return this;
        }
    }

    public void registerChangeListener(ChangeListener<LogFilter> listener) {
        this.logFilter.addListener(listener);
    }
}
