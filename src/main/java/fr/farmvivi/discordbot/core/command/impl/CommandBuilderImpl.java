package fr.farmvivi.discordbot.core.command.impl;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandBuilder;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;
import fr.farmvivi.discordbot.core.command.impl.option.OptionFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation of CommandBuilder for creating commands.
 */
public class CommandBuilderImpl implements CommandBuilder {

    private final CommandBuilderInternal builder;
    private final Command parent;

    /**
     * Creates a new command builder.
     */
    public CommandBuilderImpl() {
        this(null);
    }

    /**
     * Creates a new command builder for a subcommand.
     *
     * @param parent the parent command
     */
    public CommandBuilderImpl(Command parent) {
        this.builder = new CommandBuilderInternal();
        this.parent = parent;
    }

    @Override
    public CommandBuilder name(String name) {
        builder.name = name;
        return this;
    }

    @Override
    public CommandBuilder description(String description) {
        builder.description = description;
        return this;
    }

    @Override
    public CommandBuilder category(String category) {
        builder.category = category;
        return this;
    }

    @Override
    public CommandBuilder group(String group) {
        builder.group = group;
        return this;
    }

    @Override
    public CommandBuilder permission(String permission) {
        builder.permission = permission;
        return this;
    }

    @Override
    public CommandBuilder translationKey(String translationKey) {
        builder.translationKey = translationKey;
        return this;
    }

    @Override
    public CommandBuilder alias(String alias) {
        builder.aliases.add(alias);
        return this;
    }

    @Override
    public CommandBuilder aliases(String... aliases) {
        builder.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    @Override
    public CommandBuilder guildOnly(boolean guildOnly) {
        builder.guildOnly = guildOnly;
        return this;
    }

    @Override
    public CommandBuilder guilds(String... guildIds) {
        builder.guildIds.addAll(Arrays.asList(guildIds));
        return this;
    }

    @Override
    public CommandBuilder enabled(boolean enabled) {
        builder.enabled = enabled;
        return this;
    }

    @Override
    public CommandBuilder cooldown(int cooldown) {
        builder.cooldown = cooldown;
        return this;
    }

    @Override
    public CommandBuilder stringOption(String name, String description, boolean required) {
        builder.options.add(OptionFactory.string(name, description, required));
        return this;
    }

    @Override
    public CommandBuilder stringOption(String name, String description, boolean required, OptionChoice<String>... choices) {
        builder.options.add(OptionFactory.string(name, description, required, choices));
        return this;
    }

    @Override
    public CommandBuilder stringOption(String name, String description, boolean required, Predicate<String> validator) {
        builder.options.add(OptionFactory.string(name, description, required, validator));
        return this;
    }

    @Override
    public CommandBuilder stringOption(String name, String description, boolean required, Function<String, List<OptionChoice<String>>> autocompleteProvider) {
        builder.options.add(OptionFactory.autocompleteString(name, description, required, autocompleteProvider));
        return this;
    }

    @Override
    public CommandBuilder integerOption(String name, String description, boolean required) {
        builder.options.add(OptionFactory.integer(name, description, required));
        return this;
    }

    @Override
    public CommandBuilder integerOption(String name, String description, boolean required, OptionChoice<Integer>... choices) {
        builder.options.add(OptionFactory.integer(name, description, required, choices));
        return this;
    }

    @Override
    public CommandBuilder integerOption(String name, String description, boolean required, Predicate<Integer> validator) {
        builder.options.add(OptionFactory.integer(name, description, required, validator));
        return this;
    }

    @Override
    public CommandBuilder integerOption(String name, String description, boolean required, Integer min, Integer max) {
        builder.options.add(OptionFactory.integer(name, description, required, min, max));
        return this;
    }

    @Override
    public CommandBuilder booleanOption(String name, String description, boolean required) {
        builder.options.add(OptionFactory.bool(name, description, required));
        return this;
    }

    @Override
    public CommandBuilder userOption(String name, String description, boolean required) {
        builder.options.add(OptionFactory.user(name, description, required));
        return this;
    }

    @Override
    public CommandBuilder channelOption(String name, String description, boolean required) {
        builder.options.add(OptionFactory.channel(name, description, required));
        return this;
    }

    @Override
    public CommandBuilder roleOption(String name, String description, boolean required) {
        builder.options.add(OptionFactory.role(name, description, required));
        return this;
    }

    @Override
    public CommandBuilder mentionableOption(String name, String description, boolean required) {
        builder.options.add(OptionFactory.mentionable(name, description, required));
        return this;
    }

    @Override
    public CommandBuilder numberOption(String name, String description, boolean required) {
        builder.options.add(OptionFactory.number(name, description, required));
        return this;
    }

    @Override
    public CommandBuilder numberOption(String name, String description, boolean required, Double min, Double max) {
        builder.options.add(OptionFactory.number(name, description, required, min, max));
        return this;
    }

    @Override
    public CommandBuilder attachmentOption(String name, String description, boolean required) {
        builder.options.add(OptionFactory.attachment(name, description, required));
        return this;
    }

    @Override
    public CommandBuilder option(OptionType2 type, String name, String description, boolean required) {
        builder.options.add(OptionFactory.option(type, name, description, required));
        return this;
    }

    @Override
    public CommandBuilder option(CommandOption<?> option) {
        builder.options.add(option);
        return this;
    }

    @Override
    public CommandBuilder subcommand(Consumer<CommandBuilder> subcommandConsumer) {
        Command command = build();
        CommandBuilderImpl subcommandBuilder = new CommandBuilderImpl(command);
        subcommandConsumer.accept(subcommandBuilder);
        builder.subcommands.add(subcommandBuilder.build());
        return this;
    }

    @Override
    public CommandBuilder executor(BiFunction<CommandContext, Command, CommandResult> executor) {
        builder.executor = executor;
        return this;
    }

    @Override
    public CommandBuilder execute(BiFunction<CommandContext, Command, CommandResult> executor) {
        return executor(executor);
    }

    @Override
    public Command build() {
        return new CommandImpl(builder, parent);
    }

    /**
     * Creates a new command builder.
     *
     * @return a new command builder
     */
    public static CommandBuilder create() {
        return new CommandBuilderImpl();
    }

    /**
     * Internal command builder implementation.
     */
    private static class CommandBuilderInternal extends AbstractCommand.AbstractCommandBuilder {
        
        @Override
        public Command build() {
            return new CommandImpl(this, null);
        }
    }

    /**
     * Implementation of Command.
     */
    private static class CommandImpl extends AbstractCommand {
        
        /**
         * Creates a new command.
         *
         * @param builder the builder
         * @param parent the parent command
         */
        public CommandImpl(AbstractCommandBuilder builder, Command parent) {
            super(builder, parent);
        }
    }
}
