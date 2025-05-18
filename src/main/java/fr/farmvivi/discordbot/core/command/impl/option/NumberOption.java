package fr.farmvivi.discordbot.core.command.impl.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation of CommandOption for double (number) values.
 */
public class NumberOption extends AbstractCommandOption<Double> {
    
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
    private NumberOption(
            String name,
            String description,
            boolean required,
            List<OptionChoice<Double>> choices,
            Predicate<Double> validator,
            Function<String, List<OptionChoice<Double>>> autocompleteProvider,
            Double minValue,
            Double maxValue
    ) {
        super(
                name,
                description,
                OptionType2.NUMBER,
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
     * Creates a basic number option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new number option
     */
    public static NumberOption of(String name, String description, boolean required) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .build();
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
    public static NumberOption withChoices(
            String name,
            String description,
            boolean required,
            OptionChoice<Double>... choices
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .addChoices(choices)
                .build();
    }
    
    /**
     * Creates a number option with validation.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @param validator   the validator
     * @return a new number option
     */
    public static NumberOption withValidator(
            String name,
            String description,
            boolean required,
            Predicate<Double> validator
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .validator(validator)
                .build();
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
    public static NumberOption withRange(
            String name,
            String description,
            boolean required,
            Double min,
            Double max
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
     * Builder for number options.
     */
    public static class Builder extends AbstractCommandOption.Builder<Double, Builder> {
        
        /**
         * Creates a new builder for number options.
         */
        public Builder() {
            this.type = OptionType2.NUMBER;
        }
        
        @Override
        public CommandOption<Double> build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Option name cannot be empty");
            }
            
            if (description == null || description.isEmpty()) {
                throw new IllegalStateException("Option description cannot be empty");
            }
            
            return new NumberOption(
                    name,
                    description,
                    required,
                    choices,
                    validator,
                    autocompleteProvider,
                    minValue != null ? minValue.doubleValue() : null,
                    maxValue != null ? maxValue.doubleValue() : null
            );
        }
    }
}
