package de.minestar.nightwatch.core;

import java.io.File;

import javafx.application.Application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.minestar.nightwatch.gui.MainGUI;
import de.minestar.nightwatch.server.ServerManager;

public class Core {

    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    static {
        JSON_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static final ServerManager serverManager;
    private static final File SERVER_LIST_FILE = new File("servers.json");
    static {

        serverManager = new ServerManager(SERVER_LIST_FILE);
    }

    public static void main(String[] args) {

        Application.launch(MainGUI.class, args);
    }

}
