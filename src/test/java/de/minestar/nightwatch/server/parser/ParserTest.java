package de.minestar.nightwatch.server.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.Test;

import de.minestar.nightwatch.logging.LogLevel;
import de.minestar.nightwatch.logging.ServerLogEntry;
import de.minestar.nightwatch.logging.parser.Cauldron17ConsoleOutputParser;
import de.minestar.nightwatch.logging.parser.Forge16Parser;
import de.minestar.nightwatch.logging.parser.LogEntryParser;
import de.minestar.nightwatch.logging.parser.Version1710Parser;

public class ParserTest {

    @Test
    public void version17WithoutErrors() {
        LogEntryParser parser = new Version1710Parser();
        LocalDate today = LocalDate.now();

        ServerLogEntry entry = parser.parse(today, "[12:42:35] [Server thread/INFO]: Generating keypair");
        LocalTime logTime = LocalTime.of(12, 42, 35);
        assertEquals(logTime.atDate(today), entry.getTime());
        assertEquals("Server thread", entry.getSource());
        assertEquals(LogLevel.INFO, entry.getLogLevel());
        assertEquals("Generating keypair", entry.getText());

        entry = parser.parse(today, "[11:13:46] [Netty IO #2/INFO]: Client protocol version 1");
        logTime = LocalTime.of(11, 13, 46);
        assertEquals(logTime.atDate(today), entry.getTime());
        assertEquals("Netty IO #2", entry.getSource());
        assertEquals(LogLevel.INFO, entry.getLogLevel());
        assertEquals("Client protocol version 1", entry.getText());

        entry = parser.parse(today, "[11:15:07] [Server thread/INFO]: Meldanor[/192.168.1.33:62038] logged in with entity id 2666 at ([world] -88.5, 67.0, 174.5)");
        logTime = LocalTime.of(11, 15, 07);
        assertEquals(logTime.atDate(today), entry.getTime());
        assertEquals("Server thread", entry.getSource());
        assertEquals(LogLevel.INFO, entry.getLogLevel());
        assertEquals("Meldanor[/192.168.1.33:62038] logged in with entity id 2666 at ([world] -88.5, 67.0, 174.5)", entry.getText());

        entry = parser.parse(today, "[10:40:06] [Server thread/INFO]: ");
        logTime = LocalTime.of(10, 40, 06);
        assertEquals(logTime.atDate(today), entry.getTime());
        assertEquals("Server thread", entry.getSource());
        assertEquals(LogLevel.INFO, entry.getLogLevel());
        assertEquals("", entry.getText());
    }

    @Test
    public void version17InvalidFormat() {
        assertFalse(new Version1710Parser().accepts("2014-07-21 20:43:48 [Information] [ForgeModLoader] Forge Mod Loader version 6.4.49.965 for Minecraft 1.6.4 loading"));
    }

    @Test
    public void testForge16Parser() {
        LogEntryParser parser = new Forge16Parser();

        ServerLogEntry entry = parser.parse(LocalDate.now(), "2014-08-26 21:02:42 [INFO] Starting minecraft server version 1.6.4");
        LocalDateTime timeStamp = LocalDateTime.of(2014, 8, 26, 21, 02, 42);
        assertEquals(timeStamp, entry.getTime());
        assertEquals("Starting minecraft server version 1.6.4", entry.getText());
        assertEquals(LogLevel.INFO, entry.getLogLevel());
        assertEquals("Unknown", entry.getSource());

        entry = parser.parse(LocalDate.now(), "2014-08-26 21:03:19 [INFO] [Universal Electricity] Injected universalelectricity.core.asm.template.item.TemplateTEItem API into: resonantinduction/electrical/battery/ItemBlockBattery");
        timeStamp = LocalDateTime.of(2014, 8, 26, 21, 03, 19);
        assertEquals(timeStamp, entry.getTime());
        assertEquals("Injected universalelectricity.core.asm.template.item.TemplateTEItem API into: resonantinduction/electrical/battery/ItemBlockBattery", entry.getText());
        assertEquals(LogLevel.INFO, entry.getLogLevel());
        assertEquals("Universal Electricity", entry.getSource());
    }

    @Test
    public void testCauldronConsoleParser() {
        LogEntryParser parser = new Cauldron17ConsoleOutputParser();

        assertTrue(parser.accepts("[23:48:23 INFO]: Generating keypair"));

        ServerLogEntry entry = parser.parse(LocalDate.now(), "[23:48:23 INFO]: Generating keypair");
        LocalTime time = LocalTime.of(23, 48, 23);
        assertEquals(time.atDate(LocalDate.now()), entry.getTime());
        assertEquals("Generating keypair", entry.getText());
        assertEquals(LogLevel.INFO, entry.getLogLevel());
        assertEquals("Unknown", entry.getSource());

        entry = parser.parse(LocalDate.now(), "[23:48:23 WARN]: Offendor: org/spigotmc/RestartCommand.restart()V");
        time = LocalTime.of(23, 48, 23);
        assertEquals(time.atDate(LocalDate.now()), entry.getTime());
        assertEquals("Offendor: org/spigotmc/RestartCommand.restart()V", entry.getText());
        assertEquals(LogLevel.WARNING, entry.getLogLevel());
        assertEquals("Unknown", entry.getSource());
    }
}
