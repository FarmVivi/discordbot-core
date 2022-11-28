package fr.farmvivi.discordbot.module.cnam.form.devoir.add.step;

import fr.farmvivi.discordbot.module.cnam.form.devoir.add.AddDevoirForm;
import fr.farmvivi.discordbot.module.forms.FormStep;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

public class DescriptionFormStep extends FormStep {
    private final AddDevoirForm form;

    public DescriptionFormStep(AddDevoirForm form) {
        super(form);

        this.form = form;
    }

    @Override
    protected void handleQuestion(IReplyCallback event) {
        if (event instanceof IModalCallback modalCallback) {
            TextInput.Builder descriptionInputBuilder = TextInput.create(getDiscordID("1-1"), "Travail à faire", TextInputStyle.PARAGRAPH);
            descriptionInputBuilder.setPlaceholder("Description du devoir à effectuer");
            descriptionInputBuilder.setMinLength(1);
            if (form.getDescription() != null && !form.getDescription().isEmpty()) {
                descriptionInputBuilder.setValue(form.getDescription());
            }

            Modal.Builder modalBuilder = Modal.create(getDiscordID("1"), "Description du devoir");
            modalBuilder.addActionRow(descriptionInputBuilder.build());

            modalCallback.replyModal(modalBuilder.build()).queue();
        } else {
            questionError(event, "Impossible de créer le menu d'interaction");
        }
    }

    @Override
    protected void handleResponse(GenericInteractionCreateEvent event) {
        if (event instanceof ModalInteractionEvent interactionEvent) {
            String customID = getCustomID(interactionEvent.getModalId());
            if (customID.equals("1")) {
                ModalMapping description = interactionEvent.getValue(getDiscordID("1-1"));
                form.setDescription(description.getAsString());

                // Go to next step
                DevoirConfirmFormStep devoirConfirmFormStep = new DevoirConfirmFormStep(form);
                form.addStep(devoirConfirmFormStep);
            } else {
                replyError(event, "Une erreur est survenue");
            }
        } else {
            replyError(event, "Une erreur est survenue");
        }
    }

    @Override
    protected void clean() {

    }
}
