package fr.farmvivi.discordbot.module.cnam.form.devoir.add.step;

import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
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
import java.util.List;

public class EnseignementChooserFormStep extends FormStep {
    private final AddDevoirForm form;

    private List<Enseignement> enseignementList;
    private InteractionHook tempHook;

    public EnseignementChooserFormStep(AddDevoirForm form) {
        super(form);

        this.form = form;
    }

    @Override
    protected void handleQuestion(IReplyCallback event) {
        try {
            // Data
            enseignementList = form.getEnseignementDAO().selectAll();

            // Message
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();

            // Message content
            messageBuilder.addContent("Quel est l'enseignement du devoir ?");

            StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(getDiscordID("1"));

            // Placeholder
            menuBuilder.setPlaceholder("Choisissez l'enseignement dans lequel le devoir a été donné");

            // Options
            menuBuilder.setRequiredRange(0, 1);
            for (int i = 0; i < enseignementList.size(); i++) {
                Enseignement enseignement = enseignementList.get(i);
                menuBuilder.addOption(enseignement.toString(), getDiscordID("1-" + i));
            }

            // Add menu to message
            messageBuilder.addActionRow(menuBuilder.build());

            // Cancel button
            Button cancelButton = Button.danger(getDiscordID("2"), "Annuler");

            // Add button to message
            messageBuilder.addActionRow(cancelButton);

            // Send message
            event.reply(messageBuilder.build()).setEphemeral(true).queue(interactionHook -> tempHook = interactionHook);
        } catch (SQLException e) {
            questionError(event, "Une erreur est survenue");
        }
    }

    @Override
    protected void handleResponse(GenericInteractionCreateEvent event) {
        try {
            if (event instanceof StringSelectInteractionEvent interactionEvent) {
                List<SelectOption> selectedOptions = interactionEvent.getSelectedOptions();
                if (selectedOptions.size() == 1) {
                    // Get selected enseignement
                    int enseignementIndex = Integer.parseInt(getCustomID(selectedOptions.get(0).getValue()).split("-")[1]);
                    Enseignement enseignement = enseignementList.get(enseignementIndex);
                    form.setEnseignement(enseignement);

                    List<Enseignant> enseignantList = form.getEnseignantDAO().selectAllByEnseignement(enseignement.getCode());

                    if (enseignantList.isEmpty()) {
                        responseError(event, "Aucun enseignant n'a été trouvé pour cet enseignement");
                        return;
                    }

                    EnseignantChooserFormStep enseignantChooserFormStep = new EnseignantChooserFormStep(form, enseignantList);
                    form.addStep(enseignantChooserFormStep);
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
        } catch (SQLException e) {
            responseError(event, "Une erreur est survenue");
        }
    }

    @Override
    protected void clean() {
        if (tempHook != null) {
            tempHook.deleteOriginal().queue();
        }
    }
}
