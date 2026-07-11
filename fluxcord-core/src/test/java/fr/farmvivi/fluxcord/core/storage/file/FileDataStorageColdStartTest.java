package fr.farmvivi.fluxcord.core.storage.file;

import fr.farmvivi.fluxcord.api.storage.StorageKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests ensuring that data persisted by one process is readable via {@code get()} by a
 * freshly started process (empty in-memory cache) — the scenario used to resume music playback
 * after a restart.
 */
class FileDataStorageColdStartTest {

    @Test
    void getReadsPreviouslyPersistedValueAfterRestart(@TempDir File dir) {
        StorageKey key = StorageKey.guild("123", "music-plugin.playback_state");

        // First process: write a nested value and flush to disk.
        FileDataStorage first = new FileDataStorage(dir, null, 0);
        Map<String, Object> state = new HashMap<>();
        state.put("voiceChannelId", "456");
        state.put("currentPosition", 17500L);
        first.set(key, state);
        assertTrue(first.close(), "flush to disk should succeed");

        // Second process: brand-new instance (empty cache) pointing at the same directory.
        FileDataStorage restarted = new FileDataStorage(dir, null, 0);
        Optional<Map> restored = restarted.get(key, Map.class);

        assertTrue(restored.isPresent(), "cold-start get() must load the value from disk");
        assertEquals("456", restored.get().get("voiceChannelId"));
    }

    @Test
    void coldStartGetOfTypedNumberDoesNotThrow(@TempDir File dir) {
        // When a whole scope is loaded from disk, Gson parses JSON numbers as Double and caches them
        // as such. A subsequent typed get() must convert via the backend instead of blindly casting
        // the cached Double (which previously threw ClassCastException).
        StorageKey key = StorageKey.guild("123", "music-plugin.count");

        FileDataStorage first = new FileDataStorage(dir, null, 0);
        first.set(key, 42L);
        assertTrue(first.close());

        FileDataStorage restarted = new FileDataStorage(dir, null, 0);
        // Load the scope via getAll first so the cache holds a raw Double value.
        restarted.getAll("guild:123");
        Optional<Long> value = restarted.get(key, Long.class);

        assertTrue(value.isPresent(), "typed number should be readable without throwing");
        assertEquals(42L, value.get());
    }
}
