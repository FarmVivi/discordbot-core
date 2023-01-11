package fr.farmvivi.discordbot.module.cnam.form.devoir.add.step;

import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.form.devoir.DevoirForm;
import fr.farmvivi.discordbot.module.forms.Form;
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
    private final Form form;
    private final DevoirForm devoirForm;

    private List<Enseignement> enseignementList;
    private InteractionHook tempHook;

    public EnseignementChooserFormStep(Form form, DevoirForm devoirForm) {
        super(form);

        this.form = form;
        this.devoirForm = devoirForm;
    }

    @Override
    protected void handleQuestion(IReplyCallback event) {
        try {
            // Data
            enseignementList = devoirForm.getEnseignementDAO().selectAll();

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
                    devoirForm.setEnseignement(enseignement);

                    List<Enseignant> enseignantList = devoirForm.getEnseignantDAO().selectAllByEnseignement(enseignement.getCode());

                    if (enseignantList.isEmpty()) {
                        replyError(event, "Aucun enseignant n'a été trouvé pour cet enseignement");
                        return;
                    }

                    EnseignantChooserFormStep enseignantChooserFormStep = new EnseignantChooserFormStep(form, devoirForm, enseignantList);
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
}
