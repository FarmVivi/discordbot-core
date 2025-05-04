package fr.farmvivi.discordbot.core.language;

import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

            // Création des fichiers de langue par défaut
            createDefaultEnglishFile(new File(langFolder, "en-US.yml"));
            createDefaultFrenchFile(new File(langFolder, "fr-FR.yml"));
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

    /**
     * Crée le fichier de langue anglais par défaut.
     *
     * @param file le fichier à créer
     */
    private void createDefaultEnglishFile(File file) {
        try {
            Files.writeString(file.toPath(),
                    "# English language file (en-US)\n" +
                            "general:\n" +
                            "  yes: \"Yes\"\n" +
                            "  no: \"No\"\n" +
                            "  enabled: \"Enabled\"\n" +
                            "  disabled: \"Disabled\"\n" +
                            "  success: \"Success\"\n" +
                            "  error: \"Error\"\n" +
                            "  warning: \"Warning\"\n" +
                            "  info: \"Information\"\n" +
                            "\n" +
                            "bot:\n" +
                            "  startup: \"Bot is starting up...\"\n" +
                            "  shutdown: \"Bot is shutting down...\"\n" +
                            "  connected: \"Connected to Discord as {0}\"\n" +
                            "  disconnected: \"Disconnected from Discord\"\n" +
                            "\n" +
                            "plugins:\n" +
                            "  loading: \"Loading plugins...\"\n" +
                            "  loaded: \"Loaded {0} plugins\"\n" +
                            "  enabling: \"Enabling plugins...\"\n" +
                            "  enabled: \"Enabled {0} plugins\"\n" +
                            "  disabling: \"Disabling plugins...\"\n" +
                            "  disabled: \"Disabled {0} plugins\"\n" +
                            "  reloading: \"Reloading plugins...\"\n" +
                            "  reloaded: \"Reloaded {0} plugins\"\n" +
                            "\n" +
                            "commands:\n" +
                            "  help: \"Displays help for commands\"\n" +
                            "  reload: \"Reloads the bot or specific plugins\"\n" +
                            "  version: \"Displays the bot version\"\n" +
                            "  plugins: \"Lists all loaded plugins\"\n" +
                            "  language: \"Changes the bot language\"\n" +
                            "\n" +
                            "errors:\n" +
                            "  command_not_found: \"Command not found: {0}\"\n" +
                            "  plugin_not_found: \"Plugin not found: {0}\"\n" +
                            "  no_permission: \"You don't have permission to use this command\"\n" +
                            "  unknown: \"An unknown error occurred\"\n"
            );
            logger.info("Fichier de langue anglais par défaut créé");
        } catch (IOException e) {
            logger.error("Échec de la création du fichier de langue anglais par défaut", e);
        }
    }

    /**
     * Crée le fichier de langue français par défaut.
     *
     * @param file le fichier à créer
     */
    private void createDefaultFrenchFile(File file) {
        try {
            Files.writeString(file.toPath(),
                    "# Fichier de langue français (fr-FR)\n" +
                            "general:\n" +
                            "  yes: \"Oui\"\n" +
                            "  no: \"Non\"\n" +
                            "  enabled: \"Activé\"\n" +
                            "  disabled: \"Désactivé\"\n" +
                            "  success: \"Succès\"\n" +
                            "  error: \"Erreur\"\n" +
                            "  warning: \"Avertissement\"\n" +
                            "  info: \"Information\"\n" +
                            "\n" +
                            "bot:\n" +
                            "  startup: \"Le bot démarre...\"\n" +
                            "  shutdown: \"Le bot s'arrête...\"\n" +
                            "  connected: \"Connecté à Discord en tant que {0}\"\n" +
                            "  disconnected: \"Déconnecté de Discord\"\n" +
                            "\n" +
                            "plugins:\n" +
                            "  loading: \"Chargement des plugins...\"\n" +
                            "  loaded: \"{0} plugins chargés\"\n" +
                            "  enabling: \"Activation des plugins...\"\n" +
                            "  enabled: \"{0} plugins activés\"\n" +
                            "  disabling: \"Désactivation des plugins...\"\n" +
                            "  disabled: \"{0} plugins désactivés\"\n" +
                            "  reloading: \"Rechargement des plugins...\"\n" +
                            "  reloaded: \"{0} plugins rechargés\"\n" +
                            "\n" +
                            "commands:\n" +
                            "  help: \"Affiche l'aide des commandes\"\n" +
                            "  reload: \"Recharge le bot ou des plugins spécifiques\"\n" +
                            "  version: \"Affiche la version du bot\"\n" +
                            "  plugins: \"Liste tous les plugins chargés\"\n" +
                            "  language: \"Change la langue du bot\"\n" +
                            "\n" +
                            "errors:\n" +
                            "  command_not_found: \"Commande introuvable : {0}\"\n" +
                            "  plugin_not_found: \"Plugin introuvable : {0}\"\n" +
                            "  no_permission: \"Vous n'avez pas la permission d'utiliser cette commande\"\n" +
                            "  unknown: \"Une erreur inconnue s'est produite\"\n"
            );
            logger.info("Fichier de langue français par défaut créé");
        } catch (IOException e) {
            logger.error("Échec de la création du fichier de langue français par défaut", e);
        }
    }
}