package fr.farmvivi.fluxcord.plugins.music.util;

import fr.farmvivi.fluxcord.api.command.Command;
import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.option.CommandOption;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

/**
 * Minimal CommandContext implementation to adapt a ModalInteractionEvent.
 */
public class ModalCommandContext implements CommandContext {
    private final ModalInteractionEvent event;
    private final Locale locale;
    private boolean deferred;
    private boolean ephemeral;

    public ModalCommandContext(ModalInteractionEvent event, Locale locale, boolean deferred, boolean ephemeral) {
        this.event = event;
        this.locale = locale;
        this.deferred = deferred || event.isAcknowledged();
        this.ephemeral = ephemeral;
    }

    @Override
    public Event getOriginalEvent() {
        return event;
    }

    @Override
    public Command getCommand() {
        return null;
    }

    @Override
    public User getUser() {
        return event.getUser();
    }

    @Override
    public Optional<Guild> getGuild() {
        return Optional.ofNullable(event.getGuild());
    }

    @Override
    public MessageChannel getChannel() {
        return event.getChannel();
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public <T> Optional<T> getOption(String name) {
        return Optional.empty();
    }

    @Override
    public <T> T getOption(String name, T defaultValue) {
        return defaultValue;
    }

    @Override
    public <T> T getRequiredOption(String name) {
        throw new IllegalArgumentException("No options for modal context");
    }

    @Override
    public <T> Optional<CommandOption<T>> getOptionDefinition(String name) {
        return Optional.empty();
    }

    @Override
    public boolean hasOption(String name) {
        return false;
    }

    @Override
    public void reply(String message) {
        if (!event.isAcknowledged() && !deferred) {
            event.reply(message).setEphemeral(ephemeral).queue();
        } else {
            event.getHook().sendMessage(message).setEphemeral(true).queue();
        }
    }

    @Override
    public void reply(String message, Collection<MessageTopLevelComponent> components) {
        if (!event.isAcknowledged() && !deferred) {
            event.reply(message).setEphemeral(ephemeral).addComponents(components).queue();
        } else {
            event.getHook().sendMessage(message).addComponents(components).setEphemeral(true).queue();
        }
    }

    @Override
    public void replyEmbed(EmbedBuilder embed) {
        if (!event.isAcknowledged() && !deferred) {
            event.replyEmbeds(embed.build()).setEphemeral(ephemeral).queue();
        } else {
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }

    @Override
    public void replyEmbed(EmbedBuilder embed, Collection<MessageTopLevelComponent> components) {
        if (!event.isAcknowledged() && !deferred) {
            event.replyEmbeds(embed.build()).addComponents(components).setEphemeral(ephemeral).queue();
        } else {
            event.getHook().sendMessageEmbeds(embed.build()).addComponents(components).setEphemeral(true).queue();
        }
    }

    @Override
    public void replySuccess(String message) {
        reply(message);
    }

    @Override
    public void replyInfo(String message) {
        reply(message);
    }

    @Override
    public void replyWarning(String message) {
        reply(message);
    }

    @Override
    public void replyError(String message) {
        reply(message);
    }

    @Override
    public void deferReply() {
        deferReply(this.ephemeral);
    }

    @Override
    public void deferReply(boolean ephemeral) {
        if (!event.isAcknowledged()) {
            event.deferReply(ephemeral).queue();
        }
        this.deferred = true;
        this.ephemeral = ephemeral;
    }

    @Override
    public boolean isDeferred() {
        return deferred;
    }

    @Override
    public boolean isEphemeral() {
        return ephemeral;
    }

    @Override
    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    @Override
    public JDA getJDA() {
        return event.getJDA();
    }
}
