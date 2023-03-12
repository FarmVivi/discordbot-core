package fr.farmvivi.discordbot.module.music.command;

import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Map;

public class PlayCommand extends MusicCommand {
    public PlayCommand(MusicModule musicModule) {
        super(musicModule, "play", CommandCategory.MUSIC, "Ajoute une musique à la file d'attente");

        OptionData requestOption = new OptionData(OptionType.STRING, "requête", "Musique à ajouter à la file d'attente", true);

        this.setArgs(new OptionData[]{requestOption});
        this.setAliases(new String[]{"p"});
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Guild guild = event.getGuild();

        String query = args.get("requête").getAsString();

        musicModule.loadTrack(guild, query, event.getChannel(), reply, false);

        return true;
    }
}
