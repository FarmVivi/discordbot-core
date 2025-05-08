package fr.farmvivi.discordbot.core.api.plugin;

import fr.farmvivi.discordbot.core.api.config.Configuration;
import fr.farmvivi.discordbot.core.api.discord.DiscordAPI;
import fr.farmvivi.discordbot.core.api.event.EventManager;
import fr.farmvivi.discordbot.core.api.language.LanguageManager;
import fr.farmvivi.discordbot.core.api.language.PluginLanguageManager;
import fr.farmvivi.discordbot.core.api.permissions.PermissionManager;
import fr.farmvivi.discordbot.core.api.permissions.PluginPermissionManager;
import fr.farmvivi.discordbot.core.api.storage.DataStorageManager;
import fr.farmvivi.discordbot.core.api.storage.binary.BinaryStorageManager;
import org.slf4j.Logger;

/**
 * Convenience base class for plugins.
 * Plugins can extend this class instead of implementing the Plugin interface directly.
 */
public abstract class AbstractPlugin implements Plugin {
    protected PluginContext context;
    protected Logger logger;
    protected EventManager eventManager;
    protected DiscordAPI discordAPI;
    protected Configuration configuration;
    protected String dataFolder;
    protected LanguageManager languageManager;
    protected DataStorageManager dataStorageManager;
    protected BinaryStorageManager binaryStorageManager;
    protected PermissionManager permissionManager;
    protected PluginPermissionManager pluginPermissionManager;
    protected PluginLanguageManager pluginLanguageManager;

    private PluginStatus status = PluginStatus.LOADED;

    @Override
    public void onLoad(PluginContext context) {
        this.context = context;
        this.logger = context.getLogger();
        this.eventManager = context.getEventManager();
        this.discordAPI = context.getDiscordAPI();
        this.configuration = context.getConfiguration();
        this.dataFolder = context.getDataFolder();
        this.languageManager = context.getLanguageManager();
        this.dataStorageManager = context.getDataStorageManager();
        this.binaryStorageManager = context.getBinaryStorageManager();
        this.permissionManager = context.getPermissionManager();

        this.pluginLanguageManager = new PluginLanguageManager(this, languageManager);
        this.pluginPermissionManager = new PluginPermissionManager(this, permissionManager, languageManager);

        logger.info("Loading {} v{}", getName(), getVersion());
    }

    @Override
    public void onPreEnable() {
        logger.info("Pre-enabling {} v{}", getName(), getVersion());
        // Plugins can override this to configure Discord settings
    }

    @Override
    public void onEnable() {
        logger.info("Enabling {} v{}", getName(), getVersion());
        // Plugins implement their main initialization here
    }

    @Override
    public void onPostEnable() {
        logger.info("Post-enabling {} v{}", getName(), getVersion());
        // Plugins can interact with other plugins here
    }

    @Override
    public void onPreDisable() {
        logger.info("Pre-disabling {} v{}", getName(), getVersion());
        // Plugins prepare for shutdown here
    }

    @Override
    public void onDisable() {
        logger.info("Disabling {} v{}", getName(), getVersion());
        // Désenregistrement automatique de tous les écouteurs
        eventManager.unregisterListener(this);
    }

    @Override
    public void onPostDisable() {
        logger.info("Post-disabling {} v{}", getName(), getVersion());
        // Final cleanup after all plugins have been disabled
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

    /**
     * Gets the plugin-specific permission manager.
     * This provides convenient methods for registering and checking permissions
     * specifically for this plugin.
     *
     * @return the plugin permission manager
     */
    public PluginPermissionManager getPluginPermissionManager() {
        return pluginPermissionManager;
    }

    /**
     * Gets the plugin-specific language manager.
     * This provides convenient methods for translating strings specifically for this plugin.
     *
     * @return the plugin language manager
     */
    public PluginLanguageManager getPluginLanguageManager() {
        return pluginLanguageManager;
    }
}