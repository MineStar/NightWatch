package de.minestar.nightwatch.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StartParameters {

    // JVM Parameters
    private int minMemoryMB = -1;
    private int maxMemoryMB = -1;
    private int permSizeMB = -1;

    private List<String> additional;

    public StartParameters() {
        this.additional = new ArrayList<>();
    }

    public StartParameters maxMemoryMB(int maxMemoryMB) {
        this.maxMemoryMB = maxMemoryMB;
        return this;
    }

    public StartParameters minMemoryMB(int minMemoryMB) {
        this.minMemoryMB = minMemoryMB;
        return this;
    }

    public StartParameters permSizeMB(int permSizeMB) {
        this.permSizeMB = permSizeMB;
        return this;
    }

    public int maxMemoryMB() {
        return maxMemoryMB;
    }

    public String maxMemoryString() {
        return maxMemoryMB != -1 ? "-Xmx" + maxMemoryMB + "M" : "";
    }

    public int minMemoryMB() {
        return minMemoryMB;
    }

    public String minMemoryString() {
        return minMemoryMB != -1 ? "-Xms" + minMemoryMB + "M" : "";
    }

    public int permSizeMB() {
        return permSizeMB;
    }

    public String permSizeString() {

        return permSizeMB != -1 ? "-XX:PermSize=" + permSizeMB + "M" : "";
    }

    public StartParameters addAdditional(String... parameter) {
        this.additional.addAll(Arrays.asList(parameter));
        return this;
    }
    
    public List<String> additionals() {
        return Collections.unmodifiableList(additional);
    }

}
