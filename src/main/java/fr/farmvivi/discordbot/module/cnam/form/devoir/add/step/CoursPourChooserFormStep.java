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

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CoursPourChooserFormStep extends FormStep {
    private final AddDevoirForm form;

    private List<Cours> coursList;
    private InteractionHook tempHook;

    public CoursPourChooserFormStep(AddDevoirForm form) {
        super(form);

        this.form = form;
    }

    @Override
    protected void handleQuestion(IReplyCallback event) {
        try {
            // Message
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();

            // Message content
            messageBuilder.addContent("Quand le devoir doit-il être rendu ?");

            // Buttons
            Button prochainCours = Button.primary(getDiscordID("1-1"), "Prochain cours");
            Button choisirDate = Button.secondary(getDiscordID("1-2"), "Choisir une date");
            choisirDate = choisirDate.asDisabled();

            messageBuilder.addActionRow(prochainCours, choisirDate);

            // Data
            coursList = form.getCoursDAO().selectAllByEnseignementEnseignant(form.getEnseignement().getCode(), form.getEnseignant().getId());

            if (!coursList.isEmpty()) {
                Collections.sort(coursList);

                // Remove all cours with date before today
                coursList.removeIf(cours -> cours.getDate().isBefore(LocalDate.now()) || cours.getDate().isEqual(LocalDate.now()));

                // Limit cours list to 25
                if (coursList.size() > 25) {
                    coursList = coursList.subList(0, 25);
                }

                StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(getDiscordID("2"));

                // Placeholder
                menuBuilder.setPlaceholder("Choisissez le cours pour lequel le devoir doit être rendu");

                // Options
                menuBuilder.setRequiredRange(0, 1);
                for (int i = 0; i < coursList.size(); i++) {
                    Cours cours = coursList.get(i);
                    // Label = Lundi 27/09/2021 - 10:00 à 11:00 - dans 2 jours
                    String label = cours.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRANCE);
                    label = label.substring(0, 1).toUpperCase() + label.substring(1);
                    label += " " + new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter().format(cours.getDate());
                    label += " - " + cours.getHeureDebut().toString() + " à " + cours.getHeureFin().toString();
                    long days = cours.getDate().toEpochDay() - LocalDate.now().toEpochDay();
                    label += " - dans " + days + " jour" + (days > 1 ? "s" : "");
                    menuBuilder.addOption(label, getDiscordID("2-" + i));
                }

                // Add menu to message
                messageBuilder.addActionRow(menuBuilder.build());
            }

            // Cancel
            Button cancel = Button.danger(getDiscordID("3"), "Annuler");

            // Add cancel button to message
            messageBuilder.addActionRow(cancel);

            // Send message
            event.reply(messageBuilder.build()).setEphemeral(true).queue(interactionHook -> tempHook = interactionHook);
        } catch (SQLException e) {
            questionError(event, "Une erreur est survenue");
        }
    }

    @Override
    protected void handleResponse(GenericInteractionCreateEvent event) {
        if (event instanceof StringSelectInteractionEvent interactionEvent) {
            List<SelectOption> selectedOptions = interactionEvent.getSelectedOptions();
            if (selectedOptions.size() == 1) {
                // Get selected cours
                int coursIndex = Integer.parseInt(getCustomID(selectedOptions.get(0).getValue()).split("-")[1]);
                Cours cours = coursList.get(coursIndex);
                form.setDatePour(cours.getDate());
                form.setCoursPour(cours);

                // Go to next step
                DatePourConfirmFormStep datePourConfirmFormStep = new DatePourConfirmFormStep(form);
                form.addStep(datePourConfirmFormStep);
            } else {
                // Annuler
                form.cancel();
            }
        } else if (event instanceof ButtonInteractionEvent interactionEvent) {
            String customID = getCustomID(interactionEvent.getButton().getId());
            if (customID.startsWith("1-")) {
                // Prochain cours
                if (customID.equals("1-1")) {
                    if (!coursList.isEmpty()) {
                        Cours cours = coursList.get(0);
                        form.setDatePour(cours.getDate());
                        form.setCoursPour(cours);

                        // Go to next step
                        DatePourConfirmFormStep datePourConfirmFormStep = new DatePourConfirmFormStep(form);
                        form.addStep(datePourConfirmFormStep);
                    } else {
                        replyError(event, "Aucun cours n'est prévu dans les prochains jours");
                    }
                }
                // Choisir date
                else if (customID.equals("1-2")) {
                    // TODO : Choix de la date
                }
            } else if (customID.equals("3")) {
                // Annuler
                form.cancel();
            } else {
                replyError(event, "Une erreur est survenue");
            }
        } else {
            replyError(event, "Une erreur est survenue");
        }
    }

    @Override
    protected void clean() {
        if (tempHook != null) {
            tempHook.deleteOriginal().queue();
        }
    }
}
