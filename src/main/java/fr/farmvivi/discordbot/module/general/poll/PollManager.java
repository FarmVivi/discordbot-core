package fr.farmvivi.discordbot.module.general.poll;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.LinkedList;
import java.util.List;

public class PollManager extends ListenerAdapter {
    private final List<Poll> polls = new LinkedList<>();

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getMember() == null || event.getUser() == null || event.getUser().isBot()) {
            return;
        }

        for (Poll poll : polls) {
            if (poll.getMessage().getIdLong() == event.getMessageIdLong()) {
                if (poll.getRole() != null && !poll.getRole().isPublicRole() && !event.getMember().getRoles().contains(poll.getRole())) {
                    event.getReaction().removeReaction(event.getUser()).queue();
                    return;
                }

                PollEmoji pollEmoji = PollEmoji.getEmoji(event.getEmoji());
                if (pollEmoji.equals(PollEmoji.UNKNOWN)) {
                    continue;
                }
                poll.getResponses().get(pollEmoji.getNumber()).getUsers().add(event.getUser().getIdLong());
                return;
            }
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) {
            return;
        }

        for (Poll poll : polls) {
            if (poll.getMessage().getIdLong() == event.getMessageIdLong()) {
                PollEmoji pollEmoji = PollEmoji.getEmoji(event.getEmoji());
                if (pollEmoji.equals(PollEmoji.UNKNOWN)) {
                    continue;
                }
                poll.getResponses().get(pollEmoji.getNumber()).getUsers().remove(event.getUser().getIdLong());
                return;
            }
        }
    }

    public void registerPoll(Poll poll) {
        polls.add(poll);
    }

    public void unregisterPoll(Poll poll) {
        polls.remove(poll);
    }
}
