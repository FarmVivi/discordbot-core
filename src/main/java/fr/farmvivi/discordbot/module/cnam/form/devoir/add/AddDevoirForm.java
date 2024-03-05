package fr.farmvivi.discordbot.module.cnam.form.devoir.add;

import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.cnam.database.DatabaseManager;
import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.cours.CoursDAO;
import fr.farmvivi.discordbot.module.cnam.database.devoir.Devoir;
import fr.farmvivi.discordbot.module.cnam.database.devoir.DevoirDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.EnseignantDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.EnseignementDAO;
import fr.farmvivi.discordbot.module.cnam.events.devoir.DevoirCreateEvent;
import fr.farmvivi.discordbot.module.cnam.form.devoir.DevoirForm;
import fr.farmvivi.discordbot.module.cnam.form.devoir.add.step.CoursDonneCurrentCoursFormStep;
import fr.farmvivi.discordbot.module.forms.Form;
import fr.farmvivi.discordbot.module.forms.FormsModule;
import fr.farmvivi.discordbot.utils.event.IEventManager;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.sql.SQLException;
import java.time.LocalDate;

public class AddDevoirForm extends Form implements DevoirForm {
    private final IEventManager eventManager;
    private final FormsModule forms;

    private final DevoirDAO devoirDAO;
    private final CoursDAO coursDAO;
    private final EnseignementDAO enseignementDAO;
    private final EnseignantDAO enseignantDAO;

    private Enseignement enseignement;
    private Cours coursDonne;
    private Cours coursPour;
    private LocalDate datePour;
    private Enseignant enseignant;
    private String description;

    public AddDevoirForm(DatabaseManager databaseManager, IEventManager eventManager, FormsModule forms) {
        this.eventManager = eventManager;
        this.forms = forms;

        // DAOs
        devoirDAO = new DevoirDAO(databaseManager.getDatabaseAccess());
        coursDAO = new CoursDAO(databaseManager.getDatabaseAccess());
        enseignementDAO = new EnseignementDAO(databaseManager.getDatabaseAccess());
        enseignantDAO = new EnseignantDAO(databaseManager.getDatabaseAccess());

        // Steps
        addStep(new CoursDonneCurrentCoursFormStep(this, this));
    }

    @Override
    public void start(IReplyCallback replyCallback) {
        forms.registerForm(Modules.CNAM, this);
        nextStep(replyCallback);
    }

    @Override
    protected void finish(IReplyCallback event) {
        forms.unregisterForm(Modules.CNAM, this);

        if (isCancelled()) {
            event.reply("> :x: Le formulaire a été annulé.").setEphemeral(true).queue();
            return;
        }

        if (enseignement == null) {
            event.reply("> :x: L'enseignement n'a pas été renseigné.").setEphemeral(true).queue();
            return;
        }

        if (coursDonne == null) {
            event.reply("> :x: Le cours donné n'a pas été renseigné.").setEphemeral(true).queue();
            return;
        }

        if (enseignant == null) {
            event.reply("> :x: L'enseignant n'a pas été renseigné.").setEphemeral(true).queue();
            return;
        }

        if (datePour == null) {
            event.reply("> :x: La date pour laquelle le devoir est à faire n'a pas été renseignée.").setEphemeral(true).queue();
            return;
        }

        if (description == null) {
            event.reply("> :x: La description n'a pas été renseignée.").setEphemeral(true).queue();
            return;
        }

        Devoir devoir = new Devoir(datePour, description, false, enseignant.getId(), enseignement.getCode(), coursDonne.getId());
        devoir.setIdCoursPour(coursPour != null ? coursPour.getId() : null);

        try {
            devoir = devoirDAO.create(devoir);
            DevoirCreateEvent devoirCreateEvent = new DevoirCreateEvent(devoir);
            eventManager.handleAsync(devoirCreateEvent);
            event.reply("> :white_check_mark: Le devoir a été ajouté.").setEphemeral(true).queue();
        } catch (SQLException e) {
            event.reply("> :x: Une erreur est survenue lors de l'ajout du devoir.").setEphemeral(true).queue();
            throw new RuntimeException(e);
        }
    }

    @Override
    public DevoirDAO getDevoirDAO() {
        return devoirDAO;
    }

    @Override
    public CoursDAO getCoursDAO() {
        return coursDAO;
    }

    @Override
    public EnseignantDAO getEnseignantDAO() {
        return enseignantDAO;
    }

    @Override
    public EnseignementDAO getEnseignementDAO() {
        return enseignementDAO;
    }

    @Override
    public Enseignement getEnseignement() {
        return enseignement;
    }

    @Override
    public void setEnseignement(Enseignement enseignement) {
        this.enseignement = enseignement;
    }

    @Override
    public Cours getCoursDonne() {
        return coursDonne;
    }

    @Override
    public void setCoursDonne(Cours coursDonne) {
        this.coursDonne = coursDonne;
    }

    @Override
    public Cours getCoursPour() {
        return coursPour;
    }

    @Override
    public void setCoursPour(Cours coursPour) {
        this.coursPour = coursPour;
    }

    @Override
    public LocalDate getDatePour() {
        return datePour;
    }

    @Override
    public void setDatePour(LocalDate datePour) {
        this.datePour = datePour;
    }

    @Override
    public Enseignant getEnseignant() {
        return enseignant;
    }

    @Override
    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }
}
