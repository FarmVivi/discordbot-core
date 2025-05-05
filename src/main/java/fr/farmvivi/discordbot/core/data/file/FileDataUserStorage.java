package fr.farmvivi.discordbot.core.data.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.farmvivi.discordbot.core.api.data.UserStorage;
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
 * Implémentation du stockage utilisateur basée sur des fichiers.
 */
public class FileDataUserStorage implements UserStorage {
    private static final Logger logger = LoggerFactory.getLogger(FileDataUserStorage.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_FILE_NAME = "data.json";

    private final File usersFolder;
    private final Map<String, Map<String, Object>> userDataCache = new ConcurrentHashMap<>();
    private final Map<String, Debouncer> userSaveThrottlers = new ConcurrentHashMap<>();
    private final Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    /**
     * Crée un nouveau stockage utilisateur basé sur des fichiers.
     *
     * @param usersFolder le dossier des utilisateurs
     */
    public FileDataUserStorage(File usersFolder) {
        this.usersFolder = usersFolder;

        if (!usersFolder.exists()) {
            usersFolder.mkdirs();
        }
    }

    @Override
    public boolean exists(String key) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String userId = parts[0];
        String dataKey = parts[1];

        return hasUserData(userId, dataKey);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }

        String userId = parts[0];
        String dataKey = parts[1];

        return getUserData(userId, dataKey, type);
    }

    @Override
    public <T> boolean set(String key, T value) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String userId = parts[0];
        String dataKey = parts[1];

        return setUserData(userId, dataKey, value);
    }

    @Override
    public boolean remove(String key) {
        String[] parts = key.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        String userId = parts[0];
        String dataKey = parts[1];

        return removeUserData(userId, dataKey);
    }

    @Override
    public Set<String> getKeys() {
        Set<String> keys = new HashSet<>();

        for (Map.Entry<String, Map<String, Object>> entry : userDataCache.entrySet()) {
            String userId = entry.getKey();

            for (String dataKey : entry.getValue().keySet()) {
                keys.add(userId + ":" + dataKey);
            }
        }

        // Charger également les données non chargées
        File[] userFolders = usersFolder.listFiles(File::isDirectory);
        if (userFolders != null) {
            for (File userFolder : userFolders) {
                String userId = userFolder.getName();

                if (!userDataCache.containsKey(userId)) {
                    // Charger les données utilisateur
                    loadUserData(userId);

                    // Ajouter les clés
                    Map<String, Object> userData = userDataCache.get(userId);
                    if (userData != null) {
                        for (String dataKey : userData.keySet()) {
                            keys.add(userId + ":" + dataKey);
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

        File[] userFolders = usersFolder.listFiles(File::isDirectory);
        if (userFolders != null) {
            for (File userFolder : userFolders) {
                String userId = userFolder.getName();

                if (!clearUser(userId)) {
                    success = false;
                }
            }
        }

        userDataCache.clear();
        userSaveThrottlers.clear();

        return success;
    }

    @Override
    public boolean save() {
        boolean success = true;

        for (String userId : userDataCache.keySet()) {
            if (!saveUserData(userId)) {
                success = false;
            }
        }

        return success;
    }

    @Override
    public <T> Optional<T> getUserData(String userId, String key, Class<T> type) {
        Map<String, Object> userData = getUserDataMap(userId);

        if (userData.containsKey(key)) {
            try {
                // Convertir l'objet stocké au type demandé
                String json = gson.toJson(userData.get(key));
                T value = gson.fromJson(json, type);

                return Optional.ofNullable(value);
            } catch (Exception e) {
                logger.error("Error converting data for user {} and key {}: {}", userId, key, e.getMessage());
            }
        }

        return Optional.empty();
    }

    @Override
    public <T> boolean setUserData(String userId, String key, T value) {
        Map<String, Object> userData = getUserDataMap(userId);

        // Enregistrer la valeur
        userData.put(key, value);

        // Sauvegarder avec throttling
        scheduleUserDataSave(userId);

        return true;
    }

    @Override
    public boolean removeUserData(String userId, String key) {
        Map<String, Object> userData = getUserDataMap(userId);

        if (userData.containsKey(key)) {
            // Supprimer la valeur
            userData.remove(key);

            // Sauvegarder avec throttling
            scheduleUserDataSave(userId);

            return true;
        }

        return false;
    }

    @Override
    public boolean hasUserData(String userId, String key) {
        Map<String, Object> userData = getUserDataMap(userId);

        return userData.containsKey(key);
    }

    @Override
    public Set<String> getUserKeys(String userId) {
        Map<String, Object> userData = getUserDataMap(userId);

        return new HashSet<>(userData.keySet());
    }

    @Override
    public boolean clearUser(String userId) {
        // Supprimer les données en mémoire
        userDataCache.remove(userId);

        // Annuler les sauvegardes en attente
        Debouncer debouncer = userSaveThrottlers.remove(userId);
        if (debouncer != null) {
            // Rien à faire, le debouncer expirera tout seul
        }

        // Supprimer le dossier utilisateur
        File userFolder = new File(usersFolder, userId);
        if (userFolder.exists()) {
            try {
                deleteDirectory(userFolder);
                return true;
            } catch (IOException e) {
                logger.error("Error deleting user folder for {}: {}", userId, e.getMessage());
                return false;
            }
        }

        return true;
    }

    // Méthodes privées

    private Map<String, Object> getUserDataMap(String userId) {
        // Récupérer ou charger les données utilisateur
        return userDataCache.computeIfAbsent(userId, this::loadUserData);
    }

    private Map<String, Object> loadUserData(String userId) {
        File userFolder = new File(usersFolder, userId);
        File dataFile = new File(userFolder, DATA_FILE_NAME);

        Map<String, Object> userData = new HashMap<>();

        if (dataFile.exists()) {
            try (FileReader reader = new FileReader(dataFile)) {
                Map<String, Object> loadedData = gson.fromJson(reader, mapType);
                if (loadedData != null) {
                    userData.putAll(loadedData);
                }
            } catch (Exception e) {
                logger.error("Error loading user data for {}: {}", userId, e.getMessage());
            }
        }

        return userData;
    }

    private void scheduleUserDataSave(String userId) {
        Debouncer debouncer = userSaveThrottlers.computeIfAbsent(userId, id ->
                new Debouncer(2000, () -> saveUserData(id))
        );

        debouncer.debounce();
    }

    private boolean saveUserData(String userId) {
        Map<String, Object> userData = userDataCache.get(userId);
        if (userData == null) {
            return true; // Rien à sauvegarder
        }

        File userFolder = new File(usersFolder, userId);
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }

        File dataFile = new File(userFolder, DATA_FILE_NAME);

        try (FileWriter writer = new FileWriter(dataFile)) {
            gson.toJson(userData, writer);
            return true;
        } catch (Exception e) {
            logger.error("Error saving user data for {}: {}", userId, e.getMessage());
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