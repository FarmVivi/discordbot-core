package fr.farmvivi.discordbot.core.command.impl.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation of CommandOption for integer values.
 */
public class IntegerOption extends AbstractCommandOption<Integer> {
    
    /**
     * Private constructor used by the builder.
     *
     * @param name                the option name
     * @param description         the option description
     * @param required            true if the option is required
     * @param choices             the available choices
     * @param validator           the validator for the option value
     * @param autocompleteProvider the autocomplete provider
     * @param minValue            the minimum value
     * @param maxValue            the maximum value
     */
    private IntegerOption(
            String name,
            String description,
            boolean required,
            List<OptionChoice<Integer>> choices,
            Predicate<Integer> validator,
            Function<String, List<OptionChoice<Integer>>> autocompleteProvider,
            Integer minValue,
            Integer maxValue
    ) {
        super(
                name,
                description,
                OptionType2.INTEGER,
                required,
                choices,
                validator,
                autocompleteProvider,
                minValue,
                maxValue,
                null, // minLength not applicable
                null  // maxLength not applicable
        );
    }
    
    /**
     * Creates a basic integer option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new integer option
     */
    public static IntegerOption of(String name, String description, boolean required) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .build();
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
    public static IntegerOption withChoices(
            String name,
            String description,
            boolean required,
            OptionChoice<Integer>... choices
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .addChoices(choices)
                .build();
    }
    
    /**
     * Creates an integer option with validation.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param validator   the validator
     * @return a new integer option
     */
    public static IntegerOption withValidator(
            String name,
            String description,
            boolean required,
            Predicate<Integer> validator
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .validator(validator)
                .build();
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
    public static IntegerOption withRange(
            String name,
            String description,
            boolean required,
            Integer min,
            Integer max
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .minValue(min)
                .maxValue(max)
                .build();
    }
    
    /**
     * Builder for integer options.
     */
    public static class Builder extends AbstractCommandOption.Builder<Integer, Builder> {
        
        /**
         * Creates a new builder for integer options.
         */
        public Builder() {
            this.type = OptionType2.INTEGER;
        }
        
        @Override
        public CommandOption<Integer> build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Option name cannot be empty");
            }
            
            if (description == null || description.isEmpty()) {
                throw new IllegalStateException("Option description cannot be empty");
            }
            
            return new IntegerOption(
                    name,
                    description,
                    required,
                    choices,
                    validator,
                    autocompleteProvider,
                    minValue != null ? minValue.intValue() : null,
                    maxValue != null ? maxValue.intValue() : null
            );
        }
    }
}
