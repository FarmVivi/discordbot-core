package fr.farmvivi.discordbot.module.commands;

public enum CommandCategory {
    UTILS("Utilitaire"),
    MUSIC("Musique"),
    FUN("Fun"),
    CNAM("CNAM"),
    OTHER("Autres");

    private final String name;

    CommandCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
