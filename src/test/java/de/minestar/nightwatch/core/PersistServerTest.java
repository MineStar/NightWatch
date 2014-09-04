package de.minestar.nightwatch.core;

import java.io.File;
import java.io.IOException;

import javafx.collections.ObservableMap;

import org.junit.Assert;
import org.junit.Test;

import de.minestar.nightwatch.server.ObservedJava7Server;
import de.minestar.nightwatch.server.ObservedServer;
import de.minestar.nightwatch.server.ServerManager;

public class PersistServerTest {

    @Test
    public void saveAndLoadSerers() throws IOException {

        File tmpFile = new File("serverjsontest.json");
        ServerManager manager = new ServerManager(tmpFile);
        manager.registeredServers().put("java8server", new ObservedServer("Java8Server", new File("path/to/server"), "1024MB", "2G"));
        manager.registeredServers().put("java7server", new ObservedJava7Server("Java7Server", new File("path/to/other/server/"), "2G", "4G", new File("path/to/java7/binary"), "256MB"));

        manager = new ServerManager(tmpFile);
        ObservableMap<String, ObservedServer> registeredServers = manager.registeredServers();

        ObservedServer observedServerOne = registeredServers.get("java8server");
        Assert.assertTrue("not an instance of ObseredServer", observedServerOne instanceof ObservedServer);
        Assert.assertEquals("Java8Server", observedServerOne.getName());
        Assert.assertEquals(new File("path/to/server").getAbsolutePath(), observedServerOne.getServerFile().getAbsolutePath());
        Assert.assertEquals("1024MB", observedServerOne.getMinMemory());
        Assert.assertEquals("2G", observedServerOne.getMaxMemory());

        ObservedServer observedServerTwo = registeredServers.get("java7server");
        Assert.assertTrue("not an instance of ObseredJava7Server", observedServerTwo instanceof ObservedJava7Server);
        Assert.assertEquals("Java7Server", observedServerTwo.getName());
        Assert.assertEquals(new File("path/to/other/server").getAbsolutePath(), observedServerTwo.getServerFile().getAbsolutePath());
        Assert.assertEquals("2G", observedServerTwo.getMinMemory());
        Assert.assertEquals("4G", observedServerTwo.getMaxMemory());
        Assert.assertEquals(new File("path/to/java7/binary").getAbsolutePath(), ((ObservedJava7Server) observedServerTwo).getJava7File().getAbsolutePath());
        Assert.assertEquals("256MB", ((ObservedJava7Server) observedServerTwo).getPermGenSize());
        tmpFile.delete();

    }
}
