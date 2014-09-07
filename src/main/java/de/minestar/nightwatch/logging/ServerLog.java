package de.minestar.nightwatch.logging;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ServerLog {

	private ObservableList<ServerLogEntry> entries;
	private ObjectProperty<LocalDateTime> minDate;
	private ObjectProperty<LocalDateTime> maxDate;

	public ServerLog() {
		this(new ArrayList<>());
	}

	public ServerLog(List<ServerLogEntry> entries) {
		this.entries = FXCollections.synchronizedObservableList(FXCollections
				.observableArrayList(entries));
		// Search for min and max
		LocalDateTime min = this.entries.stream().map(ServerLogEntry::getTime)
				.min(LocalDateTime::compareTo).orElse(LocalDateTime.now());
		LocalDateTime max = this.entries.stream().map(ServerLogEntry::getTime)
				.max(LocalDateTime::compareTo).orElse(LocalDateTime.now());
		this.minDate = new SimpleObjectProperty<>(min.minusSeconds(1));
		this.maxDate = new SimpleObjectProperty<>(max.plusSeconds(1));

		this.entries.addListener((ListChangeListener<ServerLogEntry>) c -> {
			while (c.next()) {
				if (c.wasAdded()) {
					c.getAddedSubList().forEach(entry -> {
						// Increase interval to get all log entries within a
						// second span
							if (entry.getTime().isBefore(minDate.get())) {
								minDate.set(entry.getTime().minusSeconds(1));
							}
							if (entry.getTime().isAfter(maxDate.get())) {
								maxDate.set(entry.getTime().plusSeconds(1));
							}
						});
				}
			}
		});

	}

	public List<ServerLogEntry> applyFilter(Predicate<ServerLogEntry> filter) {
		return entries.stream().filter(filter).collect(Collectors.toList());
	}

	public ObservableList<ServerLogEntry> entries() {
		return entries;
	}

	public ReadOnlyObjectProperty<LocalDateTime> minDate() {
		return minDate;
	}

	public ReadOnlyObjectProperty<LocalDateTime> maxDate() {
		return maxDate;
	}

}
