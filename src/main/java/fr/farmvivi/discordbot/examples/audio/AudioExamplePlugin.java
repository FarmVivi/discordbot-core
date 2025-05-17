package fr.farmvivi.discordbot.examples.audio;

import fr.farmvivi.discordbot.core.api.audio.events.AudioFrameMixedEvent;
import fr.farmvivi.discordbot.core.api.event.EventHandler;
import fr.farmvivi.discordbot.core.api.event.EventPriority;
import fr.farmvivi.discordbot.core.api.plugin.AbstractPlugin;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Exemple de plugin audio qui montre comment utiliser le système audio.
 * Ce plugin joue un fichier audio quand un utilisateur rejoint un salon vocal
 * et enregistre l'audio reçu dans un fichier.
 */
public class AudioExamplePlugin extends AbstractPlugin {
    private final Map<String, MySendHandler> sendHandlers = new HashMap<>();
    private final Map<String, MyReceiveHandler> receiveHandlers = new HashMap<>();

    @Override
    public String getName() {
        return "AudioExample";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void onEnable() {
        logger.info("Plugin exemple audio activé !");
    }

    @Override
    public void onDisable() {
        // Le service AudioService gère automatiquement le nettoyage des connexions audio
        // lorsqu'un plugin est désactivé, mais on peut aussi le faire manuellement ici

        // Ferme manuellement les handlers d'envoi
        for (MySendHandler handler : sendHandlers.values()) {
            handler.cleanup();
        }
        sendHandlers.clear();

        // Ferme manuellement les handlers de réception
        for (MyReceiveHandler handler : receiveHandlers.values()) {
            handler.cleanup();
        }
        receiveHandlers.clear();

        logger.info("Plugin exemple audio désactivé !");
    }

    /**
     * Gère l'événement lorsqu'un utilisateur rejoint un salon vocal.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onGuildVoiceJoin(GuildVoiceUpdateEvent event) {
        // Vérifie si l'utilisateur a rejoint un salon vocal (channelJoined != null && channelLeft == null)
        if (event.getChannelJoined() == null) return;

        Guild guild = event.getGuild();
        String guildId = guild.getId();

        // Si on n'a pas déjà un handler pour cette guilde, on en crée un
        if (!sendHandlers.containsKey(guildId)) {
            // Crée le handler d'envoi audio
            File audioFile = new File(getContext().getDataFolder(), "welcome.wav");
            MySendHandler sendHandler = new MySendHandler(audioFile);

            // Crée le handler de réception audio
            File outputDir = new File(getContext().getDataFolder(), "recordings");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            MyReceiveHandler receiveHandler = new MyReceiveHandler(new File(outputDir, "recording_" + guildId + ".wav"));

            // Connecte au salon vocal
            VoiceChannel voiceChannel = event.getChannelJoined().asVoiceChannel();
            guild.getAudioManager().openAudioConnection(voiceChannel);

            // Enregistre les handlers avec le service audio
            audioService.registerSendHandler(guild, this, sendHandler, 80, 60);
            audioService.registerReceiveHandler(guild, this, receiveHandler);

            // Stocke les handlers pour plus tard
            sendHandlers.put(guildId, sendHandler);
            receiveHandlers.put(guildId, receiveHandler);

            logger.info("Connecté au salon vocal {} dans la guilde {}", voiceChannel.getName(), guild.getName());
        }
    }

    /**
     * Gère l'événement lorsqu'un utilisateur quitte un salon vocal.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onGuildVoiceLeave(GuildVoiceUpdateEvent event) {
        // Vérifie si l'utilisateur a quitté un salon vocal (channelLeft != null)
        if (event.getChannelLeft() == null) return;

        Guild guild = event.getGuild();
        String guildId = guild.getId();

        // Si tous les utilisateurs sont partis (sauf le bot), on ferme la connexion
        // Vérifie si le salon vocal est maintenant vide (à part le bot)
        if (event.getChannelLeft().getMembers().size() <= 1) {
            // Désenregistre les handlers
            if (sendHandlers.containsKey(guildId)) {
                audioService.deregisterSendHandler(guild, this);
                MySendHandler handler = sendHandlers.remove(guildId);
                handler.cleanup();
            }

            if (receiveHandlers.containsKey(guildId)) {
                audioService.deregisterReceiveHandler(guild, this);
                MyReceiveHandler handler = receiveHandlers.remove(guildId);
                handler.cleanup();
            }

            // Ferme la connexion audio
            guild.getAudioManager().closeAudioConnection();
            logger.info("Déconnecté du salon vocal dans la guilde {}", guild.getName());
        }
    }

    /**
     * Gère l'événement lorsqu'un frame audio est mixé.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAudioFrameMixed(AudioFrameMixedEvent event) {
        // On peut utiliser cet événement pour suivre l'activité audio
        // Par exemple, compter le nombre de sources actives ou surveiller les performances

        // Affiche les informations toutes les 100 frames (environ 2 secondes)
        // pour éviter de spammer les logs
        if (Math.random() < 0.01) {
            logger.debug("Frame audio mixé dans la guilde {}: {} sources actives, mode bypass: {}, contient de l'audio: {}",
                    event.getGuild().getName(),
                    event.getActiveSourceCount(),
                    event.isBypassMode() ? "oui" : "non",
                    event.containsAudio() ? "oui" : "non");
        }
    }

    /**
     * Exemple d'un handler d'envoi audio qui lit un fichier audio.
     */
    private static class MySendHandler implements AudioSendHandler {
        private final File audioFile;
        private FileInputStream stream;
        private byte[] buffer;
        private int bufferIndex = 0;
        private boolean done = false;

        public MySendHandler(File audioFile) {
            this.audioFile = audioFile;

            try {
                this.stream = new FileInputStream(audioFile);
                this.buffer = new byte[stream.available()];
                stream.read(buffer);
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
                done = true;
            }
        }

        @Override
        public boolean canProvide() {
            return !done && bufferIndex < buffer.length;
        }

        @Override
        public ByteBuffer provide20MsAudio() {
            // Copie 20ms d'audio (960 échantillons * 2 octets * 2 canaux = 3840 octets)
            int bytesToRead = Math.min(3840, buffer.length - bufferIndex);
            byte[] audio = new byte[bytesToRead];
            System.arraycopy(buffer, bufferIndex, audio, 0, bytesToRead);
            bufferIndex += bytesToRead;

            // Si on a atteint la fin du fichier
            if (bufferIndex >= buffer.length) {
                done = true;
            }

            return ByteBuffer.wrap(audio);
        }

        @Override
        public boolean isOpus() {
            // Notre exemple utilise du PCM, pas de l'Opus
            return false;
        }

        public void cleanup() {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Exemple d'un handler de réception audio qui enregistre l'audio dans un fichier.
     */
    private static class MyReceiveHandler implements AudioReceiveHandler {
        private final File outputFile;

        public MyReceiveHandler(File outputFile) {
            this.outputFile = outputFile;

            // Initialisation de l'enregistrement (dans un cas réel, on ouvrirait un flux de sortie)
            System.out.println("Démarrage de l'enregistrement dans " + outputFile.getAbsolutePath());
        }

        @Override
        public boolean canReceiveCombined() {
            return true;
        }

        @Override
        public boolean canReceiveUser() {
            return true;
        }

        @Override
        public void handleCombinedAudio(CombinedAudio combinedAudio) {
            // Traitement de l'audio combiné
            // Dans un cas réel, on écrirait les données dans un fichier

            // Récupère les données audio
            byte[] audio = combinedAudio.getAudioData(1.0); // Volume normal

            // Traitement des données (dans un cas réel, on écrirait dans un fichier)
            // System.out.println("Données audio reçues : " + audio.length + " octets");
        }

        @Override
        public void handleUserAudio(UserAudio userAudio) {
            // Traitement de l'audio par utilisateur
            // Utile pour savoir qui parle et enregistrer séparément par utilisateur

            String userId = userAudio.getUser().getId();
            byte[] audio = userAudio.getAudioData(1.0);

            // Traitement des données (dans un cas réel, on écrirait dans un fichier par utilisateur)
            // System.out.println("Données audio de l'utilisateur " + userId + " : " + audio.length + " octets");
        }

        public void cleanup() {
            // Fermeture des ressources d'enregistrement
            System.out.println("Arrêt de l'enregistrement dans " + outputFile.getAbsolutePath());
        }
    }
}