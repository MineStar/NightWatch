package de.minestar.nightwatch.server;

import java.io.File;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import de.minestar.nightwatch.core.Core;

/**
 * Extended class of an {@link ObservedServer} using java 7 to start. Old versions of minecraft servers like forge have problems to start with java 8
 * or newer. This class uses a java 7 version to start and provides the possibility to specify the perm gen size (which is obsolete in java 8)
 */
public class ObservedJava7Server extends ObservedServer {

    private String permGenSize;

    protected ObservedJava7Server() {
        // For serialization
    }

    public ObservedJava7Server(String name, File serverFile, String minMemory, String maxMemory, String vmOptions, boolean autoBackupOnShutdown, boolean autoRestartOnShutdown, List<LocalTime> restartTimes, List<Duration> warningIntervals, String permGenSize) {
        super(name, serverFile, minMemory, maxMemory, vmOptions, autoBackupOnShutdown, autoRestartOnShutdown, restartTimes, warningIntervals);
        this.permGenSize = permGenSize;
    }

    public String getPermGenSize() {
        return permGenSize;
    }

    @Override
    public void update(ObservedServer other) {
        if (!(other instanceof ObservedJava7Server))
            return;

        super.update(other);
        this.permGenSize = ((ObservedJava7Server) other).permGenSize;
    }

    @Override
    protected void buildCommands(List<String> processCommands) {
        super.buildCommands(processCommands);
        // Replace "java" with the path to java binary
        processCommands.set(0, Core.mainConfig.java7Path().get());
        processCommands.add(1, "-XX:MaxPermSize=" + getPermGenSize());
    }

}
