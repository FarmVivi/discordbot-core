package fr.farmvivi.discordbot.module.cnam.task;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatterBuilder;
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
    private final WebClient webClient;

    private final Logger logger = LoggerFactory.getLogger(PlanningScrapperTask.class);
    private final List<PlanningListener> listeners = new LinkedList<>();

    public PlanningScrapperTask(String codeScolarite, String uid, DatabaseAccess databaseAccess) {
        this.codeScolarite = codeScolarite;
        this.uid = uid;
        this.salleDAO = new SalleDAO(databaseAccess);
        this.enseignantDAO = new EnseignantDAO(databaseAccess);
        this.enseignementDAO = new EnseignementDAO(databaseAccess);
        this.coursDAO = new CoursDAO(databaseAccess);

        this.webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
    }

    @Override
    public void run() {
        // Set up the URL with the search term and send the request
        String searchUrl = "https://iscople.gescicca.net/Planning.aspx?code_scolarite=" + URLEncoder.encode(codeScolarite, StandardCharsets.UTF_8) + "&uid=" + URLEncoder.encode(uid, StandardCharsets.UTF_8);
        HtmlPage page;
        try {
            WebRequest webRequest = new WebRequest(new URL(searchUrl));
            webRequest.setCharset(StandardCharsets.UTF_8);
            page = webClient.getPage(webRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HtmlElement htmlElement = page.getFirstByXPath("//*[@id=\"m_c_planning_lbChangerModeAffichage\"]");
        try {
            htmlElement.click();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        webClient.waitForBackgroundJavaScript(10000);

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

        // Retrieve all <li> elements
        List<HtmlElement> items = page.getByXPath("//*[@class=\"cgvRow\"] | //*[@class=\"cgvRowAlt\"]");
        if (!items.isEmpty()) {
            // Iterate over all elements
            int rowId = 0;
            rowsLoop:
            for (HtmlElement row : items) {
                // Retrieve column elements
                List<HtmlElement> cols = row.getByXPath("./*");
                if (!cols.isEmpty()) {
                    String jour = null;
                    LocalDate date = null;
                    LocalTime heureDebut = null;
                    LocalTime heureFin = null;
                    String codeCours = null;
                    String libelleCours = null;
                    boolean presentiel = false;
                    String nomEnseignant = null;
                    String prenomEnseignant = null;
                    String nomSalle = null;
                    String adresseSalle = null;

                    // Iterate over all columns
                    int colId = 0;
                    for (HtmlElement col : cols) {
                        // Retrieve the text
                        switch (colId) {
                            case 0 -> {
                                String[] text = col.asNormalizedText().split("\n");
                                if (text.length != 2) {
                                    continue rowsLoop;
                                }
                                jour = text[0];
                                date = LocalDate.parse(text[1], new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter());
                            }
                            case 1 -> {
                                String[] text = col.asNormalizedText().split("\n");
                                if (text.length != 2) {
                                    continue rowsLoop;
                                }
                                heureDebut = LocalTime.parse(text[0], new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter());
                                heureFin = LocalTime.parse(text[1], new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter());
                            }
                            case 2 -> {
                                String text = col.asNormalizedText();
                                if (text.isBlank()) {
                                    continue rowsLoop;
                                }
                                codeCours = text;
                            }
                            case 3 -> {
                                String text = col.asNormalizedText();
                                if (text.isBlank()) {
                                    continue rowsLoop;
                                }
                                libelleCours = text;
                            }
                            case 5 -> {
                                String text = col.asNormalizedText();
                                if (text.isBlank()) {
                                    continue rowsLoop;
                                }
                                presentiel = text.equals("Présentiel");
                            }
                            case 6 -> {
                                String text = col.asNormalizedText();
                                /*
                                Exemple:
                                - Input: "M. BERNARD Jean Pierre"
                                - Output:
                                  - nomEnseignant: "BERNARD"
                                  - prenomEnseignant: "Jean Pierre"
                                */
                                String[] textSplit = text.split(" ");
                                if (textSplit.length < 3) {
                                    continue rowsLoop;
                                }
                                nomEnseignant = textSplit[1];
                                prenomEnseignant = text.substring(text.indexOf(nomEnseignant) + nomEnseignant.length() + 1);
                            }
                            case 7 -> {
                                String text = col.asNormalizedText();
                                if (text.isBlank()) {
                                    continue rowsLoop;
                                }
                                String[] textSplit = text.split("\n");
                                if (textSplit.length < 2) {
                                    continue rowsLoop;
                                }
                                nomSalle = textSplit[0];
                                adresseSalle = text.substring(text.indexOf(nomSalle) + nomSalle.length() + 1);

                                // Fix pour les salles virtuelles
                                if (nomSalle.contains("virtuelle") && adresseSalle.contains("Distanciel")) {
                                    presentiel = false;
                                }
                            }
                        }
                        colId++;
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
                        salle = new Salle(nomSalle, adresseSalle);
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
                                Salle searchSalle = salleDAO.selectByNom(nomSalle);

                                // Si une salle similaire est trouvée, on la met à jour
                                if (searchSalle != null) {
                                    for (PlanningListener listener : listeners) {
                                        listener.onSalleUpdate(new SalleUpdateEvent(searchSalle, salle));
                                    }
                                    salle = new Salle(searchSalle.getId(), salle.getNom(), salle.getAdresse());
                                    salleDAO.update(salle);
                                    bddSalles.remove(searchSalle);

                                    // Sinon si aucune salle similaire n'est trouvée, on en créée une nouvelle
                                } else {
                                    for (PlanningListener listener : listeners) {
                                        listener.onSalleCreate(new SalleCreateEvent(salle));
                                    }
                                    salle = salleDAO.create(salle);
                                }

                                bddSalles.add(salle);
                            }

                            salles.add(salle);
                        }

                        // Enseignant
                        enseignant = new Enseignant(nomEnseignant, prenomEnseignant);
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
                                Enseignant searchEnseignant = enseignantDAO.selectByNomPrenom(nomEnseignant, prenomEnseignant);

                                // Si un enseignant similaire est trouvé, on le met à jour
                                if (searchEnseignant != null) {
                                    for (PlanningListener listener : listeners) {
                                        listener.onEnseignantUpdate(new EnseignantUpdateEvent(searchEnseignant, enseignant));
                                    }
                                    enseignant = new Enseignant(searchEnseignant.getId(), enseignant.getNom(), enseignant.getPrenom());
                                    enseignantDAO.update(enseignant);
                                    bddEnseignants.remove(searchEnseignant);

                                    // Sinon si aucun enseignant similaire n'est trouvé, on en crée un nouveau
                                } else {
                                    for (PlanningListener listener : listeners) {
                                        listener.onEnseignantCreate(new EnseignantCreateEvent(enseignant));
                                    }
                                    enseignant = enseignantDAO.create(enseignant);
                                }

                                bddEnseignants.add(enseignant);
                            }

                            enseignants.add(enseignant);
                        }

                        // Enseignement
                        enseignement = new Enseignement(codeCours, libelleCours);
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
                                Enseignement searchEnseignement = enseignementDAO.selectById(codeCours);

                                // Si un enseignement similaire est trouvé, on le met à jour
                                if (searchEnseignement != null) {
                                    for (PlanningListener listener : listeners) {
                                        listener.onEnseignementUpdate(new EnseignementUpdateEvent(searchEnseignement, enseignement));
                                    }
                                    enseignement = new Enseignement(searchEnseignement.getCode(), enseignement.getNom());
                                    enseignementDAO.update(enseignement);
                                    bddEnseignements.remove(searchEnseignement);

                                    // Sinon si aucun enseignement similaire n'est trouvé, on en crée un nouveau
                                } else {
                                    for (PlanningListener listener : listeners) {
                                        listener.onEnseignementCreate(new EnseignementCreateEvent(enseignement));
                                    }
                                    enseignement = enseignementDAO.create(enseignement);
                                }

                                bddEnseignements.add(enseignement);
                            }

                            enseignements.add(enseignement);
                        }

                        // Cours
                        cours = new Cours(date, heureDebut, heureFin, presentiel, enseignant.getId(), salle.getId(), enseignement.getCode());
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
                                List<Cours> searchCourss = coursDAO.selectAllByDateHeure(date, heureDebut, heureFin);
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
                                    for (PlanningListener listener : listeners) {
                                        listener.onCoursUpdate(new CoursUpdateEvent(searchCours, cours, salle, enseignant, enseignement));
                                    }
                                    cours = new Cours(searchCours.getId(), cours.getDate(), cours.getHeureDebut(), cours.getHeureFin(), cours.isPresentiel(), cours.getEnseignantId(), cours.getSalleId(), cours.getEnseignementCode());
                                    coursDAO.update(cours);
                                    bddCourss.remove(searchCours);

                                    // Sinon si aucun cours similaire n'est trouvé, on en crée un nouveau
                                } else {
                                    for (PlanningListener listener : listeners) {
                                        listener.onCoursCreate(new CoursCreateEvent(cours, salle, enseignant, enseignement));
                                    }
                                    cours = coursDAO.create(cours);
                                }

                                bddCourss.add(cours);
                            }

                            courss.add(cours);
                        }
                    } catch (SQLException e) {
                        StringBuilder error = new StringBuilder("Erreur lors de l'importation d'un cours");
                        error.append("\nDate : ").append(date);
                        error.append("\nHeure de début : ").append(heureDebut);
                        error.append("\nHeure de fin : ").append(heureFin);
                        error.append("\nPrésentiel : ").append(presentiel);
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
                } else {
                    System.out.println("No column found for row " + rowId + " !");
                }
                rowId++;
            }

            // Suppression des éléments non utilisés

            try {
                // Cours
                bddCourss.removeAll(courss);
                for (Cours cours : bddCourss) {
                    for (PlanningListener listener : listeners) {
                        listener.onCoursRemove(new CoursRemoveEvent(cours, salleDAO.selectById(cours.getSalleId()), enseignantDAO.selectById(cours.getEnseignantId()), enseignementDAO.selectById(cours.getEnseignementCode())));
                    }
                    coursDAO.delete(cours);
                }

                // Salles
                bddSalles.removeAll(salles);
                for (Salle salle : bddSalles) {
                    for (PlanningListener listener : listeners) {
                        listener.onSalleRemove(new SalleRemoveEvent(salle));
                    }
                    salleDAO.delete(salle);
                }

                // Enseignants
                bddEnseignants.removeAll(enseignants);
                for (Enseignant enseignant : bddEnseignants) {
                    for (PlanningListener listener : listeners) {
                        listener.onEnseignantRemove(new EnseignantRemoveEvent(enseignant));
                    }
                    enseignantDAO.delete(enseignant);
                }

                // Enseignements
                bddEnseignements.removeAll(enseignements);
                for (Enseignement enseignement : bddEnseignements) {
                    for (PlanningListener listener : listeners) {
                        listener.onEnseignementRemove(new EnseignementRemoveEvent(enseignement));
                    }
                    enseignementDAO.delete(enseignement);
                }
            } catch (SQLException e) {
                logger.error("Erreur lors de la suppression des éléments non utilisés", e);
            }
        } else {
            logger.warn("Aucune donnée trouvée !");
        }

        page.cleanUp();
    }

    public void registerListener(PlanningListener listener) {
        listeners.add(listener);
    }

    public void close() {
        webClient.close();
    }
}
