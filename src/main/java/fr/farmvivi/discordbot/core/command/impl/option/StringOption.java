package fr.farmvivi.discordbot.core.command.impl.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation of CommandOption for string values.
 */
public class StringOption extends AbstractCommandOption<String> {
    
    /**
     * Private constructor used by the builder.
     *
     * @param name                the option name
     * @param description         the option description
     * @param required            true if the option is required
     * @param choices             the available choices
     * @param validator           the validator for the option value
     * @param autocompleteProvider the autocomplete provider
     * @param minLength           the minimum length
     * @param maxLength           the maximum length
     */
    private StringOption(
            String name,
            String description,
            boolean required,
            List<OptionChoice<String>> choices,
            Predicate<String> validator,
            Function<String, List<OptionChoice<String>>> autocompleteProvider,
            Integer minLength,
            Integer maxLength
    ) {
        super(
                name,
                description,
                OptionType2.STRING,
                required,
                choices,
                validator,
                autocompleteProvider,
                null, // minValue not applicable
                null, // maxValue not applicable
                minLength,
                maxLength
        );
    }
    
    /**
     * Creates a basic string option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new string option
     */
    public static StringOption of(String name, String description, boolean required) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .build();
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
    public static StringOption withChoices(
            String name,
            String description,
            boolean required,
            OptionChoice<String>... choices
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .addChoices(choices)
                .build();
    }
    
    /**
     * Creates a string option with validation.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param validator   the validator
     * @return a new string option
     */
    public static StringOption withValidator(
            String name,
            String description,
            boolean required,
            Predicate<String> validator
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .validator(validator)
                .build();
    }
    
    /**
     * Creates a string option with autocompletion.
     *
     * @param name                the option name
     * @param description         the option description
     * @param required            true if the option is required
     * @param autocompleteProvider the autocomplete provider
     * @return a new string option
     */
    public static StringOption withAutocomplete(
            String name,
            String description,
            boolean required,
            Function<String, List<OptionChoice<String>>> autocompleteProvider
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .autocomplete(autocompleteProvider)
                .build();
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
    public static StringOption withLength(
            String name,
            String description,
            boolean required,
            Integer minLength,
            Integer maxLength
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .minLength(minLength)
                .maxLength(maxLength)
                .build();
    }
    
    /**
     * Builder for string options.
     */
    public static class Builder extends AbstractCommandOption.Builder<String, Builder> {
        
        /**
         * Creates a new builder for string options.
         */
        public Builder() {
            this.type = OptionType2.STRING;
        }
        
        @Override
        public CommandOption<String> build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Option name cannot be empty");
            }
            
            if (description == null || description.isEmpty()) {
                throw new IllegalStateException("Option description cannot be empty");
            }
            
            return new StringOption(
                    name,
                    description,
                    required,
                    choices,
                    validator,
                    autocompleteProvider,
                    minLength,
                    maxLength
            );
        }
    }
}
