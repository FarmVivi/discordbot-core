package fr.farmvivi.discordbot.module.cnam.form.devoir.add.step;

import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.form.devoir.add.AddDevoirForm;
import fr.farmvivi.discordbot.module.forms.FormStep;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class CoursDonneChooserFormStep extends FormStep {
    private final AddDevoirForm form;
    private final List<Cours> coursList;

    private InteractionHook tempHook;

    public CoursDonneChooserFormStep(AddDevoirForm form, List<Cours> coursList) {
        super(form);

        this.form = form;
        this.coursList = coursList;
    }

    @Override
    protected void handleQuestion(IReplyCallback event) {
        // Message
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder();

        // Message content
        messageBuilder.addContent("Pendant quel cours le devoir a-t-il été donné ?");

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(getDiscordID("1"));

        // Placeholder
        menuBuilder.setPlaceholder("Choisissez le cours dans lequel le devoir a été donné");

        // Options
        menuBuilder.setRequiredRange(0, 1);
        for (int i = 0; i < coursList.size(); i++) {
            Cours cours = coursList.get(i);
            // Label = Lundi 27/09/2021 de 10:00 à 11:00
            String label = dateToString(cours.getDate());
            label += " de " + horaireToString(cours.getHeureDebut(), cours.getHeureFin());
            menuBuilder.addOption(label, getDiscordID("1-" + i));
        }

        // Add menu to message
        messageBuilder.addActionRow(menuBuilder.build());

        // Cancel button
        Button cancelButton = Button.danger(getDiscordID("2"), "Annuler");

        // Add button to message
        messageBuilder.addActionRow(cancelButton);

        // Send message
        event.reply(messageBuilder.build()).setEphemeral(true).queue(interactionHook -> tempHook = interactionHook);
    }

    @Override
    protected void handleResponse(GenericInteractionCreateEvent event) {
        if (event instanceof StringSelectInteractionEvent interactionEvent) {
            List<SelectOption> selectedOptions = interactionEvent.getSelectedOptions();
            if (selectedOptions.size() == 1) {
                // Get selected cours
                int coursIndex = Integer.parseInt(getCustomID(selectedOptions.get(0).getValue()).split("-")[1]);
                Cours cours = coursList.get(coursIndex);
                form.setCoursDonne(cours);

                // Go to next step
                CoursPourChooserFormStep coursPourChooserFormStep = new CoursPourChooserFormStep(form);
                form.addStep(coursPourChooserFormStep);
            } else {
                // Annuler
                form.cancel();
            }
        } else if (event instanceof ButtonInteractionEvent interactionEvent) {
            String customID = getCustomID(interactionEvent.getButton().getId());
            if (customID.equals("2")) {
                // Annuler
                form.cancel();
            } else {
                responseError(event, "Une erreur est survenue");
            }
        } else {
            responseError(event, "Une erreur est survenue");
        }
    }

    @Override
    protected void clean() {
        if (tempHook != null) {
            tempHook.deleteOriginal().queue();
        }
    }

    private String dateToString(LocalDate date) {
        // Lundi 1er janvier 2021
        String dateStr = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        dateStr = dateStr.substring(0, 1).toUpperCase() + dateStr.substring(1);
        dateStr += " " + date.getDayOfMonth();
        dateStr += date.getDayOfMonth() == 1 ? "er" : "";
        dateStr += " " + date.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        dateStr += " " + date.getYear();

        return dateStr;
    }

    private String horaireToString(LocalTime from, LocalTime to) {
        // 8h30 à 10h30
        String horaireStr = from.format(new DateTimeFormatterBuilder().appendPattern("H'h'mm").toFormatter());
        horaireStr += " à " + to.format(new DateTimeFormatterBuilder().appendPattern("H'h'mm").toFormatter());

        return horaireStr;
    }
}
