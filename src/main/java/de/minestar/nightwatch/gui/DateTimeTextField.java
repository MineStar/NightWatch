package de.minestar.nightwatch.gui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;

public class DateTimeTextField extends TextField {

    private ObjectProperty<LocalDateTime> dateTimeProperty;

    public DateTimeTextField() {
        this(DateTimeFormatter.BASIC_ISO_DATE);
    }

    public DateTimeTextField(LocalDateTime initialDate) {
        this(initialDate, DateTimeFormatter.BASIC_ISO_DATE);
    }

    public DateTimeTextField(DateTimeFormatter format) {
        this(LocalDateTime.now(), format);

    }

    public DateTimeTextField(LocalDateTime initialDate, DateTimeFormatter format) {
        this.setText(initialDate.format(format));
        this.dateTimeProperty = new SimpleObjectProperty<>(initialDate);
        
        textProperty().addListener((observ, oldValue, newValue) -> {
            try {
                dateTimeProperty.set(LocalDateTime.parse(getText(), format));
            } catch (DateTimeParseException e) {
                textProperty().set(oldValue);
                this.getStyleClass().add("date-textfield-error");
                // Remove the red background after 2 seconds
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> getStyleClass().remove("date-textfield-error"));
                        t.cancel();
                    }
                }, 2000);
            }
        });
    }

    public ObjectProperty<LocalDateTime> dateTimeProperty() {
        return dateTimeProperty;
    }

}
