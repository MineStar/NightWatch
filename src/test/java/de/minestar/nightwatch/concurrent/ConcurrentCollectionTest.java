package de.minestar.nightwatch.concurrent;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.junit.Assert;

/*
 * Simple test for concurrency using observable listener. You can't use the listener construct
 */
public class ConcurrentCollectionTest {

    private static final long LIST_EXAMPLES = 1000000L;

    private static final int CYCLES = 50;

//    @Test
    public void test() throws Exception {
        for (int i = 0; i < CYCLES; ++i)
            run(i + 1);
    }

    public void run(int cycle) throws InterruptedException {
        ObservableList<Long> list = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(new ArrayList<Long>((int) LIST_EXAMPLES)));
        List<String> results = new ArrayList<>((int) LIST_EXAMPLES);
        LongAdder c = new LongAdder();
        Runnable counter = () -> {
            while (c.longValue() < LIST_EXAMPLES) {
                synchronized (list) {
                    list.add(c.longValue());
                }
                c.increment();
            }

        };
        Thread t1 = new Thread(counter, "CounterThread");

        Runnable l2 = () -> list.addListener((ListChangeListener<Long>) c1 -> {

            while (c1.next()) {
                c1.getAddedSubList().forEach((s) -> results.add(Long.toString(s)));

            }

        });
        Thread t2 = new Thread(l2, "Collector");
        t2.start();
        t1.start();

        t1.join();
        t2.join();

        Assert.assertEquals("cycle: " + cycle, (list.size() - 1L), results.size());

        long last = 0L;
        for (String ss : results) {
            long j = Long.valueOf(ss);
            if (j - 1 == last) {
                last = j;
            } else {
                fail("Cyclus: " + cycle + ". ConccurentError: " + last + " was bigger then " + j);
            }
        }
    }
}
