package de.minestar.nightwatch.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import javafx.collections.ObservableMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.minestar.nightwatch.server.ObservedMinecraftServer;
import de.minestar.nightwatch.server.ObservedMinecraftServer.Builder;
import de.minestar.nightwatch.server.ServerManager;

public class PersistServerTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void saveAndLoadSerers() throws IOException {

        File tmpFile = tmpFolder.newFile();
        ServerManager manager = new ServerManager(tmpFile);
        List<LocalTime> restartTimes = Arrays.asList(LocalTime.of(12, 30), LocalTime.of(22, 30));
        List<Duration> restartWarnings = Arrays.asList(Duration.ofMinutes(30), Duration.ofMinutes(20));

        // Create server 1
        Builder server1Builder = ObservedMinecraftServer.create("Java8Server", new File("path/to/server"));
        server1Builder.minHeapSize("1024M").maxHeapSize("2G").otherVmOptions("-XX:+UseParNewGC");
        server1Builder.backupAfterShutdown(true).restartAfterShutdown(false);
        server1Builder.restartTimes(restartTimes).warningIntervals(restartWarnings);
        manager.registeredServers().put("java8server", server1Builder.build());

        // Create server 2
        Builder server2Builder = ObservedMinecraftServer.create("Java7Server", new File("path/to/other/server"));
        server2Builder.minHeapSize("4G").maxHeapSize("8G").otherVmOptions("-XX:+UseG1GC");
        server2Builder.backupAfterShutdown(false).restartAfterShutdown(true);
        server2Builder.restartTimes(restartTimes).warningIntervals(restartWarnings);
        server2Builder.useJava7(true).maxPermGen("256M");
        manager.registeredServers().put("java7server", server2Builder.build());

        // Load persisted server from file
        manager = new ServerManager(tmpFile);
        ObservableMap<String, ObservedMinecraftServer> registeredServers = manager.registeredServers();

        // Check, if the server are correctly parsed
        ObservedMinecraftServer observedServerOne = registeredServers.get("java8server");
        assertEquals("Java8Server", observedServerOne.getName());
        assertEquals(new File("path/to/server").getAbsolutePath(), observedServerOne.getServerFile().getAbsolutePath());
        assertEquals("1024M", observedServerOne.getMinHeapSize());
        assertEquals("2G", observedServerOne.getMaxHeapSize());
        assertFalse(observedServerOne.useJava7());
        assertTrue(observedServerOne.doBackupOnShutdown());
        assertFalse(observedServerOne.doRestartOnShutdown());
        assertEquals("-XX:+UseParNewGC", observedServerOne.getOtherVmOptions());
        assertTrue(observedServerOne.getRestartTimes().contains(LocalTime.of(12, 30)));
        assertTrue(observedServerOne.getRestartTimes().contains(LocalTime.of(22, 30)));

        ObservedMinecraftServer observedServerTwo = registeredServers.get("java7server");
        assertEquals("Java7Server", observedServerTwo.getName());
        assertEquals(new File("path/to/other/server").getAbsolutePath(), observedServerTwo.getServerFile().getAbsolutePath());
        assertEquals("4G", observedServerTwo.getMinHeapSize());
        assertEquals("8G", observedServerTwo.getMaxHeapSize());
        assertTrue(observedServerTwo.useJava7());
        assertEquals("256M", observedServerTwo.getMaxPermGen());
        assertFalse(observedServerTwo.doBackupOnShutdown());
        assertTrue(observedServerTwo.doRestartOnShutdown());
        assertEquals("-XX:+UseG1GC", observedServerTwo.getOtherVmOptions());
        assertTrue(observedServerTwo.getRestartTimes().contains(LocalTime.of(12, 30)));
        assertTrue(observedServerTwo.getRestartTimes().contains(LocalTime.of(22, 30)));
    }
}
