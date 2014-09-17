package de.minestar.nightwatch.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import javafx.collections.ObservableMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.minestar.nightwatch.server.ObservedJava7Server;
import de.minestar.nightwatch.server.ObservedServer;
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

        manager.registeredServers().put("java8server", new ObservedServer("Java8Server", new File("path/to/server"), "1024MB", "2G", "-XX:+UseParNewGC", true, false, restartTimes, restartWarnings));
        manager.registeredServers().put("java7server", new ObservedJava7Server("Java7Server", new File("path/to/other/server/"), "4G", "8G", "-XX:+UseG1GC", false, true, restartTimes, restartWarnings, "256MB"));

        manager = new ServerManager(tmpFile);
        ObservableMap<String, ObservedServer> registeredServers = manager.registeredServers();

        ObservedServer observedServerOne = registeredServers.get("java8server");
        assertTrue("not an instance of ObseredServer", observedServerOne instanceof ObservedServer);
        assertEquals("Java8Server", observedServerOne.getName());
        assertEquals(new File("path/to/server").getAbsolutePath(), observedServerOne.getServerFile().getAbsolutePath());
        assertEquals("1024MB", observedServerOne.getMinMemory());
        assertEquals("2G", observedServerOne.getMaxMemory());
        assertTrue(observedServerOne.doAutoBackupOnShutdown());
        assertFalse(observedServerOne.doAutoRestartOnShutdown());
        assertEquals("-XX:+UseParNewGC", observedServerOne.getVmOptions());
        assertTrue(observedServerOne.getRestartTimes().contains(LocalTime.of(12, 30)));
        assertTrue(observedServerOne.getRestartTimes().contains(LocalTime.of(22, 30)));

        ObservedServer observedServerTwo = registeredServers.get("java7server");
        assertTrue("not an instance of ObseredJava7Server", observedServerTwo instanceof ObservedJava7Server);
        assertEquals("Java7Server", observedServerTwo.getName());
        assertEquals(new File("path/to/other/server").getAbsolutePath(), observedServerTwo.getServerFile().getAbsolutePath());
        assertEquals("4G", observedServerTwo.getMinMemory());
        assertEquals("8G", observedServerTwo.getMaxMemory());
        assertEquals("256MB", ((ObservedJava7Server) observedServerTwo).getPermGenSize());
        assertFalse(observedServerTwo.doAutoBackupOnShutdown());
        assertTrue(observedServerTwo.doAutoRestartOnShutdown());
        assertEquals("-XX:+UseG1GC", observedServerTwo.getVmOptions());
        assertTrue(observedServerTwo.getRestartTimes().contains(LocalTime.of(12, 30)));
        assertTrue(observedServerTwo.getRestartTimes().contains(LocalTime.of(22, 30)));

        Files.readAllLines(tmpFile.toPath()).forEach(s -> System.out.println(s));
    }
}
