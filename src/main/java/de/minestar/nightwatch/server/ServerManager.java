package de.minestar.nightwatch.server;

import java.io.File;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import com.fasterxml.jackson.core.type.TypeReference;

import de.minestar.nightwatch.core.Core;

/**
 * Responsible to loading and saving the observed servers. Provides an public observable list to react to changes
 */
public class ServerManager {

    private ObservableMap<String, ObservedMinecraftServer> registeredServers;

    // Information for jackson parser to read and write the map directly instead wrapped in a single class
    public static final TypeReference<Map<String, ObservedMinecraftServer>> SERVER_TYPE = new TypeReference<Map<String, ObservedMinecraftServer>>() {
    };

    /**
     * Construct a server manager by parsing the file. If the file does not exists or is empty, the server manager is initially empty
     * 
     * @param serversFile
     *            The file to parse
     */
    public ServerManager(File serversFile) {
        registeredServers = loadServers(serversFile);
        // React to server changes and persist them
        registeredServers.addListener((MapChangeListener<String, ObservedMinecraftServer>) c -> {
            saveServers(serversFile, registeredServers);
        });
    }

    private ObservableMap<String, ObservedMinecraftServer> loadServers(File file) {
        if (!file.exists() || file.length() == 0L) {
            return FXCollections.observableHashMap();
        }

        try {
            Map<String, ObservedMinecraftServer> server = Core.JSON_MAPPER.readValue(file, SERVER_TYPE);
            return FXCollections.observableMap(server);
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.observableHashMap();
        }
    }

    private void saveServers(File file, Map<String, ObservedMinecraftServer> servers) {
        try {
            Core.JSON_MAPPER.writerWithType(SERVER_TYPE).writeValue(file, servers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Currently observed servers. If the map change, the list will be written to the disc
     * 
     * @return The current observed servers
     */
    public ObservableMap<String, ObservedMinecraftServer> registeredServers() {
        return registeredServers;
    }

}
