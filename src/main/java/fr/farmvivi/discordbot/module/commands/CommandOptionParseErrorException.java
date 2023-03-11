package fr.farmvivi.discordbot.module.commands;

public class CommandOptionParseErrorException extends Exception {
    public CommandOptionParseErrorException(String message) {
        super(message);
    }
}
