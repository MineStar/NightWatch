package de.minestar.nightwatch.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.minestar.nightwatch.threading.AutoRestartTask;
import de.minestar.nightwatch.util.DurationUtil;
import de.saxsys.javafx.test.JfxRunner;

/**
 * Simple unit test to see how the data api is working
 */
@RunWith(JfxRunner.class)
public class RestartTimerTest {

    @Test
    public void sameDayTest() {
        LocalTime restartTime = LocalTime.of(14, 30);
        Duration warningTime1 = Duration.ofMinutes(30);
        Duration warningTime2 = Duration.ofMinutes(15);
        Duration warningTime3 = Duration.ofMinutes(5);
        Duration warningTime4 = Duration.ofMinutes(1);
        Duration warningTime5 = Duration.ofSeconds(10);

        assertEquals(LocalTime.of(14, 00), restartTime.minus(warningTime1));
        assertEquals(LocalTime.of(14, 15), restartTime.minus(warningTime2));
        assertEquals(LocalTime.of(14, 25), restartTime.minus(warningTime3));
        assertEquals(LocalTime.of(14, 29), restartTime.minus(warningTime4));
        assertEquals(LocalTime.of(14, 29, 50), restartTime.minus(warningTime5));
    }

    @Test
    public void differentDayTest() {
        LocalTime restartTime = LocalTime.of(00, 00);

        Duration warningTime1 = Duration.ofMinutes(30);
        Duration warningTime2 = Duration.ofMinutes(15);
        Duration warningTime3 = Duration.ofMinutes(5);
        Duration warningTime4 = Duration.ofMinutes(1);
        Duration warningTime5 = Duration.ofSeconds(10);

        assertEquals(LocalTime.of(23, 30), restartTime.minus(warningTime1));
        assertEquals(LocalTime.of(23, 45), restartTime.minus(warningTime2));
        assertEquals(LocalTime.of(23, 55), restartTime.minus(warningTime3));
        assertEquals(LocalTime.of(23, 59), restartTime.minus(warningTime4));
        assertEquals(LocalTime.of(23, 59, 50), restartTime.minus(warningTime5));
    }

    @Test
    public void durationToReadalbeStringTest() {
        Duration duration = Duration.ofHours(1).plusMinutes(30);
        String result = DurationUtil.format(duration);
        assertEquals("1h 30m", result);
    }

    @Test
    public void durationSortTest() {
        List<Duration> warningTimes = Arrays.asList(Duration.ofHours(10), Duration.ofMinutes(30), Duration.ofSeconds(30), Duration.ofHours(2));
        warningTimes.sort(Comparator.reverseOrder());
    }

    @Test(timeout = 20000L)
    public void autoRestartTaskTest() throws InterruptedException {
        LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();
        LocalDateTime restartTime = LocalDateTime.now().plusSeconds(15);
        List<Duration> restartWarnings = Arrays.asList(Duration.ofSeconds(10), Duration.ofSeconds(5), Duration.ofSeconds(3), Duration.ofSeconds(1));
        AutoRestartTask task = new AutoRestartTask(restartTime, restartWarnings, commandQueue);

        Thread restartThread = new Thread(task);
        restartThread.start();
        restartThread.join();

        assertEquals("say Automatic restart in 10s", commandQueue.poll());
        assertEquals("say Automatic restart in 5s", commandQueue.poll());
        assertEquals("say Automatic restart in 3s", commandQueue.poll());
        assertEquals("say Automatic restart in 1s", commandQueue.poll());
        assertEquals("say Restart the server", commandQueue.poll());
        assertEquals("stop", commandQueue.poll());
        assertTrue(commandQueue.isEmpty());
    }

    @Test
    public void parseDurationStringTest() {
        Duration result = DurationUtil.parse("1H 30M 5S");
        assertEquals(Duration.ofHours(1).plusMinutes(30).plusSeconds(5), result);

        result = DurationUtil.parse("30M 5S");
        assertEquals(Duration.ofMinutes(30).plusSeconds(5), result);

        result = DurationUtil.parse("5s");
        assertEquals(Duration.ofSeconds(5), result);

        result = DurationUtil.parse("-5s-5h-10m");
        assertEquals(Duration.ofHours(5).plusMinutes(10).plusSeconds(5), result);
    }

}
