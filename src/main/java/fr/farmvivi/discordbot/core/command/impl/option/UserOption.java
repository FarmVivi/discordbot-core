package fr.farmvivi.discordbot.core.command.impl.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;
import net.dv8tion.jda.api.entities.User;

import java.util.function.Predicate;

/**
 * Implementation of CommandOption for user values.
 */
public class UserOption extends AbstractCommandOption<User> {
    
    /**
     * Private constructor used by the builder.
     *
     * @param name                the option name
     * @param description         the option description
     * @param required            true if the option is required
     * @param validator           the validator for the option value
     */
    private UserOption(
            String name,
            String description,
            boolean required,
            Predicate<User> validator
    ) {
        super(
                name,
                description,
                OptionType2.USER,
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
     * Creates a basic user option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new user option
     */
    public static UserOption of(String name, String description, boolean required) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .build();
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
    public static UserOption withValidator(
            String name,
            String description,
            boolean required,
            Predicate<User> validator
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .validator(validator)
                .build();
    }
    
    /**
     * Builder for user options.
     */
    public static class Builder extends AbstractCommandOption.Builder<User, Builder> {
        
        /**
         * Creates a new builder for user options.
         */
        public Builder() {
            this.type = OptionType2.USER;
        }
        
        @Override
        public CommandOption<User> build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Option name cannot be empty");
            }
            
            if (description == null || description.isEmpty()) {
                throw new IllegalStateException("Option description cannot be empty");
            }
            
            return new UserOption(
                    name,
                    description,
                    required,
                    validator
            );
        }
    }
}
