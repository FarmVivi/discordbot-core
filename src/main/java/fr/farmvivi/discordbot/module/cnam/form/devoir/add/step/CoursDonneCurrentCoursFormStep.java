package fr.farmvivi.discordbot.module.cnam.form.devoir.add.step;

import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.form.devoir.add.AddDevoirForm;
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
    private final AddDevoirForm form;

    private List<Cours> coursList;

    private InteractionHook tempHook;

    public CoursDonneCurrentCoursFormStep(AddDevoirForm form) {
        super(form);

        this.form = form;
    }

    @Override
    protected void handleQuestion(IReplyCallback event) {
        try {
            // Data
            coursList = form.getCoursDAO().selectAllByDateBetweenHeure(LocalDate.now(), LocalTime.now());
            if (coursList.isEmpty()) {
                coursList = form.getCoursDAO().selectAllByDateBetweenHeure(LocalDate.now(), LocalTime.now().minusMinutes(10));
                if (coursList.isEmpty()) {
                    EnseignementChooserFormStep enseignementChooserFormStep = new EnseignementChooserFormStep(form);
                    form.addStep(enseignementChooserFormStep);
                    skipStep(event);
                    return;
                }
            }

            // Message
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();

            // Message content
            messageBuilder.addContent("Le devoir que vous voulez ajouter à t-il été donné pendant un cours qui vient d'avoir lieu ?");
            for (Cours cours : coursList) {
                messageBuilder.addContent("\n" + cours.getHeureDebut() + " - " + cours.getHeureFin() + " : "
                        + form.getEnseignementDAO().selectById(cours.getEnseignementCode()) + " par "
                        + form.getEnseignantDAO().selectById(cours.getEnseignantId()));
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
                    case "1-1":
                        // Oui
                        if (coursList.size() == 1) {
                            form.setEnseignement(form.getEnseignementDAO().selectById(coursList.get(0).getEnseignementCode()));
                            form.setEnseignant(form.getEnseignantDAO().selectById(coursList.get(0).getEnseignantId()));
                            form.setCoursDonne(coursList.get(0));

                            // Next step
                            CoursPourChooserFormStep coursPourChooserFormStep = new CoursPourChooserFormStep(form);
                            form.addStep(coursPourChooserFormStep);
                        } else {
                            CoursDonneChooserFormStep coursDonneChooserFormStep = new CoursDonneChooserFormStep(form, coursList);
                            form.addStep(coursDonneChooserFormStep);
                        }
                        break;
                    case "1-2":
                        // Non
                        EnseignementChooserFormStep enseignementChooserFormStep = new EnseignementChooserFormStep(form);
                        form.addStep(enseignementChooserFormStep);
                        break;
                    case "2":
                        // Annuler
                        form.cancel();
                        break;
                    default:
                        responseError(event, "Une erreur est survenue");
                        break;
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
