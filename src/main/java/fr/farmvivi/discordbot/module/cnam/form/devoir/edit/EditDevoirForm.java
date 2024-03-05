package fr.farmvivi.discordbot.module.cnam.form.devoir.edit;

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
import fr.farmvivi.discordbot.module.cnam.events.devoir.DevoirUpdateEvent;
import fr.farmvivi.discordbot.module.cnam.form.devoir.DevoirForm;
import fr.farmvivi.discordbot.module.cnam.form.devoir.add.step.CoursDonneCurrentCoursFormStep;
import fr.farmvivi.discordbot.module.forms.Form;
import fr.farmvivi.discordbot.module.forms.FormsModule;
import fr.farmvivi.discordbot.utils.event.IEventManager;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.sql.SQLException;
import java.time.LocalDate;

public class EditDevoirForm extends Form implements DevoirForm {
    private final IEventManager eventManager;
    private final FormsModule forms;

    private final DevoirDAO devoirDAO;
    private final CoursDAO coursDAO;
    private final EnseignementDAO enseignementDAO;
    private final EnseignantDAO enseignantDAO;

    private Devoir devoir;

    public EditDevoirForm(DatabaseManager databaseManager, IEventManager eventManager, FormsModule forms) {
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

        if (devoir == null) {
            event.reply("> :x: Le devoir à modifier n'a pas été renseigné.").setEphemeral(true).queue();
            return;
        }

        try {
            devoirDAO.update(devoir);
            DevoirUpdateEvent devoirUpdateEvent = new DevoirUpdateEvent(devoir);
            eventManager.handleAsync(devoirUpdateEvent);
            event.reply("> :white_check_mark: Le devoir a été modifié.").setEphemeral(true).queue();
        } catch (SQLException e) {
            event.reply("> :x: Une erreur est survenue lors de la modification du devoir.").setEphemeral(true).queue();
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
        try {
            return enseignementDAO.selectById(devoir.getCodeEnseignement());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setEnseignement(Enseignement enseignement) {
        devoir.setCodeEnseignement(enseignement.getCode());
    }

    @Override
    public Cours getCoursDonne() {
        try {
            return coursDAO.selectById(devoir.getIdCoursDonne());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setCoursDonne(Cours coursDonne) {
        devoir.setIdCoursDonne(coursDonne.getId());
    }

    @Override
    public Cours getCoursPour() {
        try {
            return coursDAO.selectById(devoir.getIdCoursPour());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setCoursPour(Cours coursPour) {
        devoir.setIdCoursPour(coursPour.getId());
    }

    @Override
    public LocalDate getDatePour() {
        return devoir.getDatePour();
    }

    @Override
    public void setDatePour(LocalDate datePour) {
        devoir.setDatePour(datePour);
    }

    @Override
    public Enseignant getEnseignant() {
        try {
            return enseignantDAO.selectById(devoir.getIdEnseignant());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setEnseignant(Enseignant enseignant) {
        devoir.setIdEnseignant(enseignant.getId());
    }

    @Override
    public String getDescription() {
        return devoir.getDescription();
    }

    @Override
    public void setDescription(String description) {
        devoir.setDescription(description);
    }

    public Devoir getDevoir() {
        return devoir;
    }

    public void setDevoir(Devoir devoir) {
        this.devoir = devoir;
    }
}
