package fr.farmvivi.discordbot.module.cnam.form.devoir.add.step;

import fr.farmvivi.discordbot.module.cnam.form.devoir.add.AddDevoirForm;
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
    private final AddDevoirForm form;

    private InteractionHook tempHook;

    public DatePourConfirmFormStep(AddDevoirForm form) {
        super(form);

        this.form = form;
    }

    @Override
    protected void handleQuestion(IReplyCallback event) {
        // Message
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder();

        // Message content
        String date = form.getDatePour().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        date += " " + new DateTimeFormatterBuilder().appendPattern("d MMMM yyyy").toFormatter().format(form.getDatePour());
        messageBuilder.addContent("Le devoir devra être rendu pour le " + date);

        if (form.getCoursPour() != null) {
            String horaires = "de " + new DateTimeFormatterBuilder().appendPattern("HH'h'mm").toFormatter().format(form.getCoursPour().getHeureDebut());
            horaires += " à " + new DateTimeFormatterBuilder().appendPattern("HH'h'mm").toFormatter().format(form.getCoursPour().getHeureFin());
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
                    if (form.getDescription() == null || form.getDescription().isEmpty()) {
                        // Go to next step
                        DescriptionFormStep descriptionFormStep = new DescriptionFormStep(form);
                        form.addStep(descriptionFormStep);
                    } else {
                        // Go to next step
                        DevoirConfirmFormStep devoirConfirmFormStep = new DevoirConfirmFormStep(form);
                        form.addStep(devoirConfirmFormStep);
                    }
                } else if (customID.equals("1-2")) {
                    // Non
                    CoursPourChooserFormStep coursPourChooserFormStep = new CoursPourChooserFormStep(form);
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
