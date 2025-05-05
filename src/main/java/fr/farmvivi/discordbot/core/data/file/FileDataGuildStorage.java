package fr.farmvivi.discordbot.core.data.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.farmvivi.discordbot.core.api.data.GuildStorage;
import fr.farmvivi.discordbot.core.util.Debouncer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implémentation du stockage de guild basée sur des fichiers.
 */
public class FileDataGuildStorage implements GuildStorage {
    private static final Logger logger = LoggerFactory.getLogger(FileDataGuildStorage.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_FILE_NAME = "data.json";

    private final File guildsFolder;
    private final Map<String, Map<String, Object>> guildDataCache = new ConcurrentHashMap<>();
    private final Map<String, Debouncer> guildSaveThrottlers = new ConcurrentHashMap<>();
    private final Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    /**
     * Crée un nouveau stockage de guild basé sur des fichiers.
     *
     * @param guildsFolder le dossier des guildes
     */
    public FileDataGuildStorage(File guildsFolder) {
        this.guildsFolder = guildsFolder;

        if (!guildsFolder.exists()) {
            guildsFolder.mkdirs();
        }
    }

    @Override
    public boolean exists(String key) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String guildId = parts[0];
        String dataKey = parts[1];

        return hasGuildData(guildId, dataKey);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }

        String guildId = parts[0];
        String dataKey = parts[1];

        return getGuildData(guildId, dataKey, type);
    }

    @Override
    public <T> boolean set(String key, T value) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String guildId = parts[0];
        String dataKey = parts[1];

        return setGuildData(guildId, dataKey, value);
    }

    @Override
    public boolean remove(String key) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String guildId = parts[0];
        String dataKey = parts[1];

        return removeGuildData(guildId, dataKey);
    }

    @Override
    public Set<String> getKeys() {
        Set<String> keys = new HashSet<>();

        for (Map.Entry<String, Map<String, Object>> entry : guildDataCache.entrySet()) {
            String guildId = entry.getKey();

            for (String dataKey : entry.getValue().keySet()) {
                keys.add(guildId + ":" + dataKey);
            }
        }

        // Charger également les données non chargées
        File[] guildFolders = guildsFolder.listFiles(File::isDirectory);
        if (guildFolders != null) {
            for (File guildFolder : guildFolders) {
                String guildId = guildFolder.getName();

                if (!guildDataCache.containsKey(guildId)) {
                    // Charger les données guild
                    loadGuildData(guildId);

                    // Ajouter les clés
                    Map<String, Object> guildData = guildDataCache.get(guildId);
                    if (guildData != null) {
                        for (String dataKey : guildData.keySet()) {
                            keys.add(guildId + ":" + dataKey);
                        }
                    }
                }
            }
        }

        return keys;
    }

    @Override
    public boolean clear() {
        boolean success = true;

        File[] guildFolders = guildsFolder.listFiles(File::isDirectory);
        if (guildFolders != null) {
            for (File guildFolder : guildFolders) {
                String guildId = guildFolder.getName();

                if (!clearGuild(guildId)) {
                    success = false;
                }
            }
        }

        guildDataCache.clear();
        guildSaveThrottlers.clear();

        return success;
    }

    @Override
    public boolean save() {
        boolean success = true;

        for (String guildId : guildDataCache.keySet()) {
            if (!saveGuildData(guildId)) {
                success = false;
            }
        }

        return success;
    }

    @Override
    public <T> Optional<T> getGuildData(String guildId, String key, Class<T> type) {
        Map<String, Object> guildData = getGuildDataMap(guildId);

        if (guildData.containsKey(key)) {
            try {
                // Convertir l'objet stocké au type demandé
                String json = gson.toJson(guildData.get(key));
                T value = gson.fromJson(json, type);

                return Optional.ofNullable(value);
            } catch (Exception e) {
                logger.error("Error converting data for guild {} and key {}: {}", guildId, key, e.getMessage());
            }
        }

        return Optional.empty();
    }

    @Override
    public <T> boolean setGuildData(String guildId, String key, T value) {
        Map<String, Object> guildData = getGuildDataMap(guildId);

        // Enregistrer la valeur
        guildData.put(key, value);

        // Sauvegarder avec throttling
        scheduleGuildDataSave(guildId);

        return true;
    }

    @Override
    public boolean removeGuildData(String guildId, String key) {
        Map<String, Object> guildData = getGuildDataMap(guildId);

        if (guildData.containsKey(key)) {
            // Supprimer la valeur
            guildData.remove(key);

            // Sauvegarder avec throttling
            scheduleGuildDataSave(guildId);

            return true;
        }

        return false;
    }

    @Override
    public boolean hasGuildData(String guildId, String key) {
        Map<String, Object> guildData = getGuildDataMap(guildId);

        return guildData.containsKey(key);
    }

    @Override
    public Set<String> getGuildKeys(String guildId) {
        Map<String, Object> guildData = getGuildDataMap(guildId);

        return new HashSet<>(guildData.keySet());
    }

    @Override
    public boolean clearGuild(String guildId) {
        // Supprimer les données en mémoire
        guildDataCache.remove(guildId);

        // Annuler les sauvegardes en attente
        Debouncer debouncer = guildSaveThrottlers.remove(guildId);
        if (debouncer != null) {
            // Rien à faire, le debouncer expirera tout seul
        }

        // Supprimer le dossier guild
        File guildFolder = new File(guildsFolder, guildId);
        if (guildFolder.exists()) {
            try {
                deleteDirectory(guildFolder);
                return true;
            } catch (IOException e) {
                logger.error("Error deleting guild folder for {}: {}", guildId, e.getMessage());
                return false;
            }
        }

        return true;
    }

    // Méthodes privées

    private Map<String, Object> getGuildDataMap(String guildId) {
        // Récupérer ou charger les données guild
        return guildDataCache.computeIfAbsent(guildId, this::loadGuildData);
    }

    private Map<String, Object> loadGuildData(String guildId) {
        File guildFolder = new File(guildsFolder, guildId);
        File dataFile = new File(guildFolder, DATA_FILE_NAME);

        Map<String, Object> guildData = new HashMap<>();

        if (dataFile.exists()) {
            try (FileReader reader = new FileReader(dataFile)) {
                Map<String, Object> loadedData = gson.fromJson(reader, mapType);
                if (loadedData != null) {
                    guildData.putAll(loadedData);
                }
            } catch (Exception e) {
                logger.error("Error loading guild data for {}: {}", guildId, e.getMessage());
            }
        }

        return guildData;
    }

    private void scheduleGuildDataSave(String guildId) {
        Debouncer debouncer = guildSaveThrottlers.computeIfAbsent(guildId, id ->
                new Debouncer(2000, () -> saveGuildData(id))
        );

        debouncer.debounce();
    }

    private boolean saveGuildData(String guildId) {
        Map<String, Object> guildData = guildDataCache.get(guildId);
        if (guildData == null) {
            return true; // Rien à sauvegarder
        }

        File guildFolder = new File(guildsFolder, guildId);
        if (!guildFolder.exists()) {
            guildFolder.mkdirs();
        }

        File dataFile = new File(guildFolder, DATA_FILE_NAME);

        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(guildData, writer);
            return true;
        } catch (Exception e) {
            logger.error("Error saving guild data for {}: {}", guildId, e.getMessage());
            return false;
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new IOException("Failed to delete file: " + file);
                    }
                }
            }
        }

        if (!directory.delete()) {
            throw new IOException("Failed to delete directory: " + directory);
        }
    }
}