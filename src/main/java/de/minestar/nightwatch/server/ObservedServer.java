package de.minestar.nightwatch.server;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="class")
public class ObservedServer {

    private String name;
    private String minMemory;
    private String maxMemory;

    private File directory;
    private File serverFile;
    
    protected ObservedServer() {
        // For serialization
    }

    public ObservedServer(String name, File serverFile, String minMemory, String maxMemory) {
        this.name = name;
        this.serverFile = serverFile;
        this.directory = serverFile.getParentFile();
        this.minMemory = minMemory;
        this.maxMemory = maxMemory;
    }

    public ProcessBuilder createProcess() throws Exception {

        
        List<String> commands = new ArrayList<>();
        buildCommands(commands);
        ProcessBuilder pBuilder = new ProcessBuilder(commands);
        pBuilder.directory(directory);
        pBuilder.redirectOutput(Redirect.PIPE);
        pBuilder.redirectInput(Redirect.PIPE);
        return pBuilder;
    }

    protected void buildCommands(List<String> processCommands) {

        processCommands.add("java");
        processCommands.add("-Xms" + minMemory);
        processCommands.add("-Xmx" + maxMemory);
        processCommands.add("-jar");
        processCommands.add(serverFile.getAbsolutePath());
        processCommands.add("nogui");
    }

    public String getName() {
        return name;
    }

    public String getMinMemory() {
        return minMemory;
    }

    public String getMaxMemory() {
        return maxMemory;
    }

    public File getDirectory() {
        return directory;
    }

    public File getServerFile() {
        return serverFile;
    }

    @Override
    public String toString() {
        return "ObservedServer [name=" + name + ", minMemory=" + minMemory + ", maxMemory=" + maxMemory + ", directory=" + directory + ", serverFile=" + serverFile + "]";
    }

}