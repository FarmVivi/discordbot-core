package fr.farmvivi.discordbot.core.command.impl.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;
import net.dv8tion.jda.api.entities.Message.Attachment;

import java.util.function.Predicate;

/**
 * Implementation of CommandOption for attachment values.
 */
public class AttachmentOption extends AbstractCommandOption<Attachment> {
    
    /**
     * Private constructor used by the builder.
     *
     * @param name                the option name
     * @param description         the option description
     * @param required            true if the option is required
     * @param validator           the validator for the option value
     */
    private AttachmentOption(
            String name,
            String description,
            boolean required,
            Predicate<Attachment> validator
    ) {
        super(
                name,
                description,
                OptionType2.ATTACHMENT,
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
     * Creates a basic attachment option.
     *
     * @param name        the option name
     * @param description the option description
     * @param required    true if the option is required
     * @return a new attachment option
     */
    public static AttachmentOption of(String name, String description, boolean required) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .build();
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
    public static AttachmentOption withValidator(
            String name,
            String description,
            boolean required,
            Predicate<Attachment> validator
    ) {
        return new Builder()
                .name(name)
                .description(description)
                .required(required)
                .validator(validator)
                .build();
    }
    
    /**
     * Builder for attachment options.
     */
    public static class Builder extends AbstractCommandOption.Builder<Attachment, Builder> {
        
        /**
         * Creates a new builder for attachment options.
         */
        public Builder() {
            this.type = OptionType2.ATTACHMENT;
        }
        
        @Override
        public CommandOption<Attachment> build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Option name cannot be empty");
            }
            
            if (description == null || description.isEmpty()) {
                throw new IllegalStateException("Option description cannot be empty");
            }
            
            return new AttachmentOption(
                    name,
                    description,
                    required,
                    validator
            );
        }
    }
}
