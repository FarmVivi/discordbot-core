package fr.farmvivi.discordbot.core.command.impl.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;
import net.dv8tion.jda.api.entities.IMentionable;

import java.util.function.Predicate;

/**
 * Implementation of CommandOption for mentionable values.
 * Mentionable values can be users or roles.
 */
public class MentionableOption extends AbstractCommandOption<IMentionable> {
    
    /**
     * Private constructor used by the builder.
     *
     * @param name                the option name
     * @param description         the option description
     * @param required            true if the option is required
     * @param validator           the validator for the option value
     */
    private MentionableOption(
            String name,
            String description,
            boolean required,
            Predicate<IMentionable> validator
    ) {
        super(
                name,
                description,
                OptionType2.MENTIONABLE,
                required,
                null, // choices not applicable
                validator,
                null, // autocomplete not applicable
                null, // minValue not applicable
                null, // maxValue not applicable
                null, // minLength not applicable
                null  // maxLength not applicable
        );
    }
    
    /**
     * Creates a basic mentionable option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new mentionable option
     */
    public static MentionableOption of(String name, String description, boolean required) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .build();
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
    public static MentionableOption withValidator(
            String name,
            String description,
            boolean required,
            Predicate<IMentionable> validator
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .validator(validator)
                .build();
    }
    
    /**
     * Builder for mentionable options.
     */
    public static class Builder extends AbstractCommandOption.Builder<IMentionable, Builder> {
        
        /**
         * Creates a new builder for mentionable options.
         */
        public Builder() {
            this.type = OptionType2.MENTIONABLE;
        }
        
        @Override
        public CommandOption<IMentionable> build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Option name cannot be empty");
            }
            
            if (description == null || description.isEmpty()) {
                throw new IllegalStateException("Option description cannot be empty");
            }
            
            return new MentionableOption(
                    name,
                    description,
                    required,
                    validator
            );
        }
    }
}
