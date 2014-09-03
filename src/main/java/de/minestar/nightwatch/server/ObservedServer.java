package de.minestar.nightwatch.server;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ObservedServer {

    private String name;
    private String minMemory;
    private String maxMemory;
    private Optional<File> java7File = Optional.empty();
    private Optional<String> permGenSize = Optional.empty();

    private File directory;
    private File serverFile;

    public ObservedServer(String name, File serverFile, String minMemory, String maxMemory) {
        this.name = name;
        this.serverFile = serverFile;
        this.directory = serverFile.getParentFile();
        this.minMemory = minMemory;
        this.maxMemory = maxMemory;
    }

    public ObservedServer(String name, File serverFile, String minMemory, String maxMemory, File java7File, String permGenSize) {
        this(name, serverFile, minMemory, maxMemory);
        this.java7File = Optional.of(java7File);
        this.permGenSize = Optional.of(permGenSize);
    }

    public ProcessBuilder createProcess() throws Exception {

        List<String> commands = buildCommands();
        ProcessBuilder pBuilder = new ProcessBuilder(commands);
        pBuilder.directory(directory);
        pBuilder.redirectOutput(Redirect.PIPE);
        pBuilder.redirectInput(Redirect.PIPE);
        return pBuilder;
    }

    private List<String> buildCommands() {
        List<String> processCommands = new ArrayList<>();
        if (java7File.isPresent()) {
            processCommands.add(java7File.get().getAbsolutePath());
            processCommands.add("-XX:MaxPermSize=" + permGenSize.get());
        } else {
            processCommands.add("java");
        }
        processCommands.add("-Xms" + minMemory);
        processCommands.add("-Xmx" + maxMemory);
        processCommands.add("-jar");
        processCommands.add(serverFile.getAbsolutePath());
        processCommands.add("nogui");

        return processCommands;
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

    public Optional<File> getJava7File() {
        return java7File;
    }

    public Optional<String> getPermGenSize() {
        return permGenSize;
    }

    public File getDirectory() {
        return directory;
    }

    public File getServerFile() {
        return serverFile;
    }

    @Override
    public String toString() {
        return "ObservedServer [name=" + name + ", minMemory=" + minMemory + ", maxMemory=" + maxMemory + ", java7File=" + java7File + ", permGenSize=" + permGenSize + ", directory=" + directory + ", serverFile=" + serverFile + "]";
    }

}
