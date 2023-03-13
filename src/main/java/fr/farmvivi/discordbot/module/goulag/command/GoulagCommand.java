package fr.farmvivi.discordbot.module.goulag.command;

import fr.farmvivi.discordbot.module.commands.Command;
import fr.farmvivi.discordbot.module.commands.CommandCategory;
import fr.farmvivi.discordbot.module.commands.CommandMessageBuilder;
import fr.farmvivi.discordbot.module.commands.CommandReceivedEvent;
import fr.farmvivi.discordbot.module.goulag.GoulagPoll;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Map;

public class GoulagCommand extends Command {
    private final long respondentRoleId;
    private final long goulagRoleId;
    private final long pollTimeout;
    private final int minimumVotes;

    private GoulagPoll currentPoll;

    public GoulagCommand(long respondentRoleId, long goulagRoleId, long pollTimeout, int minimumVotes) {
        super("goulag", CommandCategory.FUN, "Faire un sondage pour mettre quelqu'un au goulag");

        OptionData userOption = new OptionData(OptionType.USER, "malfaisant", "Malfaisant à mettre au goulag", true);

        this.setArgs(new OptionData[]{userOption});
        this.setGuildOnly(true);

        this.respondentRoleId = respondentRoleId;
        this.goulagRoleId = goulagRoleId;
        this.pollTimeout = pollTimeout;
        this.minimumVotes = minimumVotes;
    }

    @Override
    public boolean execute(CommandReceivedEvent event, Map<String, OptionMapping> args, CommandMessageBuilder reply) {
        if (!super.execute(event, args, reply))
            return false;

        Member malfaisant = args.get("malfaisant").getAsMember();
        if (malfaisant == null) {
            //reply.addContent("> Une erreur est survenue lors de la récupération du malfaisant");
            reply.error("Impossible de récupérer le malfaisant");
            return false;
        }

        Role respondentRole = event.getGuild().getRoleById(this.respondentRoleId);
        Role goulagRole = event.getGuild().getRoleById(this.goulagRoleId);
        if (goulagRole == null) {
            //reply.addContent("> Une erreur est survenue, le rôle du goulag n'existe pas");
            reply.error("Impossible de récupérer le rôle du goulag");
            return false;
        }

        if (malfaisant.getUser().isBot()) {
            //reply.addContent("Tu ne peux pas mettre un bot au " + goulagRole.getAsMention() + " !");
            reply.error("Tu ne peux pas mettre un bot au " + goulagRole.getAsMention() + " !");
            return false;
        }

        if (malfaisant.getRoles().contains(goulagRole)) {
            //reply.addContent(malfaisant.getAsMention() + " est déjà au " + goulagRole.getAsMention() + " !");
            reply.error(malfaisant.getAsMention() + " est déjà au " + goulagRole.getAsMention() + " !");
            return false;
        }

        if (currentPoll != null) {
            //reply.addContent("Un sondage est déjà en cours pour envoyer " + currentPoll.getEvil().getAsMention() + " au " + goulagRole.getAsMention() + " !");
            reply.error("Un sondage est déjà en cours pour envoyer " + currentPoll.getEvil().getAsMention() + " au " + goulagRole.getAsMention() + " !");
            return false;
        }

        GoulagPoll poll = new GoulagPoll(this, respondentRole, goulagRole, malfaisant, pollTimeout, minimumVotes);
        poll.sendPoll(event.getChannel().asGuildMessageChannel());

        currentPoll = poll;

        reply.setEphemeral(true);
        //reply.addContent("Sondage envoyé pour mettre " + malfaisant.getAsMention() + " au " + goulagRole.getAsMention() + " !");
        reply.success("Sondage envoyé pour mettre " + malfaisant.getAsMention() + " au " + goulagRole.getAsMention() + " !");

        return true;
    }

    public void resetCurrentPoll() {
        currentPoll = null;
    }
}
