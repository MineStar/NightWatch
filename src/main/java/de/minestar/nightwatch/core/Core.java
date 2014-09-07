package de.minestar.nightwatch.core;

import java.io.File;

import javafx.application.Application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.minestar.nightwatch.gui.MainGUI;
import de.minestar.nightwatch.server.ServerManager;

public class Core {

	public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	static {
		JSON_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
		JSON_MAPPER.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private static final File SERVER_LIST_FILE = new File("servers.json");
	public static final ServerManager serverManager;

	private static final File MAIN_CONFIG_FILE = new File("mainConfig.json");
	public static Configuration mainConfig;

	public static int runningServers = 0;

	static {
		serverManager = new ServerManager(SERVER_LIST_FILE);
		try {
			mainConfig = Configuration.create(MAIN_CONFIG_FILE);
		} catch (Exception e) {
			System.err.println("Error while loading main config: ");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		Application.launch(MainGUI.class, args);
	}

}
