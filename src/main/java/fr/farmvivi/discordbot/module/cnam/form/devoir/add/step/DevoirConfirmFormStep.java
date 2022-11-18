package fr.farmvivi.discordbot.module.cnam.form.devoir.add.step;

import fr.farmvivi.discordbot.module.cnam.form.devoir.add.AddDevoirForm;
import fr.farmvivi.discordbot.module.forms.FormStep;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.Locale;

public class DevoirConfirmFormStep extends FormStep {
    private final AddDevoirForm form;

    private InteractionHook tempHook;

    public DevoirConfirmFormStep(AddDevoirForm form) {
        super(form);

        this.form = form;
    }

    @Override
    protected void handleQuestion(IReplyCallback event) {
        try {
            // Message
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();

            // Message content
            messageBuilder.addContent("Les informations ci-dessous sont-elles correctes ?");

            // Embed
            EmbedBuilder embedBuilder = new EmbedBuilder();

            // Embed content
            embedBuilder.setTitle("Ajout d'un devoir");
            embedBuilder.setColor(new Color(109, 224, 16));

            // Description
            embedBuilder.addField("Travail à faire", form.getDescription(), false);

            // Cours donné
            String coursDonneInfos = "Date : " + dateToString(form.getCoursDonne().getDate());
            coursDonneInfos += "\nHoraire : " + horaireToString(form.getCoursDonne().getHeureDebut(), form.getCoursDonne().getHeureFin());
            coursDonneInfos += "\nEnseignant : " + form.getEnseignantDAO().selectById(form.getCoursDonne().getEnseignantId());
            embedBuilder.addField("Cours donné", coursDonneInfos, true);

            // Cours pour
            String coursPourInfos = "Date : " + dateToString(form.getCoursPour().getDate());
            if (form.getCoursPour() != null) {
                coursPourInfos += "\nHoraire : " + horaireToString(form.getCoursPour().getHeureDebut(), form.getCoursPour().getHeureFin());
            }
            coursPourInfos += "\nEnseignant : " + form.getEnseignant();
            embedBuilder.addField("Cours pour", coursPourInfos, true);

            embedBuilder.setFooter(form.getEnseignement().toString());

            // Add embed to message
            messageBuilder.addEmbeds(embedBuilder.build());

            // Buttons
            Button confirm = Button.success(getDiscordID("1-1"), "Oui");
            Button changeCoursDonne = Button.secondary(getDiscordID("1-2"), "Changer le cours donné");
            Button changeCoursPour = Button.secondary(getDiscordID("1-3"), "Changer le cours pour");
            Button changeDescription = Button.secondary(getDiscordID("1-4"), "Changer la description");

            messageBuilder.addActionRow(confirm, changeCoursDonne, changeCoursPour, changeDescription);

            // Cancel button
            Button cancel = Button.danger(getDiscordID("2"), "Annuler");

            messageBuilder.addActionRow(cancel);

            // Send message
            event.reply(messageBuilder.build()).setEphemeral(true).queue(hook -> tempHook = hook);
        } catch (SQLException e) {
            questionError(event, "Une erreur est survenue");
        }
    }

    @Override
    protected void handleResponse(GenericInteractionCreateEvent event) {
        if (event instanceof ButtonInteractionEvent interactionEvent) {
            String customId = getCustomID(interactionEvent.getButton().getId());
            if (customId.startsWith("1-")) {
                switch (customId) {
                    case "1-1":
                        // OK !
                        break;
                    case "1-2":
                        CoursDonneCurrentCoursFormStep coursDonneCurrentCoursFormStep = new CoursDonneCurrentCoursFormStep(form);
                        form.addStep(coursDonneCurrentCoursFormStep);
                        break;
                    case "1-3":
                        CoursPourChooserFormStep coursPourChooserFormStep = new CoursPourChooserFormStep(form);
                        form.addStep(coursPourChooserFormStep);
                        break;
                    case "1-4":
                        DescriptionFormStep descriptionFormStep = new DescriptionFormStep(form);
                        form.addStep(descriptionFormStep);
                        break;
                    default:
                        responseError(event, "Une erreur est survenue");
                        break;
                }
            } else if (customId.equals("2")) {
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
