package de.minestar.nightwatch.core;

import java.io.File;
import java.lang.reflect.Field;

import com.fasterxml.jackson.annotation.JsonGetter;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class Configuration {

    public static Configuration create(File configFile) throws Exception {
        Configuration result;
        if (configFile.exists() && configFile.length() != 0L) {
            result = Core.JSON_MAPPER.readValue(configFile, Configuration.class);
        } else {
            result = new Configuration();
        }
        addChangeListenerToFields(configFile, result);
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addChangeListenerToFields(final File configFile, final Configuration config) throws Exception {
        // Use reflection to assign a change listener to all observable
        // properties. If the properties changes, the configuration is written
        // to the file.
        Field[] fields = config.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            Object object = field.get(config);
            if (object instanceof ObservableValue) {
                ((ObservableValue) object).addListener((observ, oldVal, newVal) -> {
                    try {
                        Core.JSON_MAPPER.writeValue(configFile, config);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private Configuration() {
        // Empty constructor for serialization
    }

    // ********************************
    // *** ADD HERE YOUR PROPERTIES ***
    // ********************************

    private SimpleStringProperty java7Path = new SimpleStringProperty();

    @JsonGetter
    public SimpleStringProperty java7Path() {
        return java7Path;
    }

}
