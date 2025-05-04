package fr.farmvivi.discordbot.core.api.plugin;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import org.slf4j.Logger;

/**
 * Convenience base class for plugins.
 * Plugins can extend this class instead of implementing the Plugin interface directly.
 */
public abstract class AbstractPlugin implements Plugin {
    private PluginStatus status = PluginStatus.LOADED;
    protected PluginContext context;
    protected Logger logger;
    protected EventManager eventManager;
    protected DiscordAPI discordAPI;
    protected Configuration configuration;
    protected String dataFolder;

    @Override
    public void onLoad(PluginContext context) {
        this.context = context;
        this.logger = context.getLogger();
        this.eventManager = context.getEventManager();
        this.discordAPI = context.getDiscordAPI();
        this.configuration = context.getConfiguration();
        this.dataFolder = context.getDataFolder();

        logger.info("Loading {} v{}", getName(), getVersion());
    }

    @Override
    public void onEnable() {
        logger.info("Enabling {} v{}", getName(), getVersion());
    }

    @Override
    public void onDisable() {
        logger.info("Disabling {} v{}", getName(), getVersion());

        // Désenregistrement automatique de tous les écouteurs
        eventManager.unregisterListener(this);
    }

    @Override
    public PluginStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(PluginStatus status) {
        this.status = status;
    }

    /**
     * Gets the plugin context.
     *
     * @return the plugin context
     */
    public PluginContext getContext() {
        return context;
    }
}