package fr.farmvivi.discordbot.core.command.impl.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;
import net.dv8tion.jda.api.entities.channel.Channel;

import java.util.function.Predicate;

/**
 * Implementation of CommandOption for channel values.
 */
public class ChannelOption extends AbstractCommandOption<Channel> {
    
    /**
     * Private constructor used by the builder.
     *
     * @param name                the option name
     * @param description         the option description
     * @param required            true if the option is required
     * @param validator           the validator for the option value
     */
    private ChannelOption(
            String name,
            String description,
            boolean required,
            Predicate<Channel> validator
    ) {
        super(
                name,
                description,
                OptionType2.CHANNEL,
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
     * Creates a basic channel option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new channel option
     */
    public static ChannelOption of(String name, String description, boolean required) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .build();
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
    public static ChannelOption withValidator(
            String name,
            String description,
            boolean required,
            Predicate<Channel> validator
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .validator(validator)
                .build();
    }
    
    /**
     * Builder for channel options.
     */
    public static class Builder extends AbstractCommandOption.Builder<Channel, Builder> {
        
        /**
         * Creates a new builder for channel options.
         */
        public Builder() {
            this.type = OptionType2.CHANNEL;
        }
        
        @Override
        public CommandOption<Channel> build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Option name cannot be empty");
            }
            
            if (description == null || description.isEmpty()) {
                throw new IllegalStateException("Option description cannot be empty");
            }
            
            return new ChannelOption(
                    name,
                    description,
                    required,
                    validator
            );
        }
    }
}
