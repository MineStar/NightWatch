package de.minestar.nightwatch.logging;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ServerLog {

    private ObservableList<ServerLogEntry> entries;
    private ObjectProperty<LocalDateTime> minDate;
    private ObjectProperty<LocalDateTime> maxDate;

    private List<Consumer<ServerLogEntry>> someName;

    public ServerLog() {
        this(new ArrayList<>());
    }

    public ServerLog(List<ServerLogEntry> entries) {
        this.someName = new ArrayList<>();
        this.entries = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(entries));
        // Search for min and max
        LocalDateTime min = this.entries.stream().map(ServerLogEntry::getTime).min(LocalDateTime::compareTo).orElse(LocalDateTime.now());
        LocalDateTime max = this.entries.stream().map(ServerLogEntry::getTime).max(LocalDateTime::compareTo).orElse(LocalDateTime.now());
        this.minDate = new SimpleObjectProperty<>(min.minusSeconds(1));
        this.maxDate = new SimpleObjectProperty<>(max.plusSeconds(1));

        // Register min-max checker to the consumer list
        // This enables auto recalculating the min and max values of this log
        registerSynchronousConsumer((entry) -> {
            if (entry.getTime().isBefore(minDate.get())) {
                minDate.set(entry.getTime().minusSeconds(1));
            }
            if (entry.getTime().isAfter(maxDate.get())) {
                maxDate.set(entry.getTime().plusSeconds(1));
            }
        });
    }

    public List<ServerLogEntry> applyFilter(Predicate<ServerLogEntry> filter) {
        return entries.stream().filter(filter).collect(Collectors.toList());
    }

    public ObservableList<ServerLogEntry> unmodifielableEntries() {
        return FXCollections.unmodifiableObservableList(entries);
    }

    public void add(ServerLogEntry entry) {
        this.entries.add(entry);

        // Run registered consumers syncrhon with the fx thread
        Platform.runLater(() -> {
            for (Consumer<ServerLogEntry> consumer : someName) {
                consumer.accept(entry);
            }
        });
    }

    public void registerSynchronousConsumer(Consumer<ServerLogEntry> consumer) {
        this.someName.add(consumer);
    }

    public ReadOnlyObjectProperty<LocalDateTime> minDate() {
        return minDate;
    }

    public ReadOnlyObjectProperty<LocalDateTime> maxDate() {
        return maxDate;
    }

}
