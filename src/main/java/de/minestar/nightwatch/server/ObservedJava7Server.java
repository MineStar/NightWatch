package de.minestar.nightwatch.server;

import java.io.File;
import java.util.List;

public class ObservedJava7Server extends ObservedServer {

    // TODO: Store the path in central config instead of every instance of java7
    // servers
    private File java7File;
    private String permGenSize;

    protected ObservedJava7Server() {
        // For serialization
    }

    public ObservedJava7Server(String name, File serverFile, String minMemory, String maxMemory, File java7File, String permGenSize) {
        super(name, serverFile, minMemory, maxMemory);
        this.java7File = java7File;
        this.permGenSize = permGenSize;
    }

    public File getJava7File() {
        return java7File;
    }

    public String getPermGenSize() {
        return permGenSize;
    }

    @Override
    protected void buildCommands(List<String> processCommands) {
        super.buildCommands(processCommands);
        processCommands.set(0, getJava7File().getAbsolutePath());
        processCommands.add(1, "-XX:MaxPermSize=" + getPermGenSize());
    }

}
