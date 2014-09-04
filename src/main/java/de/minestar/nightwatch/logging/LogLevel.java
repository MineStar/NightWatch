package de.minestar.nightwatch.logging;

import java.util.HashMap;
import java.util.Map;

public enum LogLevel {

    ALL("Deaktiviert"), FINEST("Am feinsten"), FINER("Feiner"), FINE("Fein"), CONFIG("Konfiguration"), INFO("Information"), WARNING("Warnung", "WARN"), SEVERE("Schwerwiegend");

    private String[] alternatives;

    private LogLevel(String... alternatives) {
        this.alternatives = alternatives;
    }

    private static Map<String, LogLevel> mapByName;

    static {
        mapByName = new HashMap<>();
        LogLevel[] levels = LogLevel.values();
        for (int i = 0; i < levels.length; i++) {
            LogLevel logLevel = levels[i];
            mapByName.put(logLevel.name(), logLevel);
            for (int j = 0; j < logLevel.alternatives.length; j++) {
                String alternative = logLevel.alternatives[j];
                mapByName.put(alternative, logLevel);
            }
        }
    }

    public static LogLevel getByName(String name) {
        LogLevel l = mapByName.get(name);
        if (l == null)
            System.out.println(name);
        return l;
    }
}
