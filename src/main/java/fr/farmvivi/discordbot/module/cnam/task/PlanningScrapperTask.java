package fr.farmvivi.discordbot.module.cnam.task;

import fr.farmvivi.discordbot.module.cnam.database.DatabaseAccess;
import fr.farmvivi.discordbot.module.cnam.database.cours.Cours;
import fr.farmvivi.discordbot.module.cnam.database.cours.CoursDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.Enseignant;
import fr.farmvivi.discordbot.module.cnam.database.enseignant.EnseignantDAO;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.Enseignement;
import fr.farmvivi.discordbot.module.cnam.database.enseignement.EnseignementDAO;
import fr.farmvivi.discordbot.module.cnam.database.salle.Salle;
import fr.farmvivi.discordbot.module.cnam.database.salle.SalleDAO;
import fr.farmvivi.discordbot.module.cnam.events.PlanningListener;
import fr.farmvivi.discordbot.module.cnam.events.cours.CoursCreateEvent;
import fr.farmvivi.discordbot.module.cnam.events.cours.CoursRemoveEvent;
import fr.farmvivi.discordbot.module.cnam.events.cours.CoursUpdateEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignant.EnseignantCreateEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignant.EnseignantRemoveEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignant.EnseignantUpdateEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignement.EnseignementCreateEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignement.EnseignementRemoveEvent;
import fr.farmvivi.discordbot.module.cnam.events.enseignement.EnseignementUpdateEvent;
import fr.farmvivi.discordbot.module.cnam.events.salle.SalleCreateEvent;
import fr.farmvivi.discordbot.module.cnam.events.salle.SalleRemoveEvent;
import fr.farmvivi.discordbot.module.cnam.events.salle.SalleUpdateEvent;
import fr.farmvivi.discordbot.module.cnam.task.planning.PlanningItem;
import fr.farmvivi.discordbot.module.cnam.task.planning.PlanningItemType;
import fr.farmvivi.discordbot.module.cnam.task.planning.PlanningScrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PlanningScrapperTask implements Runnable {
    private final String codeScolarite;
    private final String uid;
    private final SalleDAO salleDAO;
    private final EnseignantDAO enseignantDAO;
    private final EnseignementDAO enseignementDAO;
    private final CoursDAO coursDAO;

    private final Logger logger = LoggerFactory.getLogger(PlanningScrapperTask.class);
    private final List<PlanningListener> listeners = new LinkedList<>();

    public PlanningScrapperTask(String codeScolarite, String uid, DatabaseAccess databaseAccess) {
        this.codeScolarite = codeScolarite;
        this.uid = uid;
        this.salleDAO = new SalleDAO(databaseAccess);
        this.enseignantDAO = new EnseignantDAO(databaseAccess);
        this.enseignementDAO = new EnseignementDAO(databaseAccess);
        this.coursDAO = new CoursDAO(databaseAccess);
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        // Parsing planning
        logger.info("Parsing planning...");

        PlanningScrapper scrapper = new PlanningScrapper(codeScolarite, uid);
        List<PlanningItem> planningCoursses = scrapper.scrap();
        scrapper.close();

        if (planningCoursses == null) {
            logger.error("Error while parsing planning");
            return;
        }

        if (planningCoursses.isEmpty()) {
            logger.info("No planning found");
            return;
        }

        // Retrieve planning from database
        logger.info("Retrieving planning from database...");

        List<Salle> bddSalles;
        List<Enseignant> bddEnseignants;
        List<Enseignement> bddEnseignements;
        List<Cours> bddCourss;

        try {
            bddSalles = salleDAO.selectAll();
            bddEnseignants = enseignantDAO.selectAll();
            bddEnseignements = enseignementDAO.selectAll();
            bddCourss = coursDAO.selectAll();
        } catch (SQLException e) {
            logger.error("Error while getting data from database", e);
            return;
        }

        List<Salle> salles = new ArrayList<>();
        List<Enseignant> enseignants = new ArrayList<>();
        List<Enseignement> enseignements = new ArrayList<>();
        List<Cours> courss = new ArrayList<>();

        // Iterate over all cours
        for (PlanningItem planningItem : planningCoursses) {
            // Skip private cours
            if (planningItem.isPrive()) {
                continue;
            }

            /***************************************************
             *** Création des objets et appel des événements ***
             ***************************************************/

            Salle salle = null;
            Enseignant enseignant = null;
            Enseignement enseignement = null;
            Cours cours = null;
            try {
                // Salle
                salle = new Salle(planningItem.getEmplacementNom(), planningItem.getEmplacementAdresse());
                // Si la salle est chargée en mémoire
                if (salles.contains(salle)) {
                    salle = salles.get(salles.indexOf(salle));

                    // Sinon si la salle n'est pas chargée en mémoire
                } else {
                    // Si la salle est déjà enregistrée dans la base de données
                    if (bddSalles.contains(salle)) {
                        salle = bddSalles.get(bddSalles.indexOf(salle));

                        // Sinon si la salle n'est pas enregistrée dans la base de données
                    } else {
                        // On recherche une salle similaire
                        Salle searchSalle = salleDAO.selectByNom(planningItem.getEmplacementNom());

                        // Si une salle similaire est trouvée, on la met à jour
                        if (searchSalle != null) {
                            logger.info("Updating salle " + searchSalle.getId() + " \"" + searchSalle + "\" to " + salle.getId() + " \"" + salle + "\"");

                            // Envoi de l'événement
                            for (PlanningListener listener : listeners) {
                                listener.onSalleUpdate(new SalleUpdateEvent(searchSalle, salle));
                            }

                            // Mise à jour en base de données
                            salle = new Salle(searchSalle.getId(), salle.getNom(), salle.getAdresse());
                            salleDAO.update(salle);
                            bddSalles.remove(searchSalle);

                            // Sinon si aucune salle similaire n'est trouvée, on en créée une nouvelle
                        } else {
                            logger.info("Creating salle \"" + salle + "\"");

                            // Envoi de l'événement
                            for (PlanningListener listener : listeners) {
                                listener.onSalleCreate(new SalleCreateEvent(salle));
                            }

                            // Création en base de données
                            salle = salleDAO.create(salle);
                        }

                        bddSalles.add(salle);
                    }

                    salles.add(salle);
                }

                // Enseignant
                enseignant = new Enseignant(planningItem.getOrganisateurNom(), planningItem.getOrganisateurPrenom());
                // Si l'enseignant est chargé en mémoire
                if (enseignants.contains(enseignant)) {
                    enseignant = enseignants.get(enseignants.indexOf(enseignant));

                    // Sinon si l'enseignant n'est pas chargé en mémoire
                } else {
                    // Si l'enseignant est déjà enregistré dans la base de données
                    if (bddEnseignants.contains(enseignant)) {
                        enseignant = bddEnseignants.get(bddEnseignants.indexOf(enseignant));

                        // Sinon si l'enseignant n'est pas enregistré dans la base de données
                    } else {
                        // On recherche un enseignant similaire
                        Enseignant searchEnseignant = enseignantDAO.selectByNomPrenom(planningItem.getOrganisateurNom(), planningItem.getOrganisateurPrenom());

                        // Si un enseignant similaire est trouvé, on le met à jour
                        if (searchEnseignant != null) {
                            logger.info("Updating enseignant " + searchEnseignant.getId() + " \"" + searchEnseignant + "\" to " + enseignant.getId() + " \"" + enseignant + "\"");

                            // Envoi de l'événement
                            for (PlanningListener listener : listeners) {
                                listener.onEnseignantUpdate(new EnseignantUpdateEvent(searchEnseignant, enseignant));
                            }

                            // Mise à jour en base de données
                            enseignant = new Enseignant(searchEnseignant.getId(), enseignant.getNom(), enseignant.getPrenom());
                            enseignantDAO.update(enseignant);
                            bddEnseignants.remove(searchEnseignant);

                            // Sinon si aucun enseignant similaire n'est trouvé, on en crée un nouveau
                        } else {
                            logger.info("Creating enseignant \"" + enseignant + "\"");

                            // Envoi de l'événement
                            for (PlanningListener listener : listeners) {
                                listener.onEnseignantCreate(new EnseignantCreateEvent(enseignant));
                            }

                            // Création en base de données
                            enseignant = enseignantDAO.create(enseignant);
                        }

                        bddEnseignants.add(enseignant);
                    }

                    enseignants.add(enseignant);
                }

                // Enseignement
                enseignement = new Enseignement(planningItem.getCodeUE(), planningItem.getDescription());
                // Si l'enseignement est chargé en mémoire
                if (enseignements.contains(enseignement)) {
                    enseignement = enseignements.get(enseignements.indexOf(enseignement));

                    // Sinon si l'enseignement n'est pas chargé en mémoire
                } else {
                    // Si l'enseignement est déjà enregistré dans la base de données
                    if (bddEnseignements.contains(enseignement)) {
                        enseignement = bddEnseignements.get(bddEnseignements.indexOf(enseignement));

                        // Sinon si l'enseignement n'est pas enregistré dans la base de données
                    } else {
                        // On recherche un enseignement similaire
                        Enseignement searchEnseignement = enseignementDAO.selectById(planningItem.getCodeUE());

                        // Si un enseignement similaire est trouvé, on le met à jour
                        if (searchEnseignement != null) {
                            logger.info("Updating enseignement " + searchEnseignement.getCode() + " \"" + searchEnseignement + "\" to " + enseignement.getCode() + " \"" + enseignement + "\"");

                            // Envoi de l'événement
                            for (PlanningListener listener : listeners) {
                                listener.onEnseignementUpdate(new EnseignementUpdateEvent(searchEnseignement, enseignement));
                            }

                            // Mise à jour en base de données
                            enseignement = new Enseignement(searchEnseignement.getCode(), enseignement.getNom());
                            enseignementDAO.update(enseignement);
                            bddEnseignements.remove(searchEnseignement);

                            // Sinon si aucun enseignement similaire n'est trouvé, on en crée un nouveau
                        } else {
                            logger.info("Creating enseignement \"" + enseignement + "\"");

                            // Envoi de l'événement
                            for (PlanningListener listener : listeners) {
                                listener.onEnseignementCreate(new EnseignementCreateEvent(enseignement));
                            }

                            // Création en base de données
                            enseignement = enseignementDAO.create(enseignement);
                        }

                        bddEnseignements.add(enseignement);
                    }

                    enseignements.add(enseignement);
                }

                // Cours
                cours = new Cours(planningItem.getDateDebut(), planningItem.getDateFin(), planningItem.isEmplacementPresentiel(), planningItem.getType().equals(PlanningItemType.EXAMEN), enseignant.getId(), salle.getId(), enseignement.getCode());
                // Si le cours est chargé en mémoire
                if (courss.contains(cours)) {
                    cours = courss.get(courss.indexOf(cours));

                    // Sinon si le cours n'est pas chargé en mémoire
                } else {
                    // Si le cours est déjà enregistré dans la base de données
                    if (bddCourss.contains(cours)) {
                        cours = bddCourss.get(bddCourss.indexOf(cours));

                        // Sinon si le cours n'est pas enregistré dans la base de données
                    } else {
                        // On recherche les cours similaires
                        List<Cours> searchCourss = coursDAO.selectAllByHoraires(planningItem.getDateDebut(), planningItem.getDateFin());
                        Cours searchCours = null;

                        // On recherche un cours ayant le même enseignement
                        for (Cours searchCoursTemp : searchCourss) {
                            if (searchCoursTemp.getEnseignementCode().equals(enseignement.getCode())) {
                                searchCours = searchCoursTemp;
                                break;
                            }
                        }

                        // Si un cours similaire est trouvé, on le met à jour
                        if (searchCours != null) {
                            logger.info("Updating cours " + searchCours.getId() + " \"" + searchCours + "\" to " + cours.getId() + " \"" + cours + "\"");

                            // Envoi de l'événement
                            for (PlanningListener listener : listeners) {
                                listener.onCoursUpdate(new CoursUpdateEvent(searchCours, cours, salle, enseignant, enseignement));
                            }

                            // Mise à jour en base de données
                            cours = new Cours(searchCours.getId(), cours.getDebutCours(), cours.getFinCours(), cours.isPresentiel(), cours.isExamen(), cours.getEnseignantId(), cours.getSalleId(), cours.getEnseignementCode());
                            coursDAO.update(cours);
                            bddCourss.remove(searchCours);

                            // Sinon si aucun cours similaire n'est trouvé, on en crée un nouveau
                        } else {
                            logger.info("Creating cours \"" + cours + "\"");

                            // Envoi de l'événement
                            for (PlanningListener listener : listeners) {
                                listener.onCoursCreate(new CoursCreateEvent(cours, salle, enseignant, enseignement));
                            }

                            // Création en base de données
                            cours = coursDAO.create(cours);
                        }

                        bddCourss.add(cours);
                    }

                    courss.add(cours);
                }
            } catch (SQLException e) {
                StringBuilder error = new StringBuilder("Erreur lors de l'importation d'un cours");
                error.append("\nDébut : ").append(planningItem.getDateDebut());
                error.append("\nFin : ").append(planningItem.getDateFin());
                error.append("\nPrésentiel : ").append(planningItem.isEmplacementPresentiel());
                error.append("\nSalle : ").append(salle);
                error.append("\nEnseignant : ").append(enseignant);
                error.append("\nEnseignement : ").append(enseignement);
                error.append("\nCours : ").append(cours);
                error.append("\nDEBUG");
                if (salle != null) {
                    error.append("\nSalle.getId() : ").append(salle.getId());
                }
                if (enseignant != null) {
                    error.append("\nEnseignant.getId() : ").append(enseignant.getId());
                }
                if (enseignement != null) {
                    error.append("\nEnseignement.getCode() : ").append(enseignement.getCode());
                }
                if (cours != null) {
                    error.append("\nCours.getId() : ").append(cours.getId());
                    error.append("\nCours.getSalleId() : ").append(cours.getSalleId());
                    error.append("\nCours.getEnseignantId() : ").append(cours.getEnseignantId());
                    error.append("\nCours.getEnseignementCode() : ").append(cours.getEnseignementCode());
                }

                logger.error(error.toString(), e);
            }

            /*******************************************************
             *** Fin création des objets et appel des événements ***
             *******************************************************/
        }

        // Delete all elements that are not present anymore
        logger.info("Deleting elements that are not present anymore...");

        // Suppression des éléments non utilisés
        // Cours
        bddCourss.removeAll(courss);
        for (Cours cours : bddCourss) {
            logger.info("Deleting cours " + cours.getId() + " \"" + cours + "\"");

            // Récupération des informations du cours
            Salle salleCours;
            Enseignant enseignantCours;
            Enseignement enseignementCours;
            try {
                salleCours = salleDAO.selectById(cours.getSalleId());
                enseignantCours = enseignantDAO.selectById(cours.getEnseignantId());
                enseignementCours = enseignementDAO.selectById(cours.getEnseignementCode());
            } catch (SQLException e) {
                logger.error("Error while deleting a cours, impossible to retrieve cours informations " + cours.getId() + " \"" + cours + "\" to trigger the event", e);
                continue;
            }

            // Envoi de l'événement
            for (PlanningListener listener : listeners) {
                listener.onCoursRemove(new CoursRemoveEvent(cours, salleCours, enseignantCours, enseignementCours));
            }

            // Suppression en base de données
            try {
                coursDAO.delete(cours);
            } catch (SQLException e) {
                logger.error("Error while deleting a cours " + cours.getId() + " \"" + cours + "\"", e);
            }
        }

        // Salles
        bddSalles.removeAll(salles);
        for (Salle salle : bddSalles) {
            logger.info("Deleting salle " + salle.getId() + " \"" + salle + "\"");

            // Envoi de l'événement
            for (PlanningListener listener : listeners) {
                listener.onSalleRemove(new SalleRemoveEvent(salle));
            }

            // Suppression en base de données
            try {
                salleDAO.delete(salle);
            } catch (SQLException e) {
                logger.error("Error while deleting a salle " + salle.getId() + " \"" + salle + "\"", e);
            }
        }

        // Enseignants
        bddEnseignants.removeAll(enseignants);
        for (Enseignant enseignant : bddEnseignants) {
            logger.info("Deleting enseignant " + enseignant.getId() + " \"" + enseignant + "\"");

            // Envoi de l'événement
            for (PlanningListener listener : listeners) {
                listener.onEnseignantRemove(new EnseignantRemoveEvent(enseignant));
            }

            // Suppression en base de données
            try {
                enseignantDAO.delete(enseignant);
            } catch (SQLException e) {
                logger.error("Error while deleting a enseignant " + enseignant.getId() + " \"" + enseignant + "\"", e);
            }
        }

        // Enseignements
        bddEnseignements.removeAll(enseignements);
        for (Enseignement enseignement : bddEnseignements) {
            logger.info("Deleting enseignement " + enseignement.getCode() + " \"" + enseignement + "\"");

            // Envoi de l'événement
            for (PlanningListener listener : listeners) {
                listener.onEnseignementRemove(new EnseignementRemoveEvent(enseignement));
            }

            // Suppression en base de données
            try {
                enseignementDAO.delete(enseignement);
            } catch (SQLException e) {
                logger.error("Error while deleting a enseignement " + enseignement.getCode() + " \"" + enseignement + "\"", e);
            }
        }

        long finish = System.currentTimeMillis();

        logger.info("Done (" + (float) (finish - start) / 1000 + "s)!");
    }

    public void registerListener(PlanningListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(PlanningListener listener) {
        listeners.remove(listener);
    }
}
