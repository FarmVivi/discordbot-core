package fr.farmvivi.discordbot.module.cnam.form.devoir.add.step;

import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
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
import java.util.Collections;
import java.util.List;

public class EnseignantChooserFormStep extends FormStep {
    private final AddDevoirForm form;
    private final List<Enseignant> enseignantList;

    private InteractionHook tempHook;

    public EnseignantChooserFormStep(AddDevoirForm form, List<Enseignant> enseignantList) {
        super(form);

        this.form = form;
        this.enseignantList = enseignantList;
    }

    @Override
    protected void handleQuestion(IReplyCallback event) {
        if (enseignantList.size() == 1) {
            try {
                nextStep((GenericInteractionCreateEvent) event, enseignantList.get(0));
                skipStep(event);
            } catch (SQLException e) {
                questionError(event, "Une erreur est survenue");
            }
            return;
        }

        // Message
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder();

        // Message content
        messageBuilder.addContent("Quel est l'enseignant qui a donné le devoir ?");

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(getDiscordID("1"));

        // Placeholder
        menuBuilder.setPlaceholder("Choisissez l'enseignant qui a donné le devoir");

        // Options
        menuBuilder.setRequiredRange(0, 1);
        for (int i = 0; i < enseignantList.size(); i++) {
            Enseignant enseignant = enseignantList.get(i);
            menuBuilder.addOption(enseignant.toString(), getDiscordID("1-" + i));
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
        try {
            if (event instanceof StringSelectInteractionEvent interactionEvent) {
                List<SelectOption> selectedOptions = interactionEvent.getSelectedOptions();
                if (selectedOptions.size() == 1) {
                    // Get selected enseignant
                    int enseignantIndex = Integer.parseInt(getCustomID(selectedOptions.get(0).getValue()).split("-")[1]);
                    Enseignant enseignant = enseignantList.get(enseignantIndex);

                    nextStep(event, enseignant);
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
                    replyError(event, "Une erreur est survenue");
                }
            } else {
                replyError(event, "Une erreur est survenue");
            }
        } catch (SQLException e) {
            replyError(event, "Une erreur est survenue");
        }
    }

    @Override
    protected void clean() {
        if (tempHook != null) {
            tempHook.deleteOriginal().queue();
        }
    }

    private void nextStep(GenericInteractionCreateEvent event, Enseignant enseignant) throws SQLException {
        form.setEnseignant(enseignant);

        List<Cours> coursList = form.getCoursDAO().selectAllByEnseignementEnseignant(form.getEnseignement().getCode(), enseignant.getId());
        Collections.sort(coursList);

        // Remove all cours with date after today
        coursList.removeIf(cours -> cours.getDate().isAfter(LocalDate.now()));

        // Limit cours list to 25 last cours
        if (coursList.size() > 25) {
            coursList = coursList.subList(coursList.size() - 25, coursList.size());
        }

        if (coursList.isEmpty()) {
            replyError(event, "Aucun cours n'a été trouvé pour cet enseignement");
            return;
        }

        CoursDonneChooserFormStep coursDonneChooserFormStep = new CoursDonneChooserFormStep(form, coursList);
        form.addStep(coursDonneChooserFormStep);
    }
}
