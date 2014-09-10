package de.minestar.nightwatch.util;

public class Counter {

    private int count;

    public Counter() {
        this(0);
    }

    public Counter(int initialValue) {
        this.count = initialValue;
    }

    public void increment() {
        ++count;
    }

    public void decrement() {
        --count;
    }

    public int get() {
        return count;
    }

    public int incrementAndGet() {
        return ++count;
    }

    public int getAndIncrement() {
        return count++;
    }

}
