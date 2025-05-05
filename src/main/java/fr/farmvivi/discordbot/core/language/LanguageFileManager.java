package fr.farmvivi.discordbot.core.language;

import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Gestionnaire des fichiers de langue.
 * Cette classe est responsable du chargement et de la création des fichiers de langue.
 */
public class LanguageFileManager {
    private static final Logger logger = LoggerFactory.getLogger(LanguageFileManager.class);
    private final LanguageManager languageManager;
    private final File langFolder;

    /**
     * Crée un nouveau gestionnaire de fichiers de langue.
     *
     * @param languageManager le gestionnaire de langue
     * @param langFolder      le dossier contenant les fichiers de langue
     */
    public LanguageFileManager(LanguageManager languageManager, File langFolder) {
        this.languageManager = languageManager;
        this.langFolder = langFolder;
    }

    /**
     * Charge tous les fichiers de langue du dossier.
     */
    public void loadLanguageFiles() {
        if (!langFolder.exists()) {
            langFolder.mkdirs();

            // Copie des fichiers de langue par défaut depuis les resources vers le dossier runtime
            copyDefaultLanguageFile("en-US.yml");
            copyDefaultLanguageFile("fr-FR.yml");
        }

        // Chargement de tous les fichiers .yml du dossier de langue
        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File langFile : langFiles) {
                loadLanguageFile(langFile);
            }
        }
    }

    /**
     * Copie un fichier de langue par défaut depuis les resources vers le dossier runtime.
     *
     * @param fileName le nom du fichier à copier
     */
    private void copyDefaultLanguageFile(String fileName) {
        try {
            // Charge le fichier depuis les resources
            InputStream inputStream = getClass().getResourceAsStream("/lang/" + fileName);
            if (inputStream == null) {
                logger.warn("Default language file not found in resources: {}. Please make sure the file exists in the JAR resources.", fileName);
                return;
            }

            // Copie le fichier vers le dossier runtime
            File outputFile = new File(langFolder, fileName);
            Files.copy(inputStream, outputFile.toPath());
            logger.info("Copied default language file from resources: {}", fileName);
        } catch (IOException e) {
            logger.error("Failed to copy default language file: {}", fileName, e);
        }
    }

    /**
     * Charge un fichier de langue spécifique.
     *
     * @param langFile le fichier de langue à charger
     */
    private void loadLanguageFile(File langFile) {
        try {
            // Analyse du code de la locale à partir du nom du fichier
            String localeCode = langFile.getName().replace(".yml", "");
            Locale locale = Locale.forLanguageTag(localeCode);

            // Chargement du fichier de langue
            Yaml yaml = new Yaml();
            try (FileReader reader = new FileReader(langFile)) {
                Map<String, Object> langData = yaml.load(reader);
                if (langData != null) {
                    // Conversion de la carte imbriquée en carte plate avec des clés comme "section.key"
                    Map<String, String> flatMap = flattenMap(langData, "");

                    // Chargement des chaînes de langue
                    languageManager.loadLanguage("core", locale, flatMap);
                    logger.info("Fichier de langue chargé : {}", langFile.getName());
                }
            }
        } catch (Exception e) {
            logger.error("Échec du chargement du fichier de langue : {}", langFile.getName(), e);
        }
    }

    /**
     * Aplatit une carte imbriquée en carte plate avec des clés en notation pointée.
     *
     * @param map    la carte imbriquée
     * @param prefix le préfixe pour les clés
     * @return une carte aplatie
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> flattenMap(Map<String, Object> map, String prefix) {
        Map<String, String> flatMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();

            if (entry.getValue() instanceof Map) {
                // Aplatissement récursif des cartes imbriquées
                flatMap.putAll(flattenMap((Map<String, Object>) entry.getValue(), key));
            } else {
                // Ajout de la valeur feuille
                flatMap.put(key, String.valueOf(entry.getValue()));
            }
        }

        return flatMap;
    }
}