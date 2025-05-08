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

import java.io.File;

/**
 * Convenience base class for plugins.
 * Provides common functionality and access to core services.
 */
public abstract class AbstractPlugin implements Plugin {
    // Core services
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

    // Plugin-specific managers
    protected PluginPermissionManager pluginPermissionManager;
    protected PluginLanguageManager pluginLanguageManager;

    // Plugin state
    private PluginLifecycle lifecycle = PluginLifecycle.DISCOVERED;

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

        // Initialize plugin-specific managers
        this.pluginLanguageManager = new PluginLanguageManager(this, languageManager);
        this.pluginPermissionManager = new PluginPermissionManager(this, permissionManager, languageManager);

        // Create data directory if it doesn't exist
        File dataDir = new File(dataFolder);
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            logger.warn("Failed to create data directory for plugin: {}", getName());
        }

        logger.info("Loading {} v{}", getName(), getVersion());
    }

    @Override
    public void onPreEnable() {
        logger.info("Pre-enabling {} v{}", getName(), getVersion());
    }

    @Override
    public void onEnable() {
        logger.info("Enabling {} v{}", getName(), getVersion());
    }

    @Override
    public void onPostEnable() {
        logger.info("Post-enabling {} v{}", getName(), getVersion());
    }

    @Override
    public void onPreDisable() {
        logger.info("Pre-disabling {} v{}", getName(), getVersion());
    }

    @Override
    public void onDisable() {
        logger.info("Disabling {} v{}", getName(), getVersion());

        // Automatically unregister all event handlers
        if (eventManager != null) {
            eventManager.unregisterListener(this);
            eventManager.unregisterAll(this);
        }
    }

    @Override
    public void onPostDisable() {
        logger.info("Post-disabling {} v{}", getName(), getVersion());
    }

    @Override
    public PluginLifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public void setLifecycle(PluginLifecycle lifecycle) {
        this.lifecycle = lifecycle;
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

    /**
     * Returns whether this plugin is currently enabled.
     *
     * @return true if this plugin is enabled
     */
    public boolean isEnabled() {
        return lifecycle == PluginLifecycle.ENABLED;
    }
}