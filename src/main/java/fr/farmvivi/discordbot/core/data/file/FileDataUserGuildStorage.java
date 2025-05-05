package fr.farmvivi.discordbot.core.data.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.farmvivi.discordbot.core.api.data.UserGuildStorage;
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
 * Implémentation du stockage par utilisateur et par guild basée sur des fichiers.
 */
public class FileDataUserGuildStorage implements UserGuildStorage {
    private static final Logger logger = LoggerFactory.getLogger(FileDataUserGuildStorage.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_FILE_NAME = "data.json";

    private final File userGuildsFolder;
    private final Map<String, Map<String, Map<String, Object>>> userGuildDataCache = new ConcurrentHashMap<>();
    private final Map<String, Debouncer> userGuildSaveThrottlers = new ConcurrentHashMap<>();
    private final Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    /**
     * Crée un nouveau stockage par utilisateur et par guild basé sur des fichiers.
     *
     * @param userGuildsFolder le dossier des données
     */
    public FileDataUserGuildStorage(File userGuildsFolder) {
        this.userGuildsFolder = userGuildsFolder;

        if (!userGuildsFolder.exists()) {
            userGuildsFolder.mkdirs();
        }
    }

    @Override
    public boolean exists(String key) {
        String[] parts = key.split(":", 3);
        if (parts.length != 3) {
            return false;
        }

        String userId = parts[0];
        String guildId = parts[1];
        String dataKey = parts[2];

        return hasUserGuildData(userId, guildId, dataKey);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String[] parts = key.split(":", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }

        String userId = parts[0];
        String guildId = parts[1];
        String dataKey = parts[2];

        return getUserGuildData(userId, guildId, dataKey, type);
    }

    @Override
    public <T> boolean set(String key, T value) {
        String[] parts = key.split(":", 3);
        if (parts.length != 3) {
            return false;
        }

        String userId = parts[0];
        String guildId = parts[1];
        String dataKey = parts[2];

        return setUserGuildData(userId, guildId, dataKey, value);
    }

    @Override
    public boolean remove(String key) {
        String[] parts = key.split(":", 3);
        if (parts.length != 3) {
            return false;
        }

        String userId = parts[0];
        String guildId = parts[1];
        String dataKey = parts[2];

        return removeUserGuildData(userId, guildId, dataKey);
    }

    @Override
    public Set<String> getKeys() {
        Set<String> keys = new HashSet<>();

        for (Map.Entry<String, Map<String, Map<String, Object>>> userEntry : userGuildDataCache.entrySet()) {
            String userId = userEntry.getKey();

            for (Map.Entry<String, Map<String, Object>> guildEntry : userEntry.getValue().entrySet()) {
                String guildId = guildEntry.getKey();

                for (String dataKey : guildEntry.getValue().keySet()) {
                    keys.add(userId + ":" + guildId + ":" + dataKey);
                }
            }
        }

        // Charger également les données non chargées
        File[] userFolders = userGuildsFolder.listFiles(File::isDirectory);
        if (userFolders != null) {
            for (File userFolder : userFolders) {
                String userId = userFolder.getName();

                File[] guildFolders = userFolder.listFiles(File::isDirectory);
                if (guildFolders != null) {
                    for (File guildFolder : guildFolders) {
                        String guildId = guildFolder.getName();

                        if (!hasUserGuildCache(userId, guildId)) {
                            // Charger les données utilisateur-guilde
                            loadUserGuildData(userId, guildId);

                            // Ajouter les clés
                            Map<String, Object> userGuildData = getUserGuildDataMap(userId, guildId);
                            for (String dataKey : userGuildData.keySet()) {
                                keys.add(userId + ":" + guildId + ":" + dataKey);
                            }
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

        File[] userFolders = userGuildsFolder.listFiles(File::isDirectory);
        if (userFolders != null) {
            for (File userFolder : userFolders) {
                try {
                    deleteDirectory(userFolder);
                } catch (IOException e) {
                    logger.error("Error deleting user folder: {}", userFolder.getName(), e);
                    success = false;
                }
            }
        }

        userGuildDataCache.clear();
        userGuildSaveThrottlers.clear();

        return success;
    }

    @Override
    public boolean save() {
        boolean success = true;

        for (String userId : userGuildDataCache.keySet()) {
            Map<String, Map<String, Object>> guildDataMap = userGuildDataCache.get(userId);

            for (String guildId : guildDataMap.keySet()) {
                if (!saveUserGuildData(userId, guildId)) {
                    success = false;
                }
            }
        }

        return success;
    }

    @Override
    public <T> Optional<T> getUserGuildData(String userId, String guildId, String key, Class<T> type) {
        Map<String, Object> userGuildData = getUserGuildDataMap(userId, guildId);

        if (userGuildData.containsKey(key)) {
            try {
                // Convertir l'objet stocké au type demandé
                String json = gson.toJson(userGuildData.get(key));
                T value = gson.fromJson(json, type);

                return Optional.ofNullable(value);
            } catch (Exception e) {
                logger.error("Error converting data for user {} in guild {} and key {}: {}",
                        userId, guildId, key, e.getMessage());
            }
        }

        return Optional.empty();
    }

    @Override
    public <T> boolean setUserGuildData(String userId, String guildId, String key, T value) {
        Map<String, Object> userGuildData = getUserGuildDataMap(userId, guildId);

        // Enregistrer la valeur
        userGuildData.put(key, value);

        // Sauvegarder avec throttling
        scheduleUserGuildDataSave(userId, guildId);

        return true;
    }

    @Override
    public boolean removeUserGuildData(String userId, String guildId, String key) {
        Map<String, Object> userGuildData = getUserGuildDataMap(userId, guildId);

        if (userGuildData.containsKey(key)) {
            // Supprimer la valeur
            userGuildData.remove(key);

            // Sauvegarder avec throttling
            scheduleUserGuildDataSave(userId, guildId);

            return true;
        }

        return false;
    }

    @Override
    public boolean hasUserGuildData(String userId, String guildId, String key) {
        Map<String, Object> userGuildData = getUserGuildDataMap(userId, guildId);

        return userGuildData.containsKey(key);
    }

    @Override
    public Set<String> getUserGuildKeys(String userId, String guildId) {
        Map<String, Object> userGuildData = getUserGuildDataMap(userId, guildId);

        return new HashSet<>(userGuildData.keySet());
    }

    @Override
    public boolean clearUserGuild(String userId, String guildId) {
        // Supprimer les données en mémoire
        Map<String, Map<String, Object>> guildMap = userGuildDataCache.get(userId);
        if (guildMap != null) {
            guildMap.remove(guildId);

            // Si c'était la dernière guilde pour cet utilisateur, supprimer l'entrée utilisateur
            if (guildMap.isEmpty()) {
                userGuildDataCache.remove(userId);
            }
        }

        // Annuler les sauvegardes en attente
        String cacheKey = getCacheKey(userId, guildId);
        Debouncer debouncer = userGuildSaveThrottlers.remove(cacheKey);
        if (debouncer != null) {
            // Rien à faire, le debouncer expirera tout seul
        }

        // Supprimer le dossier utilisateur-guilde
        File userFolder = new File(userGuildsFolder, userId);
        if (userFolder.exists()) {
            File guildFolder = new File(userFolder, guildId);
            if (guildFolder.exists()) {
                try {
                    deleteDirectory(guildFolder);

                    // Si c'était le dernier dossier guilde pour cet utilisateur, supprimer le dossier utilisateur
                    if (userFolder.listFiles() == null || userFolder.listFiles().length == 0) {
                        userFolder.delete();
                    }

                    return true;
                } catch (IOException e) {
                    logger.error("Error deleting user-guild folder for {}:{}: {}",
                            userId, guildId, e.getMessage());
                    return false;
                }
            }
        }

        return true;
    }

    // Méthodes privées

    private boolean hasUserGuildCache(String userId, String guildId) {
        Map<String, Map<String, Object>> guildMap = userGuildDataCache.get(userId);
        if (guildMap == null) {
            return false;
        }

        return guildMap.containsKey(guildId);
    }

    private Map<String, Object> getUserGuildDataMap(String userId, String guildId) {
        // Récupérer la carte des guildes pour cet utilisateur
        Map<String, Map<String, Object>> guildMap = userGuildDataCache.computeIfAbsent(
                userId, k -> new ConcurrentHashMap<>());

        // Récupérer ou charger les données utilisateur-guilde
        return guildMap.computeIfAbsent(guildId, g -> loadUserGuildData(userId, g));
    }

    private Map<String, Object> loadUserGuildData(String userId, String guildId) {
        File userFolder = new File(userGuildsFolder, userId);
        File guildFolder = new File(userFolder, guildId);
        File dataFile = new File(guildFolder, DATA_FILE_NAME);

        Map<String, Object> userGuildData = new HashMap<>();

        if (dataFile.exists()) {
            try (FileReader reader = new FileReader(dataFile)) {
                Map<String, Object> loadedData = gson.fromJson(reader, mapType);
                if (loadedData != null) {
                    userGuildData.putAll(loadedData);
                }
            } catch (Exception e) {
                logger.error("Error loading user-guild data for {}:{}: {}",
                        userId, guildId, e.getMessage());
            }
        }

        return userGuildData;
    }

    private String getCacheKey(String userId, String guildId) {
        return userId + ":" + guildId;
    }

    private void scheduleUserGuildDataSave(String userId, String guildId) {
        String cacheKey = getCacheKey(userId, guildId);

        Debouncer debouncer = userGuildSaveThrottlers.computeIfAbsent(cacheKey, id ->
                new Debouncer(2000, () -> saveUserGuildData(userId, guildId))
        );

        debouncer.debounce();
    }

    private boolean saveUserGuildData(String userId, String guildId) {
        Map<String, Map<String, Object>> guildMap = userGuildDataCache.get(userId);
        if (guildMap == null) {
            return true; // Rien à sauvegarder
        }

        Map<String, Object> userGuildData = guildMap.get(guildId);
        if (userGuildData == null) {
            return true; // Rien à sauvegarder
        }

        File userFolder = new File(userGuildsFolder, userId);
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }

        File guildFolder = new File(userFolder, guildId);
        if (!guildFolder.exists()) {
            guildFolder.mkdirs();
        }

        File dataFile = new File(guildFolder, DATA_FILE_NAME);

        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(userGuildData, writer);
            return true;
        } catch (Exception e) {
            logger.error("Error saving user-guild data for {}:{}: {}",
                    userId, guildId, e.getMessage());
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