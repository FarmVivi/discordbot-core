package fr.farmvivi.discordbot.core.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Debouncer {
    private final long delayMillis;
    private final Runnable action;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;

    /**
     * Crée un debouncer avec un délai spécifié et une action à exécuter.
     *
     * @param delayMillis Le délai en millisecondes
     * @param action      L'action à exécuter
     */
    public Debouncer(long delayMillis, Runnable action) {
        this.delayMillis = delayMillis;
        this.action = action;
    }

    /**
     * Déclenche l'action après le délai spécifié.
     * Si cette méthode est appelée plusieurs fois rapidement, l'action ne sera exécutée qu'une seule fois après le délai spécifié.
     */
    public synchronized void debounce() {
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
        future = executor.schedule(action, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Arrête l'exécuteur sous-jacent.
     */
    public void shutdown() {
        executor.shutdownNow();
    }
}
