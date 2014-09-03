package de.minestar.nightwatch.server.parser;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.Test;

import de.minestar.nightwatch.core.ServerLogEntry;
import de.minestar.nightwatch.server.LogLevel;

public class ParserTest {

    @Test
    public void version17WithoutErrors() throws Exception {
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

    @Test(expected = ParseException.class)
    public void version17InvalidFormat() throws Exception {
        new Version1710Parser().parse(LocalDate.now(), "2014-07-21 20:43:48 [Information] [ForgeModLoader] Forge Mod Loader version 6.4.49.965 for Minecraft 1.6.4 loading");
    }

    @Test(expected = ParseException.class)
    public void version17WithException() throws Exception {

        LogEntryParser parser = new Version1710Parser();
        parser.parse(LocalDate.now(),"");

    }

}
