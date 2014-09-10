package de.minestar.nightwatch.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

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
        manager.registeredServers().put("java8server", new ObservedServer("Java8Server", new File("path/to/server"), "1024MB", "2G", true, false));
        manager.registeredServers().put("java7server", new ObservedJava7Server("Java7Server", new File("path/to/other/server/"), "2G", "4G", false, true, "256MB"));

        manager = new ServerManager(tmpFile);
        ObservableMap<String, ObservedServer> registeredServers = manager.registeredServers();

        ObservedServer observedServerOne = registeredServers.get("java8server");
        assertTrue("not an instance of ObseredServer", observedServerOne instanceof ObservedServer);
        assertEquals("Java8Server", observedServerOne.getName());
        assertEquals(new File("path/to/server").getAbsolutePath(), observedServerOne.getServerFile().getAbsolutePath());
        assertEquals("1024MB", observedServerOne.getMinMemory());
        assertEquals("2G", observedServerOne.getMaxMemory());
        assertTrue(observedServerOne.doAutomaticBackups());
        assertFalse(observedServerOne.doAutoRestarts());

        ObservedServer observedServerTwo = registeredServers.get("java7server");
        assertTrue("not an instance of ObseredJava7Server", observedServerTwo instanceof ObservedJava7Server);
        assertEquals("Java7Server", observedServerTwo.getName());
        assertEquals(new File("path/to/other/server").getAbsolutePath(), observedServerTwo.getServerFile().getAbsolutePath());
        assertEquals("2G", observedServerTwo.getMinMemory());
        assertEquals("4G", observedServerTwo.getMaxMemory());
        assertEquals("256MB", ((ObservedJava7Server) observedServerTwo).getPermGenSize());
        assertFalse(observedServerTwo.doAutomaticBackups());
        assertTrue(observedServerTwo.doAutoRestarts());
    }
}
