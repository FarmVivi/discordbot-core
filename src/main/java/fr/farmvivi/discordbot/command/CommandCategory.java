package fr.farmvivi.discordbot.command;

public enum CommandCategory {
    MUSIC("Musique"), OTHER("Autres");

    private String name;

    CommandCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
