package fr.farmvivi.discordbot.module.general.poll;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class SimplePool extends Poll {
    private final boolean showVotesInResult;

    public SimplePool(Role role, long timeout, boolean showVotesInResult, String question, String... responses) {
        super(role, timeout, question, responses);

        this.showVotesInResult = showVotesInResult;
    }

    @Override
    public void finishPoll(List<PollResponse> responses) {
        super.finishPoll(responses);

        StringBuilder text = new StringBuilder();

        Role role = this.getRole();
        if (role != null) {
            text.append(role.getAsMention()).append(" ");
        }
        text.append("Résultats du sondage : **").append(this.getQuestion()).append("**\n");

        int previousPosition = 0;
        for (int i = 0; i < responses.size(); i++) {
            PollResponse pollResponse = responses.get(i);
            int position = i + 1;
            if (i != 0) {
                PollResponse previousPollResponse = responses.get(i - 1);
                if (pollResponse.getUsers().size() == previousPollResponse.getUsers().size()) {
                    position = previousPosition;
                }
            }
            text.append("\n");
            if (position == 1) {
                text.append(":first_place:");
            } else if (position == 2) {
                text.append(":second_place:");
            } else if (position == 3) {
                text.append(":third_place:");
            } else {
                text.append(":medal:");
            }
            text.append(" ")
                    .append(pollResponse.getResponse())
                    .append(" (")
                    .append(pollResponse.getUsers().size())
                    .append(" vote")
                    .append((pollResponse.getUsers().size() == 1) ? "" : "s")
                    .append(")");

            if (showVotesInResult && pollResponse.getUsers().size() > 0) {
                text.append(" : ");
                int j = 0;
                for (long userId : pollResponse.getUsers()) {
                    if (j > 25) {
                        text.append("...");
                        break;
                    }
                    if (j != 0) {
                        text.append(", ");
                    }
                    User user = this.getMessage().getJDA().getUserById(userId);
                    if (user != null) {
                        text.append(user.getAsMention());
                    } else {
                        text.append(userId);
                    }
                    j++;
                }
            }

            previousPosition = position;
        }

        this.getMessage().getChannel().sendMessage(text.toString()).queue();
    }
}
