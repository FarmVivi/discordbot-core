package fr.farmvivi.discordbot.module.cnam.task.planning;

public enum PlanningItemPriorite {
    NORMALE("Normale", "Normale"),
    ;

    private final String nom;
    private final String fromCSV;

    PlanningItemPriorite(String nom, String fromCSV) {
        this.nom = nom;
        this.fromCSV = fromCSV;
    }

    /**
     * Get the PlanningItemPriorite from the CSV value
     *
     * @param csv The CSV value
     * @return The PlanningItemPriorite
     */
    public static PlanningItemPriorite fromCSV(String csv) {
        for (PlanningItemPriorite priorite : values()) {
            if (priorite.fromCSV.equals(csv)) {
                return priorite;
            }
        }
        return null;
    }

    public String getNom() {
        return nom;
    }

    public String getFromCSV() {
        return fromCSV;
    }

    @Override
    public String toString() {
        return nom;
    }
}
