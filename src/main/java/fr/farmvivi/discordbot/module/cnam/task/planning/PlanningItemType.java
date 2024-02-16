package fr.farmvivi.discordbot.module.cnam.task.planning;

public enum PlanningItemType {
    COURS("Cours", "Cours"),
    EXAMEN("Examen", "Exam"),
    ;

    private final String nom;
    private final String fromCSV;

    PlanningItemType(String nom, String fromCSV) {
        this.nom = nom;
        this.fromCSV = fromCSV;
    }

    /**
     * Get the PlanningItemType from the CSV value
     *
     * @param csv The CSV value
     * @return The PlanningItemType
     */
    public static PlanningItemType fromCSV(String csv) {
        for (PlanningItemType type : values()) {
            if (type.fromCSV.equals(csv)) {
                return type;
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
