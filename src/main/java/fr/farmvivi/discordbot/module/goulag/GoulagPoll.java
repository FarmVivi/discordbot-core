package fr.farmvivi.discordbot.module.goulag;

import fr.farmvivi.discordbot.module.general.poll.Poll;
import fr.farmvivi.discordbot.module.general.poll.PollResponse;
import fr.farmvivi.discordbot.module.goulag.command.GoulagCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class GoulagPoll extends Poll {
    private final GoulagCommand command;
    private final Role respondentRole;
    private final Role goulagRole;
    private final Member evil;
    private final int minimumVotes;

    public GoulagPoll(GoulagCommand goulagCommand, Role respondentRole, Role goulagRole, Member evil, long timeout, int minimumVotes) {
        super(respondentRole, timeout, "Voulez-vous envoyer " + evil.getAsMention() + " au " + goulagRole.getAsMention() + " ?", "Oui", "Non");

        this.command = goulagCommand;
        this.respondentRole = respondentRole;
        this.goulagRole = goulagRole;
        this.evil = evil;
        this.minimumVotes = minimumVotes;
    }

    @Override
    public void finishPoll(List<PollResponse> responses) {
        super.finishPoll(responses);

        PollResponse yesResponse = null;
        PollResponse noResponse = null;
        for (PollResponse response : responses) {
            if (response.getResponse().equals("Oui")) {
                yesResponse = response;
            } else if (response.getResponse().equals("Non")) {
                noResponse = response;
            }
        }

        if (yesResponse == null || noResponse == null) {
            throw new IllegalStateException("Poll not finished");
        }

        int yes = yesResponse.getUsers().size();
        int no = noResponse.getUsers().size();

        if (yes + no < minimumVotes) {
            this.getMessage().getChannel().sendMessage("> Il faut au moins 5 votes pour envoyer " + evil.getAsMention() + " au " + goulagRole.getAsMention() + " ! (**" + yes + "** pour, **" + no + "** contre)").queue();
        } else if (yes < no) {
            this.getMessage().getChannel().sendMessage("> Le " + goulagRole.getAsMention() + " n'est pas pour toi " + evil.getAsMention() + " ! (**" + yes + "** pour, **" + no + "** contre)").queue();
        } else if (yes == no) {
            this.getMessage().getChannel().sendMessage("> Égalité ! Tiens-toi à carreau " + evil.getAsMention() + " ! (**" + yes + "** pour, **" + no + "** contre)").queue();
        } else {
            String goulagEnterMessage = "> ";
            if (respondentRole != null) {
                goulagEnterMessage += respondentRole.getAsMention() + ", ";
            }
            goulagEnterMessage += evil.getAsMention() + " est envoyé au " + goulagRole.getAsMention() + " ! (**" + yes + "** pour, **" + no + "** contre)";
            this.getMessage().getChannel().sendMessage(goulagEnterMessage).and(this.getMessage().getChannel().sendMessage("https://tenor.com/view/pokemon-chammal-chamsin-metro-train-gif-23999164")).queue();
            for (Member member : this.getMessage().getGuild().getMembersWithRoles(goulagRole)) {
                this.getMessage().getGuild().removeRoleFromMember(member, goulagRole).queue();
                this.getMessage().getChannel().sendMessage("> " + member.getAsMention() + " est libéré du " + goulagRole.getAsMention() + " !").queue();
            }
            this.evil.getGuild().addRoleToMember(this.evil, this.goulagRole).queue();
        }

        command.resetCurrentPoll();
    }

    public Member getEvil() {
        return evil;
    }
}
