package fr.farmvivi.discordbot.core.command.impl;

import fr.farmvivi.discordbot.core.api.command.Command;
import fr.farmvivi.discordbot.core.api.command.CommandContext;
import fr.farmvivi.discordbot.core.api.command.CommandResult;
import fr.farmvivi.discordbot.core.api.command.option.CommandOption;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Abstract base implementation of Command.
 * Provides common functionality for all commands.
 */
public abstract class AbstractCommand implements Command {

    private final String name;
    private final String description;
    private final String category;
    private final List<CommandOption<?>> options;
    private final List<Command> subcommands;
    private final Set<String> aliases;
    private final Set<String> guildIds;
    private final boolean guildOnly;
    private final String permission;
    private final String translationKey;
    private final String group;
    private final Command parent;
    private final BiFunction<CommandContext, Command, CommandResult> executor;
    private final int cooldown;
    private boolean enabled;

    /**
     * Creates a new abstract command.
     * 
     * @param builder the builder with command configuration
     * @param parent the parent command, or null if this is a top-level command
     */
    protected AbstractCommand(AbstractCommandBuilder builder, Command parent) {
        this.name = Objects.requireNonNull(builder.name, "Command name cannot be null");
        this.description = Objects.requireNonNull(builder.description, "Command description cannot be null");
        this.category = builder.category != null ? builder.category : "general";
        this.options = Collections.unmodifiableList(new ArrayList<>(builder.options));
        this.subcommands = Collections.unmodifiableList(new ArrayList<>(builder.subcommands));
        this.aliases = Collections.unmodifiableSet(new HashSet<>(builder.aliases));
        this.guildIds = Collections.unmodifiableSet(new HashSet<>(builder.guildIds));
        this.guildOnly = builder.guildOnly;
        this.permission = builder.permission;
        this.translationKey = builder.translationKey != null ? builder.translationKey 
                : "command." + (parent != null ? parent.getName() + "." : "") + name;
        this.group = builder.group;
        this.parent = parent;
        this.executor = builder.executor;
        this.cooldown = builder.cooldown;
        this.enabled = builder.enabled;

        // Validate that we have either options or subcommands, but not both
        if (!options.isEmpty() && !subcommands.isEmpty()) {
            throw new IllegalStateException("Command cannot have both options and subcommands");
        }

        // Validate that we have an executor if we have no subcommands
        if (executor == null && subcommands.isEmpty()) {
            throw new IllegalStateException("Command must have an executor if it has no subcommands");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public List<CommandOption<?>> getOptions() {
        return options;
    }

    @Override
    public List<Command> getSubcommands() {
        return subcommands;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean isGuildOnly() {
        return guildOnly;
    }

    @Override
    public Set<String> getGuildIds() {
        return guildIds;
    }

    @Override
    public boolean isSubcommand() {
        return parent != null;
    }

    @Override
    public Command getParent() {
        return parent;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this command is enabled.
     * 
     * @param enabled true to enable the command, false to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getCooldown() {
        return cooldown;
    }

    @Override
    public CommandResult execute(CommandContext context) {
        if (executor != null) {
            return executor.apply(context, this);
        }
        
        // If no executor but we have subcommands, try to find a matching subcommand
        if (!subcommands.isEmpty()) {
            return CommandResult.error("This is a parent command. Please use one of its subcommands.");
        }
        
        return CommandResult.error("Command has no executor");
    }

    /**
     * Validates command options in the context.
     * Checks that all required options are provided and all provided options are valid.
     * 
     * @param context the command context
     * @return true if all options are valid, false otherwise
     */
    protected boolean validateOptions(CommandContext context) {
        for (CommandOption<?> option : options) {
            if (option.isRequired() && !context.hasOption(option.getName())) {
                context.replyError("Missing required option: " + option.getName());
                return false;
            }
            
            if (context.hasOption(option.getName())) {
                Object value = context.getOption(option.getName(), null);
                if (!option.isValid(value)) {
                    context.replyError("Invalid value for option: " + option.getName());
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * Abstract builder for commands.
     * Provides common functionality for all command builders.
     */
    public abstract static class AbstractCommandBuilder {
        protected String name;
        protected String description;
        protected String category = "general";
        protected final List<CommandOption<?>> options = new ArrayList<>();
        protected final List<Command> subcommands = new ArrayList<>();
        protected final Set<String> aliases = new HashSet<>();
        protected final Set<String> guildIds = new HashSet<>();
        protected boolean guildOnly = false;
        protected String permission;
        protected String translationKey;
        protected String group;
        protected BiFunction<CommandContext, Command, CommandResult> executor;
        protected int cooldown = 0;
        protected boolean enabled = true;

        /**
         * Builds the command.
         * 
         * @return the built command
         */
        public abstract Command build();
    }
}
