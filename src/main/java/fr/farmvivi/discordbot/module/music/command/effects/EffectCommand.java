package fr.farmvivi.discordbot.module.music.command.effects;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;

public class EffectCommand extends Command {
    private final MusicModule musicModule;

    public EffectCommand(MusicModule musicModule) {
        super("effect", CommandCategory.MUSIC, "Gestion des effets");

        this.musicModule = musicModule;
        this.getSubCommands().add(new KaraokeCommand(musicModule));
        this.getSubCommands().add(new DistortionCommand(musicModule));
    }

    @Override
    public boolean execute(CommandReceivedEvent event, String content, CommandMessageBuilder reply) {
        if (!super.execute(event, content, reply))
            return false;

        StringBuilder stringBuilder = new StringBuilder("**Utilisation des commandes d'effets** :");

        for (Command cmd : this.getSubCommands()) {
            stringBuilder.append("\n->").append(formatCmdHelp(cmd));
        }

        reply.addContent(stringBuilder.toString());
        reply.setEphemeral(true);

        return true;
    }

    private String formatCmdHelp(Command command) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("** /").append(command.getName());
        if (command.getArgs().length != 0)
            stringBuilder.append(" ").append(command.getArgsAsString());
        stringBuilder.append(" **| ").append(command.getDescription());
        return stringBuilder.toString();
    }
}
