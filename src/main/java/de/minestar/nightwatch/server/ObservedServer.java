package de.minestar.nightwatch.server;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import de.minestar.nightwatch.gui.dialog.EditServerDialog;
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY, property = "class")
public class ObservedServer {

    private String name;

    private File directory;
    private File serverFile;

    @JsonProperty
    private boolean autoBackupOnShutdown;
    @JsonProperty
    private boolean autoRestartOnShutdown;
    
    private String minMemory;
    private String maxMemory;
    private String vmOptions;

    private List<LocalTime> restartTimes;
    private List<Duration> warningIntervals;

    protected ObservedServer() {
        // For serialization
    }

    /**
     * Creates an Observed Server holding all necessary information to start and control the server.
     * 
     * @param name
     *            The unique name of the server.
     * @param serverFile
     *            The executable file to start the server, for example minecraft-server.jar.
     * @param minMemory
     *            The minimum heap memory for the server.
     * @param maxMemory
     *            The maximum heap memory for the server.
     * @param vmOptions
     *            Additional options not covered by the program, for example the type of garbage collector to use.
     * @param autoBackupOnShutdown
     *            If set to <code>true</code> it will automatically do backups of the server after the shutdown.
     * @param autoRestartOnShutdown
     *            If set to <code>true</code> it will automatically restart after a shutdown.
     */
    public ObservedServer(String name, File serverFile, String minMemory, String maxMemory, String vmOptions, boolean autoBackupOnShutdown, boolean autoRestartOnShutdown, List<LocalTime> restartTimes, List<Duration> warningIntervals) {
        this.name = name;
        this.serverFile = serverFile;
        this.directory = serverFile.getParentFile();
        this.minMemory = minMemory;
        this.maxMemory = maxMemory;
        this.vmOptions = vmOptions;
        this.autoBackupOnShutdown = autoBackupOnShutdown;
        this.autoRestartOnShutdown = autoRestartOnShutdown;
        this.restartTimes = restartTimes;
        this.warningIntervals = warningIntervals;
    }

    /**
     * Create a {@link ProcessBuilder} and fill it with start parameters to start this server. Also redirects the output of the server
     * 
     * @return {@link ProcessBuilder} containing all start parameter of this server
     * @throws Exception
     */
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

    /**
     * Add the start parameters to the list. The order is important, otherwise the server will not start and run correctly. Can be overridden by
     * inherited classes to add/change/remove parameters
     * 
     * @param processCommands
     *            The list to fill
     */
    protected void buildCommands(List<String> processCommands) {

        processCommands.add("java");
        processCommands.add("-Xms" + minMemory);
        processCommands.add("-Xmx" + maxMemory);
        processCommands.add(vmOptions);
        processCommands.add("-jar");
        processCommands.add(serverFile.getAbsolutePath());
        processCommands.add("nogui");
    }

    /**
     * @return The unique name of the server
     */
    public String getName() {
        return name;
    }

    /**
     * @return The minimum memory amount for the java heap the server needs.
     */
    public String getMinMemory() {
        return minMemory;
    }

    /**
     * @return The maximum memory amount for the java heap the server needs.
     */
    public String getMaxMemory() {
        return maxMemory;
    }

    /**
     * @return The server's directory containing all server files like the world or the logs. Will be calculated using {@link #getServerFile()}s
     *         parent
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * @return The executable server file
     */
    public File getServerFile() {
        return serverFile;
    }

    /**
     * @return Plain string with additional, not directly by the program covered, vm parameters
     */
    public String getVmOptions() {
        return vmOptions;
    }

    /**
     * @return If <code>true</code>, NightWatch will create a backup of the server after server shutdown.
     */
    public boolean doAutoBackupOnShutdown() {
        return autoBackupOnShutdown;
    }

    /**
     * @return If <code>true</code>, NightWatch will restart the server after server shutdown.
     */
    public boolean doAutoRestartOnShutdown() {
        return autoRestartOnShutdown;
    }

    /**
     * @return If <code>true</code>, the server will be stopped and restarted by NightWatch at the {@link #getRestartTimes()}
     * 
     */
    public boolean doAutoRestarts() {
        return restartTimes != null && !restartTimes.isEmpty();
    }

    /**
     * @return The times on the current day (or next, if the time is in the past), where is server will be stopped by NightWatch and restarted
     */
    public List<LocalTime> getRestartTimes() {
        return restartTimes;
    }

    /**
     * @return The intervals before the auto restarts to inform the people, when the next auto restart is.
     */
    public List<Duration> getWarningIntervals() {
        return warningIntervals;
    }

    /**
     * Copy all values of the other server to this server. This is by the {@link EditServerDialog} to set and change a server configuration
     * 
     * @param other
     *            The other instance holding the updated values.
     */
    public void update(ObservedServer other) {
        this.name = other.name;
        this.minMemory = other.minMemory;
        this.maxMemory = other.maxMemory;
        this.serverFile = other.serverFile;
        this.directory = other.directory;
        this.autoBackupOnShutdown = other.autoBackupOnShutdown;
        this.autoRestartOnShutdown = other.autoRestartOnShutdown;
        this.vmOptions = other.vmOptions;
        this.restartTimes = other.restartTimes;
        this.warningIntervals = other.warningIntervals;
    }

    @Override
    public String toString() {
        return "ObservedServer [name=" + name + ", minMemory=" + minMemory + ", maxMemory=" + maxMemory + ", directory=" + directory + ", serverFile=" + serverFile + ", autoBackupOnShutdown=" + autoBackupOnShutdown + ", autoRestartOnShutdown=" + autoRestartOnShutdown + ", vmOptions=" + vmOptions + "]";
    }

}
