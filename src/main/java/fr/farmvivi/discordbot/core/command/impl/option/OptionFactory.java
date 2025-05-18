package fr.farmvivi.discordbot.core.command.impl.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Factory for creating command options of different types.
 * This provides a unified API for option creation without having to know the specific option classes.
 */
public final class OptionFactory {
    
    /**
     * Private constructor to prevent instantiation.
     */
    private OptionFactory() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Creates a string option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new string option
     */
    public static CommandOption<String> string(String name, String description, boolean required) {
        return StringOption.of(name, description, required);
    }
    
    /**
     * Creates a string option with choices.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param choices     the available choices
     * @return a new string option
     */
    public static CommandOption<String> string(String name, String description, boolean required, OptionChoice<String>... choices) {
        return StringOption.withChoices(name, description, required, choices);
    }
    
    /**
     * Creates a string option with length constraints.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param minLength   the minimum length
     * @param maxLength   the maximum length
     * @return a new string option
     */
    public static CommandOption<String> string(String name, String description, boolean required, Integer minLength, Integer maxLength) {
        return StringOption.withLength(name, description, required, minLength, maxLength);
    }
    
    /**
     * Creates a string option with autocomplete.
     *
     * @param name                the option name
     * @param description         the option description
     * @param required            true if the option is required
     * @param autocompleteProvider the autocomplete provider
     * @return a new string option
     */
    public static CommandOption<String> autocompleteString(
            String name,
            String description,
            boolean required,
            Function<String, List<OptionChoice<String>>> autocompleteProvider
    ) {
        return StringOption.withAutocomplete(name, description, required, autocompleteProvider);
    }
    
    /**
     * Creates an integer option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new integer option
     */
    public static CommandOption<Integer> integer(String name, String description, boolean required) {
        return IntegerOption.of(name, description, required);
    }
    
    /**
     * Creates an integer option with choices.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param choices     the available choices
     * @return a new integer option
     */
    public static CommandOption<Integer> integer(String name, String description, boolean required, OptionChoice<Integer>... choices) {
        return IntegerOption.withChoices(name, description, required, choices);
    }
    
    /**
     * Creates an integer option with range constraints.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param min         the minimum value
     * @param max         the maximum value
     * @return a new integer option
     */
    public static CommandOption<Integer> integer(String name, String description, boolean required, Integer min, Integer max) {
        return IntegerOption.withRange(name, description, required, min, max);
    }
    
    /**
     * Creates a boolean option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new boolean option
     */
    public static CommandOption<Boolean> bool(String name, String description, boolean required) {
        return BooleanOption.of(name, description, required);
    }
    
    /**
     * Creates a user option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new user option
     */
    public static CommandOption<User> user(String name, String description, boolean required) {
        return UserOption.of(name, description, required);
    }
    
    /**
     * Creates a user option with validation.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param validator   the validator
     * @return a new user option
     */
    public static CommandOption<User> user(String name, String description, boolean required, Predicate<User> validator) {
        return UserOption.withValidator(name, description, required, validator);
    }
    
    /**
     * Creates a role option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new role option
     */
    public static CommandOption<Role> role(String name, String description, boolean required) {
        return RoleOption.of(name, description, required);
    }
    
    /**
     * Creates a role option with validation.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param validator   the validator
     * @return a new role option
     */
    public static CommandOption<Role> role(String name, String description, boolean required, Predicate<Role> validator) {
        return RoleOption.withValidator(name, description, required, validator);
    }
    
    /**
     * Creates a channel option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new channel option
     */
    public static CommandOption<Channel> channel(String name, String description, boolean required) {
        return ChannelOption.of(name, description, required);
    }
    
    /**
     * Creates a channel option with validation.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param validator   the validator
     * @return a new channel option
     */
    public static CommandOption<Channel> channel(String name, String description, boolean required, Predicate<Channel> validator) {
        return ChannelOption.withValidator(name, description, required, validator);
    }
    
    /**
     * Creates a mentionable option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new mentionable option
     */
    public static CommandOption<IMentionable> mentionable(String name, String description, boolean required) {
        return MentionableOption.of(name, description, required);
    }
    
    /**
     * Creates a mentionable option with validation.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param validator   the validator
     * @return a new mentionable option
     */
    public static CommandOption<IMentionable> mentionable(String name, String description, boolean required, Predicate<IMentionable> validator) {
        return MentionableOption.withValidator(name, description, required, validator);
    }
    
    /**
     * Creates a number option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new number option
     */
    public static CommandOption<Double> number(String name, String description, boolean required) {
        return NumberOption.of(name, description, required);
    }
    
    /**
     * Creates a number option with choices.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param choices     the available choices
     * @return a new number option
     */
    public static CommandOption<Double> number(String name, String description, boolean required, OptionChoice<Double>... choices) {
        return NumberOption.withChoices(name, description, required, choices);
    }
    
    /**
     * Creates a number option with range constraints.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param min         the minimum value
     * @param max         the maximum value
     * @return a new number option
     */
    public static CommandOption<Double> number(String name, String description, boolean required, Double min, Double max) {
        return NumberOption.withRange(name, description, required, min, max);
    }
    
    /**
     * Creates an attachment option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new attachment option
     */
    public static CommandOption<Attachment> attachment(String name, String description, boolean required) {
        return AttachmentOption.of(name, description, required);
    }
    
    /**
     * Creates an attachment option with validation.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param validator   the validator
     * @return a new attachment option
     */
    public static CommandOption<Attachment> attachment(String name, String description, boolean required, Predicate<Attachment> validator) {
        return AttachmentOption.withValidator(name, description, required, validator);
    }
    
    /**
     * Creates a generic option based on the given type.
     *
     * @param type        the option type
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new option of the specified type
     */
    public static CommandOption<?> option(OptionType2 type, String name, String description, boolean required) {
        return switch (type) {
            case STRING -> string(name, description, required);
            case INTEGER -> integer(name, description, required);
            case BOOLEAN -> bool(name, description, required);
            case USER -> user(name, description, required);
            case CHANNEL -> channel(name, description, required);
            case ROLE -> role(name, description, required);
            case MENTIONABLE -> mentionable(name, description, required);
            case NUMBER -> number(name, description, required);
            case ATTACHMENT -> attachment(name, description, required);
        };
    }
}
