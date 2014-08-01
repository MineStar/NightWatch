package de.minestar.nightwatch.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServerSpawner {

    private StartParameters defaultParameter;

    public ServerSpawner() {
        this.defaultParameter = createDefaultParameter();
    }

    private StartParameters createDefaultParameter() {
        return new StartParameters().minMemoryMB(1024).maxMemoryMB(2048);
    }

    public Process spawnServer(File serverJar) throws Exception {
        return spawnServer(serverJar, defaultParameter);
    }

    public Process spawnServer(File serverJar, StartParameters parameter) throws Exception {
        List<String> commands = getCommands(serverJar.getAbsolutePath(), parameter);

        ProcessBuilder pBuilder = new ProcessBuilder(commands);
        pBuilder.directory(serverJar.getParentFile());

        return pBuilder.start();
    }

    private List<String> getCommands(String jarPath, StartParameters parameter) {
        List<String> commands = new ArrayList<String>();

        commands.add(parameter.minMemoryString());
        commands.add(parameter.maxMemoryString());
        commands.add(parameter.permSizeString());

        commands.add("-jar");
        commands.add(jarPath);

        commands.addAll(parameter.additionals());
        return commands;
    }
}
