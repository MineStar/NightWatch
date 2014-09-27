package de.minestar.nightwatch.core;

import java.io.File;

import javafx.application.Application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

import de.minestar.nightwatch.gui.MainGUI;
import de.minestar.nightwatch.server.ServerManager;

/**
 * The main class of the application.
 */
public class Core {

    /**
     * The parser and writer for JSON
     */
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    static {
        // Configure the JSON mapper
        JSON_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JSON_MAPPER.registerModule(new JSR310Module());
    }

    /**
     * Logger for NightWatch project using Log4J2 from Apache
     */
    public static final Logger logger = LogManager.getLogger("NightWatch");

    private static final File SERVER_LIST_FILE = new File("servers.json");
    /**
     * Hold all registered servers and responsible for persisting changes
     */
    public static final ServerManager serverManager;

    private static final File MAIN_CONFIG_FILE = new File("mainConfig.json");

    /**
     * The configuration for NightWatch application containing all global properties. Changing an attribute of it will write the configuration to
     * disc.
     */
    public static Configuration mainConfig;

    static {
        // Initialize all final objects
        serverManager = new ServerManager(SERVER_LIST_FILE);
        try {
            mainConfig = Configuration.create(MAIN_CONFIG_FILE);
        } catch (Exception e) {
            logger.error("Can't load configuration", e);
        }
    }

    public static void main(String[] args) {
        logger.info("Starting application");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.info("Stopping application")));
        Application.launch(MainGUI.class, args);
    }

}
