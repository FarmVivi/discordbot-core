package fr.farmvivi.discordbot.module.cnam.form.devoir.add;

import fr.farmvivi.discordbot.module.Modules;
import fr.farmvivi.discordbot.module.cnam.CnamModule;
import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.cours.CoursDAO;
import fr.farmvivi.discordbot.module.cnam.database.devoir.Devoir;
import fr.farmvivi.discordbot.module.cnam.database.devoir.DevoirDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.EnseignantDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.EnseignementDAO;
import fr.farmvivi.discordbot.module.cnam.form.devoir.add.step.CoursDonneCurrentCoursFormStep;
import fr.farmvivi.discordbot.module.forms.Form;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.sql.SQLException;
import java.time.LocalDate;

public class AddDevoirForm extends Form {
    private final CnamModule module;
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

    public AddDevoirForm(CnamModule module) {
        this.module = module;

        // DAOs
        devoirDAO = new DevoirDAO(module.getDatabaseManager().getDatabaseAccess());
        coursDAO = new CoursDAO(module.getDatabaseManager().getDatabaseAccess());
        enseignementDAO = new EnseignementDAO(module.getDatabaseManager().getDatabaseAccess());
        enseignantDAO = new EnseignantDAO(module.getDatabaseManager().getDatabaseAccess());

        // Steps
        addStep(new CoursDonneCurrentCoursFormStep(this));
    }

    @Override
    public void start(IReplyCallback replyCallback) {
        module.getFormsModule().registerForm(Modules.CNAM, this);
        nextStep(replyCallback);
    }

    @Override
    protected void finish(IReplyCallback event) {
        module.getFormsModule().unregisterForm(Modules.CNAM, this);

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

        Devoir devoir = new Devoir(datePour, description, enseignant.getId(), enseignement.getCode(), coursDonne.getId());
        devoir.setIdCoursPour(coursPour != null ? coursPour.getId() : null);

        try {
            devoirDAO.create(devoir);
            event.reply("> :white_check_mark: Le devoir a été ajouté.").setEphemeral(true).queue();
        } catch (SQLException e) {
            event.reply("> :x: Une erreur est survenue lors de l'ajout du devoir.").setEphemeral(true).queue();
            throw new RuntimeException(e);
        }
    }

    public DevoirDAO getDevoirDAO() {
        return devoirDAO;
    }

    public CoursDAO getCoursDAO() {
        return coursDAO;
    }

    public EnseignantDAO getEnseignantDAO() {
        return enseignantDAO;
    }

    public EnseignementDAO getEnseignementDAO() {
        return enseignementDAO;
    }

    public Enseignement getEnseignement() {
        return enseignement;
    }

    public void setEnseignement(Enseignement enseignement) {
        this.enseignement = enseignement;
    }

    public Cours getCoursDonne() {
        return coursDonne;
    }

    public void setCoursDonne(Cours coursDonne) {
        this.coursDonne = coursDonne;
    }

    public Cours getCoursPour() {
        return coursPour;
    }

    public void setCoursPour(Cours coursPour) {
        this.coursPour = coursPour;
    }

    public LocalDate getDatePour() {
        return datePour;
    }

    public void setDatePour(LocalDate datePour) {
        this.datePour = datePour;
    }

    public Enseignant getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(Enseignant enseignant) {
        this.enseignant = enseignant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
