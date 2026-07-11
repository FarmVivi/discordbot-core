package fr.farmvivi.fluxcord.core.command;

import fr.farmvivi.fluxcord.api.command.Command;
import fr.farmvivi.fluxcord.api.command.CommandContext;
import fr.farmvivi.fluxcord.api.command.option.CommandOption;
import fr.farmvivi.fluxcord.api.language.LanguageManager;
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
 * CommandContext adapter for modal interactions.
 */
public class InteractionCommandContext implements CommandContext {
    private final ModalInteractionEvent event;
    private final LanguageManager languageManager;
    private final Locale locale;

    public InteractionCommandContext(ModalInteractionEvent event, LanguageManager languageManager, Locale locale) {
        this.event = event;
        this.languageManager = languageManager;
        this.locale = locale;
    }

    @Override
    public Event getOriginalEvent() {
        return event;
    }

    @Override
    public Command getCommand() {
        return null; // Not tied to a Command instance
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
        throw new IllegalArgumentException("No options available for modal context");
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
        CommandMessageBuilder builder = new CommandMessageBuilder(event, languageManager, locale);
        builder.setContent(message);
        builder.setDiffer(true);
        builder.setEphemeral(true);
        builder.replyNow();
    }

    @Override
    public void reply(String message, Collection<MessageTopLevelComponent> components) {
        CommandMessageBuilder builder = new CommandMessageBuilder(event, languageManager, locale);
        builder.setContent(message);
        builder.setDiffer(true);
        builder.setEphemeral(true);
        if (components != null && !components.isEmpty()) builder.setComponents(components);
        builder.replyNow();
    }

    @Override
    public void replyEmbed(EmbedBuilder embed) {
        CommandMessageBuilder builder = new CommandMessageBuilder(event, languageManager, locale);
        builder.setDiffer(true);
        builder.setEphemeral(true);
        builder.addEmbeds(embed.build());
        builder.replyNow();
    }

    @Override
    public void replyEmbed(EmbedBuilder embed, Collection<MessageTopLevelComponent> components) {
        CommandMessageBuilder builder = new CommandMessageBuilder(event, languageManager, locale);
        builder.setDiffer(true);
        builder.setEphemeral(true);
        builder.addEmbeds(embed.build());
        if (components != null && !components.isEmpty()) builder.setComponents(components);
        builder.replyNow();
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
        deferReply(true);
    }

    @Override
    public void deferReply(boolean ephemeral) {
        if (!event.isAcknowledged()) {
            event.deferReply(ephemeral).queue();
        }
    }

    @Override
    public boolean isDeferred() {
        return true;
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    @Override
    public void setEphemeral(boolean ephemeral) { /* ignored in adapter */ }

    @Override
    public JDA getJDA() {
        return event.getJDA();
    }
}
