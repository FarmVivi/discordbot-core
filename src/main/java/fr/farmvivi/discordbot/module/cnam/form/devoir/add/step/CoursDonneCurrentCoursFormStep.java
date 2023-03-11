package fr.farmvivi.discordbot.module.cnam.form.devoir.add.step;

import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.form.devoir.DevoirForm;
import fr.farmvivi.discordbot.module.forms.Form;
import fr.farmvivi.discordbot.module.forms.FormStep;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CoursDonneCurrentCoursFormStep extends FormStep {
    private final Form form;
    private final DevoirForm devoirForm;

    private List<Cours> coursList;

    private InteractionHook tempHook;

    public CoursDonneCurrentCoursFormStep(Form form, DevoirForm devoirForm) {
        super(form);

        this.form = form;
        this.devoirForm = devoirForm;
    }

    @Override
    protected void handleQuestion(IReplyCallback event) {
        try {
            // Data
            coursList = devoirForm.getCoursDAO().selectAllByDateBetweenHeure(LocalDate.now(), LocalTime.now());
            if (coursList.isEmpty()) {
                coursList = devoirForm.getCoursDAO().selectAllByDateBetweenHeure(LocalDate.now(), LocalTime.now().minusMinutes(10));
                if (coursList.isEmpty()) {
                    EnseignementChooserFormStep enseignementChooserFormStep = new EnseignementChooserFormStep(form, devoirForm);
                    form.addStep(enseignementChooserFormStep);
                    skipStep(event);
                    return;
                }
            }

            // Message
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();

            // Message content
            messageBuilder.addContent("Le devoir a-t-il été donné pendant le cours qui vient d'avoir lieu ?");
            for (Cours cours : coursList) {
                messageBuilder.addContent("\n" + cours.getHeureDebut() + " - " + cours.getHeureFin() + " : "
                        + devoirForm.getEnseignementDAO().selectById(cours.getEnseignementCode()) + " par "
                        + devoirForm.getEnseignantDAO().selectById(cours.getEnseignantId()));
            }

            // Buttons
            Button oui = Button.primary(getDiscordID("1-1"), "Oui");
            Button non = Button.secondary(getDiscordID("1-2"), "Non");
            Button cancel = Button.danger(getDiscordID("2"), "Annuler");
            messageBuilder.addActionRow(oui, non);
            messageBuilder.addActionRow(cancel);

            // Send message
            event.reply(messageBuilder.build()).setEphemeral(true).queue(interactionHook -> tempHook = interactionHook);
        } catch (SQLException e) {
            questionError(event, "Une erreur est survenue");
        }
    }

    @Override
    protected void handleResponse(GenericInteractionCreateEvent event) {
        try {
            if (event instanceof ButtonInteractionEvent interactionEvent) {
                String customID = getCustomID(interactionEvent.getButton().getId());
                switch (customID) {
                    case "1-1" -> {
                        // Oui
                        if (coursList.size() == 1) {
                            devoirForm.setEnseignement(devoirForm.getEnseignementDAO().selectById(coursList.get(0).getEnseignementCode()));
                            devoirForm.setEnseignant(devoirForm.getEnseignantDAO().selectById(coursList.get(0).getEnseignantId()));
                            devoirForm.setCoursDonne(coursList.get(0));

                            // Next step
                            CoursPourChooserFormStep coursPourChooserFormStep = new CoursPourChooserFormStep(form, devoirForm);
                            form.addStep(coursPourChooserFormStep);
                        } else {
                            CoursDonneChooserFormStep coursDonneChooserFormStep = new CoursDonneChooserFormStep(form, devoirForm, coursList);
                            form.addStep(coursDonneChooserFormStep);
                        }
                    }
                    case "1-2" -> {
                        // Non
                        EnseignementChooserFormStep enseignementChooserFormStep = new EnseignementChooserFormStep(form, devoirForm);
                        form.addStep(enseignementChooserFormStep);
                    }
                    case "2" ->
                        // Annuler
                            form.cancel();
                    default -> replyError(event, "Une erreur est survenue");
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
