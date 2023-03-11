package fr.farmvivi.discordbot.module;

public enum Modules {
    COMMANDS("Commands"),
    FORMS("Forms"),
    GENERAL("General", COMMANDS),
    GOULAG("Goulag", GENERAL, COMMANDS),
    MUSIC("Music", COMMANDS),
    CNAM("Cnam", GOULAG, COMMANDS, FORMS),
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
