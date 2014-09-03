package de.minestar.nightwatch.server;

public enum ServerType {

    VANILLA("Vanilla"), BUKKIT("Craftbukkit"), SPIGOT("Spigot"), MCPC("MCPC");

    private final String name;

    private ServerType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
