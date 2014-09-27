package de.minestar.nightwatch.util;

/**
 * Encapsulate an single integer and provide standard operation. An usage is for lambdas, which can only use final primitive variables. <br>
 * This class is mutable, so any operation will change this object instead of returning a new instance.
 */
public class Counter implements Comparable<Counter> {

    /**
     * The integer.
     */
    private int value;

    /**
     * Construct a counter with the initial value zero.
     */
    public Counter() {
        this(0);
    }

    /**
     * Construct a counter beginning with the given value.
     * 
     * @param initialValue
     *            The value the counter initialized.
     */
    public Counter(int initialValue) {
        this.value = initialValue;
    }

    /**
     * Increment the value ( this value + 1).
     */
    public void increment() {
        ++value;
    }

    /**
     * Decrement the value (this value - 1).
     */
    public void decrement() {
        --value;
    }

    /**
     * @return The current value of the counter.
     */
    public int get() {
        return value;
    }

    /**
     * @return This value after incrementing the value (post-increment).
     */
    public int incrementAndGet() {
        return ++value;
    }

    /**
     * @return The value before incrementing the value (pre-increment).
     */
    public int getAndIncrement() {
        return value++;
    }

    @Override
    public String toString() {
        return "Counter [count=" + value + "]";
    }

    @Override
    public int compareTo(Counter o) {
        return Integer.compare(this.value, o.value);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Counter))
            return false;

        return this.value == ((Counter) obj).value;
    }

}
