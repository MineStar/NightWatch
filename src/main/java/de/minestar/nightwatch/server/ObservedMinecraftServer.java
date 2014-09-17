package de.minestar.nightwatch.server;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.minestar.nightwatch.core.Core;

/**
 * Data class holding all necessary information to observe and start a minecraft server.
 * <p>
 * This class uses the Builder pattern. To start constructing a server, use
 * </p>
 */
public class ObservedMinecraftServer {

    private String name;
    private File serverFile;

    @JsonProperty
    private boolean doBackupAfterShutdown;
    @JsonProperty
    private boolean doRestartAfterShutdown;

    private String minHeapSize;
    private String maxHeapSize;
    private String otherVmOptions;

    @JsonProperty
    private boolean useJava7;
    private String maxPermGen;

    private List<LocalTime> restartTimes;
    private List<Duration> warningIntervals;

    private ObservedMinecraftServer() {
        // Empty constructor for serialization
    }

    // ***************
    // BUILDER PATTERN
    // ***************

    private ObservedMinecraftServer(Builder builder) {
        // Construct an instance using the builds attributes
        this.name = builder.name;
        this.serverFile = builder.serverFile;

        this.doBackupAfterShutdown = builder.doBackupAfterShutdown;
        this.doRestartAfterShutdown = builder.doRestartAfterShutdown;
        this.minHeapSize = builder.minHeapSize;
        this.maxHeapSize = builder.maxHeapSize;
        this.otherVmOptions = builder.otherVmOptions;
        this.useJava7 = builder.useJava7;
        this.maxPermGen = builder.maxPermGen;
        this.restartTimes = builder.restartTimes;
        this.warningIntervals = builder.warningIntervals;
    }

    public static class Builder {

        // Necessary parameters
        private final String name;
        private final File serverFile;

        // Optional parameters
        private boolean doBackupAfterShutdown = false;
        private boolean doRestartAfterShutdown = false;

        private String minHeapSize = "1G";
        private String maxHeapSize = "1G";
        private String otherVmOptions = "";

        private boolean useJava7 = false;
        private String maxPermGen = "128MB";

        private List<LocalTime> restartTimes = Collections.emptyList();
        private List<Duration> warningIntervals = Collections.emptyList();

        private Builder(final String name, final File serverFile) {
            this.serverFile = serverFile;
            this.name = name;
        }

        /**
         * If <code>true</code>, the server will do a backup after its shutdown.
         * 
         * @param enable
         *            Enable or disable feature.
         * @return This builder
         */
        public Builder backupAfterShutdown(boolean enable) {
            this.doBackupAfterShutdown = enable;
            return this;
        }

        /**
         * If <code>true</code>, the server will initiate a restart after its shutdown.
         * 
         * @param enable
         *            Enable or disable feature.
         * @return This builder
         */
        public Builder restartAfterShutdown(boolean enable) {
            this.doRestartAfterShutdown = enable;
            return this;
        }

        /**
         * Set the size for the vm option -Xms. Higher values will reserve more RAM at start for the server.
         * 
         * @param size
         *            Size parameter, using a format like '4K','5M' or '6G'
         * @return This builder
         */
        public Builder minHeapSize(String size) {
            this.minHeapSize = size;
            return this;
        }

        /**
         * Set the size for the vm option -Xmx. Higher values will reserve more RAM at start for the server.
         * 
         * @param size
         *            Size parameter, using a format like '4K','5M' or '6G'
         * @return This builder
         */
        public Builder maxHeapSize(String size) {
            this.maxHeapSize = size;
            return this;
        }

        /**
         * Additional options and parameters for the VM like type of garbage collector.
         * 
         * @param options
         *            One string, parameters should be separated by whitespace.
         * @return This builder
         */
        public Builder otherVmOptions(String options) {
            this.otherVmOptions = options;
            return this;
        }

        /**
         * Use a Java7 VM instead of the current > 8 java version to start the server. This is important for forge servers of minecraft version 1.7 or
         * lower.
         * 
         * @param Enable
         *            or disable feature.
         * @return This builder
         */
        public Builder useJava7(boolean enable) {
            this.useJava7 = enable;
            return this;
        }

        /**
         * Java7 and highly modificated servers with many classes use a higher amount of perm gen size. Will have no affect, if
         * {@link #useJava7(boolean)} was never enabled(because java 8 doesn't have perm gen anymore).
         * 
         * @param size
         *            Size parameter, using a format like '4K','5M' or '6G'
         * @return This builder
         */
        public Builder maxPermGen(String size) {
            this.maxPermGen = size;
            return this;
        }

        /**
         * If there is at least one element in the list, the server will automatically shutdown at the given times and restart. <br>
         * At default, this is disabled.
         * 
         * @param restartTimes
         *            A list of times of the day, when the server should restart.
         * @return This builder
         */
        public Builder restartTimes(List<LocalTime> restartTimes) {
            this.restartTimes = new ArrayList<>(restartTimes);
            return this;
        }

        /**
         * Intervals before a restart, when the server informs the player about an upcoming restart. If there is no non-empty list given at
         * {@link #restartTimes(List)}, the attributes will be ignored.
         * 
         * @param warningIntervals
         *            A list of intervals, when the server informs the player.
         * @return This builder
         */
        public Builder warningIntervals(List<Duration> warningIntervals) {
            this.warningIntervals = new ArrayList<>(warningIntervals);
            return this;
        }

        /**
         * @return Creates a new instance of {@link ObservedMinecraftServer} with the attributes set at the building process. <br>
         *         Creates always a new instance, so you can reuse the builder to build more than one server
         */
        public ObservedMinecraftServer build() {
            return new ObservedMinecraftServer(this);
        }

    }

    /**
     * Start building process of an {@link ObservedMinecraftServer} using a builder. To finalize the process, use {@link Builder#build()}
     * 
     * @param name
     *            The unique name of the server
     * @param serverExecutable
     *            The path to the server executable, like the jar or exe.
     * @return A Builder instance based of the builder pattern
     */
    public static Builder create(final String name, final File serverExecutable) {
        return new Builder(name, serverExecutable);
    }

    // *************
    // Class content
    // *************

    /**
     * Create a {@link ProcessBuilder} and fill it with start parameters to start this server. Also redirects the output of the server
     * 
     * @return {@link ProcessBuilder} containing all start parameter of this server
     * @throws Exception
     */
    public ProcessBuilder createProcess() throws Exception {
        ProcessBuilder pBuilder = new ProcessBuilder(buildProcessParameter());
        pBuilder.directory(this.serverFile.getParentFile());
        pBuilder.redirectOutput(Redirect.PIPE);
        pBuilder.redirectInput(Redirect.PIPE);
        pBuilder.redirectErrorStream(true);
        return pBuilder;
    }

    private static final Pattern SPLIT_PATTERN = Pattern.compile(" ");

    /**
     * Helper method to add all process parameter to a list
     * 
     * @return List of all process parameter
     */
    private List<String> buildProcessParameter() {
        List<String> parameter = new ArrayList<>();

        if (this.useJava7) {
            parameter.add(Core.mainConfig.java7Path().get());
            parameter.add("-XX:MaxPermSize=" + this.maxPermGen);
        } else {
            parameter.add("java");
        }
        parameter.add("-Xms" + this.minHeapSize);
        parameter.add("-Xmx" + this.maxHeapSize);

        if (!otherVmOptions.isEmpty()) {
            // Add all options containing in otherVmOptions and separated by a whitespace to the list
            SPLIT_PATTERN.splitAsStream(otherVmOptions).forEach(s -> parameter.add(s));
        }

        parameter.add("-jar");
        parameter.add(serverFile.getAbsolutePath());
        parameter.add("nogui");

        return parameter;
    }

    /**
     * @return The unique name of the server
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return The executable server file
     */
    public File getServerFile() {
        return this.serverFile;
    }

    /**
     * @return The server's directory containing all server files like the world or the logs. Will be calculated using {@link #getServerFile()}s
     *         parent
     */
    public File getDirectory() {
        return getServerFile().getParentFile();
    }

    /**
     * @return The minimum memory amount for the java heap the server needs.
     */
    public String getMinHeapSize() {
        return this.minHeapSize;
    }

    /**
     * @return The maximum memory amount for the java heap the server needs.
     */
    public String getMaxHeapSize() {
        return this.maxHeapSize;
    }
    /**
     * @return Plain string with additional, not directly by the program covered, vm parameters
     */
    public String getOtherVmOptions() {
        return this.otherVmOptions;
    }

    /**
     * @return If <code>true</code>, the server will use another VM instead of this VM to start the server.
     */
    public boolean useJava7() {
        return this.useJava7;
    }

    /**
     * @return The amount of memory for perm gen. If {@link #useJava7()} returns <code>false</code>, the perm gen will not used at server start.
     */
    public String getMaxPermGen() {
        return this.maxPermGen;
    }

    /**
     * @return If <code>true</code>, NightWatch will create a backup of the server after server shutdown.
     */
    public boolean doBackupOnShutdown() {
        return this.doBackupAfterShutdown;
    }

    /**
     * @return If <code>true</code>, NightWatch will restart the server after server shutdown.
     */
    public boolean doRestartOnShutdown() {
        return this.doRestartAfterShutdown;
    }

    /**
     * @return If <code>true</code>, the server will be stopped and restarted by NightWatch at the {@link #getRestartTimes()}
     * 
     */
    public boolean doAutoRestarts() {
        return this.restartTimes != null && !this.restartTimes.isEmpty();
    }

    /**
     * @return The times on the current day (or next, if the time is in the past), where is server will be stopped by NightWatch and restarted
     */
    public List<LocalTime> getRestartTimes() {
        return new ArrayList<>(this.restartTimes);
    }

    /**
     * @return The intervals before the auto restarts to inform the people, when the next auto restart is.
     */
    public List<Duration> getWarningIntervals() {
        return new ArrayList<>(this.warningIntervals);
    }

    public void update(ObservedMinecraftServer other) {
        this.name = other.name;
        this.minHeapSize = other.minHeapSize;
        this.maxHeapSize = other.maxHeapSize;
        this.useJava7 = other.useJava7;
        this.maxPermGen = other.maxPermGen;
        this.serverFile = other.serverFile;
        this.doBackupAfterShutdown = other.doBackupAfterShutdown;
        this.doRestartAfterShutdown = other.doRestartAfterShutdown;
        this.otherVmOptions = other.otherVmOptions;
        this.restartTimes = other.restartTimes;
        this.warningIntervals = other.warningIntervals;
    }

    @Override
    public String toString() {
        return "ObservedMinecraftServer [name=" + name + ", serverFile=" + serverFile + ", doBackupAfterShutdown=" + doBackupAfterShutdown + ", doRestartAfterShutdown=" + doRestartAfterShutdown + ", minHeapSize=" + minHeapSize + ", maxHeapSize=" + maxHeapSize + ", otherVmOptions=" + otherVmOptions + ", useJava7=" + useJava7 + ", maxPermGen=" + maxPermGen + ", restartTimes=" + restartTimes + ", warningIntervals=" + warningIntervals + "]";
    }

}
