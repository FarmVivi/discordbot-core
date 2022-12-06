package fr.farmvivi.discordbot.module.cnam;

import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.cours.CoursDAO;
import fr.farmvivi.discordbot.module.cnam.database.devoir.Devoir;
import fr.farmvivi.discordbot.module.cnam.database.devoir.DevoirDAO;
import fr.farmvivi.discordbot.module.cnam.database.devoirutilisateur.DevoirUtilisateur;
import fr.farmvivi.discordbot.module.cnam.database.devoirutilisateur.DevoirUtilisateurDAO;
import fr.farmvivi.discordbot.module.cnam.database.discorduser.DiscordUser;
import fr.farmvivi.discordbot.module.cnam.database.discorduser.DiscordUserDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.EnseignantDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.EnseignementDAO;
import fr.farmvivi.discordbot.module.cnam.database.utilisateur.Utilisateur;
import fr.farmvivi.discordbot.module.cnam.database.utilisateur.UtilisateurDAO;
import fr.farmvivi.discordbot.module.cnam.events.DevoirListener;
import fr.farmvivi.discordbot.module.cnam.events.devoir.DevoirCreateEvent;
import fr.farmvivi.discordbot.module.cnam.events.devoir.DevoirRemoveEvent;
import fr.farmvivi.discordbot.module.cnam.events.devoir.DevoirUpdateEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.Locale;

public class DevoirEventHandler extends ListenerAdapter implements DevoirListener {
    private final TextChannel todoChannel;
    private final TextChannel alertChannel;

    private final UtilisateurDAO utilisateurDAO;
    private final DiscordUserDAO discordUserDAO;
    private final DevoirDAO devoirDAO;
    private final DevoirUtilisateurDAO devoirUtilisateurDAO;
    private final CoursDAO coursDAO;
    private final EnseignementDAO enseignementDAO;
    private final EnseignantDAO enseignantDAO;

    public DevoirEventHandler(CnamModule module, TextChannel todoChannel, TextChannel alertChannel) {
        this.todoChannel = todoChannel;
        this.alertChannel = alertChannel;

        this.utilisateurDAO = new UtilisateurDAO(module.getDatabaseManager().getDatabaseAccess());
        this.discordUserDAO = new DiscordUserDAO(module.getDatabaseManager().getDatabaseAccess());
        this.devoirDAO = new DevoirDAO(module.getDatabaseManager().getDatabaseAccess());
        this.devoirUtilisateurDAO = new DevoirUtilisateurDAO(module.getDatabaseManager().getDatabaseAccess());
        this.coursDAO = new CoursDAO(module.getDatabaseManager().getDatabaseAccess());
        this.enseignementDAO = new EnseignementDAO(module.getDatabaseManager().getDatabaseAccess());
        this.enseignantDAO = new EnseignantDAO(module.getDatabaseManager().getDatabaseAccess());
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        // Devoir
        long messageId = event.getMessageIdLong();
        Devoir devoir;
        try {
            devoir = devoirDAO.selectByDiscordMessageId(messageId);
            if (devoir == null)
                return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Utilisateur
        User user = event.getUser();
        if (user == null || user.isBot())
            return;

        try {
            DiscordUser discordUser = discordUserDAO.selectById(user.getIdLong());
            if (discordUser == null) {
                discordUser = discordUserDAO.create(new DiscordUser(user.getIdLong()));
            }
            Utilisateur utilisateur = utilisateurDAO.selectByDiscordId(discordUser.getId());
            if (utilisateur == null) {
                utilisateur = new Utilisateur();
                utilisateur.setDiscordId(discordUser.getId());
                utilisateur = utilisateurDAO.create(utilisateur);
            }
            DevoirUtilisateur devoirUtilisateur = devoirUtilisateurDAO.selectByIdDevoirAndIdUtilisateur(devoir.getId(), utilisateur.getId());
            if (devoirUtilisateur == null) {
                devoirUtilisateur = new DevoirUtilisateur(devoir.getId(), utilisateur.getId(), LocalDateTime.now());
                devoirUtilisateurDAO.create(devoirUtilisateur);
            }
        } catch (SQLException e) {
            event.getReaction().removeReaction(user).queue();
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        // Devoir
        long messageId = event.getMessageIdLong();
        Devoir devoir;
        try {
            devoir = devoirDAO.selectByDiscordMessageId(messageId);
            if (devoir == null)
                return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Utilisateur
        User user = event.getUser();
        if (user == null || user.isBot())
            return;

        try {
            DiscordUser discordUser = discordUserDAO.selectById(user.getIdLong());
            if (discordUser == null) {
                discordUser = discordUserDAO.create(new DiscordUser(user.getIdLong()));
            }
            Utilisateur utilisateur = utilisateurDAO.selectByDiscordId(discordUser.getId());
            if (utilisateur == null) {
                utilisateur = new Utilisateur();
                utilisateur.setDiscordId(discordUser.getId());
                utilisateur = utilisateurDAO.create(utilisateur);
            }
            DevoirUtilisateur devoirUtilisateur = devoirUtilisateurDAO.selectByIdDevoirAndIdUtilisateur(devoir.getId(), utilisateur.getId());
            if (devoirUtilisateur != null) {
                devoirUtilisateurDAO.delete(devoirUtilisateur);
            }
        } catch (SQLException e) {
            event.getReaction().removeReaction(user).queue();
        }
    }

    @Override
    public void onMessageReactionRemoveAll(MessageReactionRemoveAllEvent event) {
        // Devoir
        long messageId = event.getMessageIdLong();
        Devoir devoir;
        try {
            devoir = devoirDAO.selectByDiscordMessageId(messageId);
            if (devoir == null)
                return;
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try {
            devoirUtilisateurDAO.deleteByIdDevoir(devoir.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReactionRemoveEmoji(MessageReactionRemoveEmojiEvent event) {
        super.onMessageReactionRemoveEmoji(event);
    }

    @Override
    public void onDevoirCreate(DevoirCreateEvent event) {
        try {
            Devoir devoir = event.getDevoir();

            // UE
            Enseignement enseignement = enseignementDAO.selectById(devoir.getCodeEnseignement());

            // Enseignant
            Enseignant enseignant;
            {
                Cours cours;
                if (devoir.getIdCoursPour() != null) {
                    cours = coursDAO.selectById(devoir.getIdCoursPour());
                } else {
                    cours = coursDAO.selectById(devoir.getIdCoursDonne());
                }
                enseignant = enseignantDAO.selectById(cours.getEnseignantId());
            }

            // Pour le
            LocalDate datePour;
            if (devoir.getIdCoursPour() != null) {
                datePour = coursDAO.selectById(devoir.getIdCoursPour()).getDate();
            } else {
                datePour = devoir.getDatePour();
            }

            // Donné le
            LocalDate dateDonne = coursDAO.selectById(devoir.getIdCoursDonne()).getDate();

            // Message
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();

            // Embed
            EmbedBuilder embedBuilder = new EmbedBuilder();

            // Embed content
            embedBuilder.setTitle(dateToShortString(datePour) + " - " + enseignement.getCode() + (devoir.isOptionnel() ? " (facultatif)" : ""));
            embedBuilder.setColor(new Color(109, 224, 16));

            // UE
            embedBuilder.addField("UE", enseignement.toString(), false);

            // Enseignant
            embedBuilder.addField("Enseignant", enseignant.toString(), false);

            // Description
            embedBuilder.addField("Description", devoir.getDescription(), false);

            // Pour le
            embedBuilder.addField("Pour le", dateToString(datePour), false);

            // Donné le
            embedBuilder.addField("Donné le", dateToString(dateDonne), false);

            // Add embed to message
            messageBuilder.addEmbeds(embedBuilder.build());

            // Send message
            todoChannel.sendMessage(messageBuilder.build()).queue(message -> {
                // Add white_check_mark reaction
                Emoji whiteCheckMark = Emoji.fromUnicode("✅");
                message.addReaction(whiteCheckMark).queue();
                devoir.setDiscordMessageId(message.getIdLong());
                try {
                    devoirDAO.update(devoir);
                } catch (SQLException e) {
                    todoChannel.sendMessage("Une erreur est survenue lors de la mise à jour du devoir.").queue();
                }
            });
        } catch (SQLException e) {
            todoChannel.sendMessage("Une erreur est survenue lors de l'affichage d'un devoir.").queue();
        }
    }

    @Override
    public void onDevoirRemove(DevoirRemoveEvent event) {

    }

    @Override
    public void onDevoirUpdate(DevoirUpdateEvent event) {

    }

    private String dateToShortString(LocalDate date) {
        return date.format(new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter());
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
