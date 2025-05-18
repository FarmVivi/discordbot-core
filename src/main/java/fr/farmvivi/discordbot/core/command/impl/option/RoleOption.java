package fr.farmvivi.discordbot.core.command.impl.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;
import net.dv8tion.jda.api.entities.Role;

import java.util.function.Predicate;

/**
 * Implementation of CommandOption for role values.
 */
public class RoleOption extends AbstractCommandOption<Role> {
    
    /**
     * Private constructor used by the builder.
     *
     * @param name                the option name
     * @param description         the option description
     * @param required            true if the option is required
     * @param validator           the validator for the option value
     */
    private RoleOption(
            String name,
            String description,
            boolean required,
            Predicate<Role> validator
    ) {
        super(
                name,
                description,
                OptionType2.ROLE,
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
     * Creates a basic role option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new role option
     */
    public static RoleOption of(String name, String description, boolean required) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .build();
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
    public static RoleOption withValidator(
            String name,
            String description,
            boolean required,
            Predicate<Role> validator
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .validator(validator)
                .build();
    }
    
    /**
     * Builder for role options.
     */
    public static class Builder extends AbstractCommandOption.Builder<Role, Builder> {
        
        /**
         * Creates a new builder for role options.
         */
        public Builder() {
            this.type = OptionType2.ROLE;
        }
        
        @Override
        public CommandOption<Role> build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Option name cannot be empty");
            }
            
            if (description == null || description.isEmpty()) {
                throw new IllegalStateException("Option description cannot be empty");
            }
            
            return new RoleOption(
                    name,
                    description,
                    required,
                    validator
            );
        }
    }
}
