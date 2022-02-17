package fr.farmvivi.discordbot.module.commands;

public enum CommandCategory {
    MUSIC("Musique"), OTHER("Autres");

    private final String name;

    CommandCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
