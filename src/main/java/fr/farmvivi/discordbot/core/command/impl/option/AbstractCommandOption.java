package fr.farmvivi.discordbot.core.command.impl.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Abstract base implementation of CommandOption.
 * Provides common functionality for all option types.
 *
 * @param <T> the type of the option value
 */
public abstract class AbstractCommandOption<T> implements CommandOption<T> {
    
    private final String name;
    private final String description;
    private final OptionType2 type;
    private final boolean required;
    private final List<OptionChoice<T>> choices;
    private final Predicate<T> validator;
    private final Function<String, List<OptionChoice<T>>> autocompleteProvider;
    private final Number minValue;
    private final Number maxValue;
    private final Integer minLength;
    private final Integer maxLength;
    
    /**
     * Creates a new abstract command option.
     *
     * @param name                the option name
     * @param description         the option description
     * @param type                the option type
     * @param required            true if the option is required
     * @param choices             the available choices
     * @param validator           the validator for the option value
     * @param autocompleteProvider the autocomplete provider
     * @param minValue            the minimum value for number options
     * @param maxValue            the maximum value for number options
     * @param minLength           the minimum length for string options
     * @param maxLength           the maximum length for string options
     */
    protected AbstractCommandOption(
            String name,
            String description,
            OptionType2 type,
            boolean required,
            List<OptionChoice<T>> choices,
            Predicate<T> validator,
            Function<String, List<OptionChoice<T>>> autocompleteProvider,
            Number minValue,
            Number maxValue,
            Integer minLength,
            Integer maxLength
    ) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
        this.choices = choices != null ? new ArrayList<>(choices) : Collections.emptyList();
        this.validator = validator;
        this.autocompleteProvider = autocompleteProvider;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minLength = minLength;
        this.maxLength = maxLength;
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
    public OptionType2 getType() {
        return type;
    }
    
    @Override
    public boolean isRequired() {
        return required;
    }
    
    @Override
    public List<OptionChoice<T>> getChoices() {
        return Collections.unmodifiableList(choices);
    }
    
    @Override
    public Predicate<T> getValidator() {
        return validator;
    }
    
    @Override
    public Function<String, List<OptionChoice<T>>> getAutocompleteProvider() {
        return autocompleteProvider;
    }
    
    @Override
    public Number getMinValue() {
        return minValue;
    }
    
    @Override
    public Number getMaxValue() {
        return maxValue;
    }
    
    @Override
    public Integer getMinLength() {
        return minLength;
    }
    
    @Override
    public Integer getMaxLength() {
        return maxLength;
    }
    
    /**
     * Builder for creating command options.
     *
     * @param <T> the type of the option value
     * @param <B> the builder type
     */
    public abstract static class Builder<T, B extends Builder<T, B>> {
        
        protected String name;
        protected String description;
        protected OptionType2 type;
        protected boolean required;
        protected List<OptionChoice<T>> choices = new ArrayList<>();
        protected Predicate<T> validator;
        protected Function<String, List<OptionChoice<T>>> autocompleteProvider;
        protected Number minValue;
        protected Number maxValue;
        protected Integer minLength;
        protected Integer maxLength;
        
        /**
         * Sets the name of the option.
         *
         * @param name the option name
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B name(String name) {
            this.name = name;
            return (B) this;
        }
        
        /**
         * Sets the description of the option.
         *
         * @param description the option description
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B description(String description) {
            this.description = description;
            return (B) this;
        }
        
        /**
         * Sets the type of the option.
         *
         * @param type the option type
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B type(OptionType2 type) {
            this.type = type;
            return (B) this;
        }
        
        /**
         * Sets whether the option is required.
         *
         * @param required true if the option is required
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B required(boolean required) {
            this.required = required;
            return (B) this;
        }
        
        /**
         * Adds a choice to the option.
         *
         * @param choice the choice to add
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B addChoice(OptionChoice<T> choice) {
            this.choices.add(choice);
            return (B) this;
        }
        
        /**
         * Adds multiple choices to the option.
         *
         * @param choices the choices to add
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B addChoices(OptionChoice<T>... choices) {
            Collections.addAll(this.choices, choices);
            return (B) this;
        }
        
        /**
         * Sets the validator for the option.
         *
         * @param validator the validator
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B validator(Predicate<T> validator) {
            this.validator = validator;
            return (B) this;
        }
        
        /**
         * Sets the autocomplete provider for the option.
         *
         * @param autocompleteProvider the autocomplete provider
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B autocomplete(Function<String, List<OptionChoice<T>>> autocompleteProvider) {
            this.autocompleteProvider = autocompleteProvider;
            return (B) this;
        }
        
        /**
         * Sets the minimum value for the option.
         *
         * @param minValue the minimum value
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B minValue(Number minValue) {
            this.minValue = minValue;
            return (B) this;
        }
        
        /**
         * Sets the maximum value for the option.
         *
         * @param maxValue the maximum value
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B maxValue(Number maxValue) {
            this.maxValue = maxValue;
            return (B) this;
        }
        
        /**
         * Sets the minimum length for the option.
         *
         * @param minLength the minimum length
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B minLength(Integer minLength) {
            this.minLength = minLength;
            return (B) this;
        }
        
        /**
         * Sets the maximum length for the option.
         *
         * @param maxLength the maximum length
         * @return this builder
         */
        @SuppressWarnings("unchecked")
        public B maxLength(Integer maxLength) {
            this.maxLength = maxLength;
            return (B) this;
        }
        
        /**
         * Builds the option.
         *
         * @return the built option
         */
        public abstract CommandOption<T> build();
    }
}
