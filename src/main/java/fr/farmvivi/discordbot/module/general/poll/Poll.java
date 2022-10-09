package fr.farmvivi.discordbot.module.general.poll;

import fr.farmvivi.discordbot.Bot;
import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.general.GeneralModule;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public abstract class Poll {
    private final String question;
    private final Map<Integer, PollResponse> responses = new TreeMap<>();
    private final Role role;
    private final long timeout;

    private Message message;

    public Poll(String question, String... responses) {
        this(null, question, responses);
    }

    public Poll(long timeout, String question, String... responses) {
        this(null, timeout, question, responses);
    }

    public Poll(Role role, String question, String... responses) {
        this(role, -1, question, responses);
    }

    public Poll(Role role, long timeout, String question, String... responses) {
        this.role = role;
        this.timeout = timeout;
        this.question = question;
        for (int i = 0; i < responses.length; i++) {
            PollResponse pollResponse = new PollResponse(i + 1, responses[i]);
            this.responses.put(pollResponse.getId(), pollResponse);
        }
    }

    public void sendPoll(GuildMessageChannel channel) {
        if (this.message != null) {
            throw new IllegalStateException("Poll already sent");
        }
        OffsetDateTime sentTime = OffsetDateTime.now();

        StringBuilder text = new StringBuilder();
        if (role != null) {
            text.append(role.getAsMention()).append(" ");
        }
        text.append("Sondage : **").append(this.question).append("**\n");

        for (PollResponse pollResponse : responses.values()) {
            text.append("\n");
            text.append(PollEmoji.getEmoji(pollResponse.getId()).getEmoji().getFormatted()).append(" ").append(pollResponse.getResponse());
        }

        OffsetDateTime timeoutTime = sentTime.plusSeconds(timeout);
        if (timeout != -1) {
            text.append("\n\nCe sondage se terminera <t:").append(timeoutTime.toEpochSecond()).append(":R>");
        }

        PollManager pollManager = ((GeneralModule) Bot.getInstance().getModulesManager().getModule(Modules.GENERAL)).getPollManager();
        pollManager.registerPoll(this);

        // Envoie du sondage dans le salon et ajout des réactions, puis suppression du sondage et affichage des résultats
        channel.sendMessage(text.toString()).flatMap(message -> {
            this.message = message;
            RestAction<Void> restAction = null;
            for (PollResponse pollResponse : responses.values()) {
                if (restAction == null) {
                    restAction = message.addReaction(PollEmoji.getEmoji(pollResponse.getId()).getEmoji());
                } else {
                    restAction = restAction.and(message.addReaction(PollEmoji.getEmoji(pollResponse.getId()).getEmoji()));
                }
            }
            if (timeout != -1) {
                assert restAction != null;
                restAction = restAction.delay(timeout, TimeUnit.SECONDS).flatMap(message1 -> {
                    pollManager.unregisterPoll(this);
                    List<PollResponse> responses = new ArrayList<>(this.responses.values());
                    responses.sort(new PollResponseComparator());
                    finishPoll(responses);
                    return message.delete();
                });
            }
            return restAction;
        }).queue();
    }

    public void finishPoll(List<PollResponse> responses) {
        if (this.message == null) {
            throw new IllegalStateException("Poll not sent");
        }
        sendResults(responses);
    }

    private void sendResults(List<PollResponse> responses) {
        if (message == null) {
            throw new IllegalStateException("Poll not sent");
        }

        StringBuilder text = new StringBuilder();
        if (role != null) {
            text.append(role.getAsMention()).append(" ");
        }
        text.append("Résultats du sondage : **").append(this.question).append("**\n");

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
            text.append(" ").append(pollResponse.getResponse()).append(" (").append(pollResponse.getUsers().size()).append(" vote").append((pollResponse.getUsers().size() == 1) ? "" : "s").append(")");
            previousPosition = position;
        }

        message.getChannel().sendMessage(text.toString()).queue();
    }

    public String getQuestion() {
        return question;
    }

    public Map<Integer, PollResponse> getResponses() {
        return responses;
    }

    public Role getRole() {
        return role;
    }

    public Message getMessage() {
        return message;
    }
}
