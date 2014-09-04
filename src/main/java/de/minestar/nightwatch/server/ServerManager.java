package de.minestar.nightwatch.server;

import java.io.File;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import com.fasterxml.jackson.core.type.TypeReference;

import de.minestar.nightwatch.core.Core;

public class ServerManager {

    private ObservableMap<String, ObservedServer> registeredServers;

    public static final TypeReference<Map<String, ObservedServer>> SERVER_TYPE = new TypeReference<Map<String, ObservedServer>>() {
    };

    public ServerManager(File serversFile) {
        registeredServers = loadServers(serversFile);
        registeredServers.addListener((MapChangeListener<String, ObservedServer>) c -> {
            saveServers(serversFile, registeredServers);
        });
    }

    private ObservableMap<String, ObservedServer> loadServers(File file) {
        ObservableMap<String, ObservedServer> result = FXCollections.observableHashMap();
        if (!file.exists()) {
            return result;
        }

        try {
            Map<String, ObservedServer> list = Core.JSON_MAPPER.readValue(file, SERVER_TYPE);
            return FXCollections.observableMap(list);
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.observableHashMap();
        }
    }

    private void saveServers(File file, Map<String, ObservedServer> servers) {
        try {
            Core.JSON_MAPPER.writerWithType(SERVER_TYPE).writeValue(file, servers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public ObservableMap<String, ObservedServer> registeredServers() {
        return registeredServers;
    }

}
