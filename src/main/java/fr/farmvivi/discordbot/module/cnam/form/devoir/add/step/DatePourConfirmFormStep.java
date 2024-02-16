package fr.farmvivi.discordbot.module.cnam.form.devoir.add.step;

import fr.farmvivi.discordbot.module.cnam.form.devoir.DevoirForm;
import fr.farmvivi.discordbot.module.forms.Form;
import fr.farmvivi.discordbot.module.forms.FormStep;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.Locale;

public class DatePourConfirmFormStep extends FormStep {
    private final Form form;
    private final DevoirForm devoirForm;

    private InteractionHook tempHook;

    public DatePourConfirmFormStep(Form form, DevoirForm devoirForm) {
        super(form);

        this.form = form;
        this.devoirForm = devoirForm;
    }

    @Override
    protected void handleQuestion(IReplyCallback event) {
        // Message
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder();

        // Message content
        String date = devoirForm.getDatePour().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        date += " " + new DateTimeFormatterBuilder().appendPattern("d MMMM yyyy").toFormatter().format(devoirForm.getDatePour());
        messageBuilder.addContent("Le devoir devra être rendu pour le " + date);

        if (devoirForm.getCoursPour() != null) {
            String horaires = "de " + new DateTimeFormatterBuilder().appendPattern("HH'h'mm").toFormatter().format(devoirForm.getCoursPour().getDebutCours().toLocalTime());
            horaires += " à " + new DateTimeFormatterBuilder().appendPattern("HH'h'mm").toFormatter().format(devoirForm.getCoursPour().getFinCours().toLocalTime());
            messageBuilder.addContent(" " + horaires);
        }

        messageBuilder.addContent("\n\nEst-ce correct ?");

        // Buttons
        Button oui = Button.success(getDiscordID("1-1"), "Oui");
        Button non = Button.danger(getDiscordID("1-2"), "Non");

        // Add buttons
        messageBuilder.addActionRow(oui, non);

        // Cancel
        Button cancel = Button.danger(getDiscordID("2"), "Annuler");

        // Add cancel button
        messageBuilder.addActionRow(cancel);

        // Send message
        event.reply(messageBuilder.build()).setEphemeral(true).queue(interactionHook -> tempHook = interactionHook);
    }

    @Override
    protected void handleResponse(GenericInteractionCreateEvent event) {
        if (event instanceof ButtonInteractionEvent interactionEvent) {
            String customID = getCustomID(interactionEvent.getButton().getId());
            if (customID.startsWith("1-")) {
                if (customID.equals("1-1")) {
                    // Oui
                    if (devoirForm.getDescription() == null || devoirForm.getDescription().isEmpty()) {
                        // Go to next step
                        DescriptionFormStep descriptionFormStep = new DescriptionFormStep(form, devoirForm);
                        form.addStep(descriptionFormStep);
                    } else {
                        // Go to next step
                        DevoirConfirmFormStep devoirConfirmFormStep = new DevoirConfirmFormStep(form, devoirForm);
                        form.addStep(devoirConfirmFormStep);
                    }
                } else if (customID.equals("1-2")) {
                    // Non
                    CoursPourChooserFormStep coursPourChooserFormStep = new CoursPourChooserFormStep(form, devoirForm);
                    form.addStep(coursPourChooserFormStep);
                }
            } else if (customID.equals("2")) {
                // Annuler
                form.cancel();
            }
        }
    }

    @Override
    protected void clean() {
        if (tempHook != null) {
            tempHook.deleteOriginal().queue();
        }
    }
}
