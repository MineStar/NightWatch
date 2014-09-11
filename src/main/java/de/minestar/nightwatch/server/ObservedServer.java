package de.minestar.nightwatch.server;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY, property = "class")
public class ObservedServer {

    private String name;
    private String minMemory;
    private String maxMemory;

    private File directory;
    private File serverFile;
    private boolean doAutomaticBackups;
    private boolean doAutoRestarts;

    private String vmOptions;

    protected ObservedServer() {
        // For serialization
    }

    public ObservedServer(String name, File serverFile, String minMemory, String maxMemory, String vmOptions, boolean automaticBackups, boolean doAutoRestarts) {
        this.name = name;
        this.serverFile = serverFile;
        this.directory = serverFile.getParentFile();
        this.minMemory = minMemory;
        this.maxMemory = maxMemory;
        this.vmOptions = vmOptions;
        this.doAutomaticBackups = automaticBackups;
        this.doAutoRestarts = doAutoRestarts;
    }

    public ProcessBuilder createProcess() throws Exception {

        List<String> commands = new ArrayList<>();
        buildCommands(commands);
        ProcessBuilder pBuilder = new ProcessBuilder(commands);
        pBuilder.directory(directory);
        pBuilder.redirectOutput(Redirect.PIPE);
        pBuilder.redirectInput(Redirect.PIPE);
        pBuilder.redirectErrorStream(true);
        return pBuilder;
    }

    protected void buildCommands(List<String> processCommands) {

        processCommands.add("java");
        processCommands.add("-Xms" + minMemory);
        processCommands.add("-Xmx" + maxMemory);
        processCommands.add(vmOptions);
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

    public String getVmOptions() {
        return vmOptions;
    }

    @JsonGetter
    public boolean doAutomaticBackups() {
        return doAutomaticBackups;
    }

    @JsonGetter
    public boolean doAutoRestarts() {
        return doAutoRestarts;
    }

    public void update(ObservedServer other) {
        this.name = other.name;
        this.minMemory = other.minMemory;
        this.maxMemory = other.maxMemory;
        this.serverFile = other.serverFile;
        this.directory = other.directory;
        this.doAutomaticBackups = other.doAutomaticBackups;
        this.doAutoRestarts = other.doAutoRestarts;
        this.vmOptions = other.vmOptions;
    }

    @Override
    public String toString() {
        return "ObservedServer [name=" + name + ", minMemory=" + minMemory + ", maxMemory=" + maxMemory + ", directory=" + directory + ", serverFile=" + serverFile + ", doAutomaticBackups=" + doAutomaticBackups + ", doAutoRestarts=" + doAutoRestarts + ", vmOptions=" + vmOptions + "]";
    }

}
