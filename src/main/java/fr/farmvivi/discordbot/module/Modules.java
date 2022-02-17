package fr.farmvivi.discordbot.module;

public enum Modules {
    COMMANDS("Commands"),
    MUSIC("Music", COMMANDS),
    CHANNEL_RENAME("Channels Renames"),
    TEST("Test", COMMANDS);

    private final String name;
    private final Modules[] requiredModules;

    Modules(String name, Modules... requiredModules) {
        this.name = name;
        this.requiredModules = requiredModules;
    }

    public String getName() {
        return name;
    }

    public Modules[] getRequiredModules() {
        return requiredModules;
    }
}
