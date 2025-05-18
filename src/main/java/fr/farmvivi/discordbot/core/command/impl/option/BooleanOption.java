package fr.farmvivi.discordbot.core.command.impl.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation of CommandOption for boolean values.
 */
public class BooleanOption extends AbstractCommandOption<Boolean> {
    
    /**
     * Private constructor used by the builder.
     *
     * @param name                the option name
     * @param description         the option description
     * @param required            true if the option is required
     * @param choices             the available choices
     * @param validator           the validator for the option value
     * @param autocompleteProvider the autocomplete provider
     */
    private BooleanOption(
            String name,
            String description,
            boolean required,
            List<OptionChoice<Boolean>> choices,
            Predicate<Boolean> validator,
            Function<String, List<OptionChoice<Boolean>>> autocompleteProvider
    ) {
        super(
                name,
                description,
                OptionType2.BOOLEAN,
                required,
                choices,
                validator,
                autocompleteProvider,
                null, // minValue not applicable
                null, // maxValue not applicable
                null, // minLength not applicable
                null  // maxLength not applicable
        );
    }
    
    /**
     * Creates a basic boolean option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new boolean option
     */
    public static BooleanOption of(String name, String description, boolean required) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .build();
    }
    
    /**
     * Builder for boolean options.
     */
    public static class Builder extends AbstractCommandOption.Builder<Boolean, Builder> {
        
        /**
         * Creates a new builder for boolean options.
         */
        public Builder() {
            this.type = OptionType2.BOOLEAN;
        }
        
        @Override
        public CommandOption<Boolean> build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Option name cannot be empty");
            }
            
            if (description == null || description.isEmpty()) {
                throw new IllegalStateException("Option description cannot be empty");
            }
            
            return new BooleanOption(
                    name,
                    description,
                    required,
                    choices,
                    validator,
                    autocompleteProvider
            );
        }
    }
}
