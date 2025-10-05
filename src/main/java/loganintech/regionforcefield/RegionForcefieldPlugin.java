package loganintech.regionforcefield;

import loganintech.regionforcefield.forcefield.ForcefieldRenderer;
import loganintech.regionforcefield.region.RegionPermissionChecker;
import loganintech.regionforcefield.task.ForcefieldUpdateTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main plugin class for RegionForcefield.
 * Displays particle forcefields around WorldGuard regions that players cannot enter.
 */
public final class RegionForcefieldPlugin extends JavaPlugin {

    private RegionPermissionChecker permissionChecker;
    private ForcefieldRenderer forcefieldRenderer;
    private ForcefieldUpdateTask updateTask;

    @Override
    public void onEnable() {
        try {
            // Save default config
            saveDefaultConfig();

            // Check for WorldGuard
            if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
                getLogger().severe("WorldGuard not found! Disabling plugin.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            // Initialize components
            this.permissionChecker = new RegionPermissionChecker(this);
            this.forcefieldRenderer = new ForcefieldRenderer(this);

            // Start the periodic update task
            this.updateTask = new ForcefieldUpdateTask(this, permissionChecker, forcefieldRenderer);
            long updateInterval = getConfig().getLong("update-interval-ticks", 20L);
            updateTask.runTaskTimer(this, 0L, updateInterval);

            getLogger().info("RegionForcefield has been enabled!");
            getLogger().info("Update interval: " + updateInterval + " ticks");
            getLogger().info("Max render distance: " + getConfig().getInt("max-render-distance", 100) + " blocks");
        } catch (Exception e) {
            getLogger().severe("Failed to enable RegionForcefield: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Cancel the update task
        if (updateTask != null) {
            updateTask.cancel();
        }

        getLogger().info("RegionForcefield has been disabled!");
    }

    /**
     * Gets the region permission checker.
     *
     * @return the permission checker
     */
    @NotNull
    public RegionPermissionChecker getPermissionChecker() {
        return permissionChecker;
    }

    /**
     * Gets the forcefield renderer.
     *
     * @return the forcefield renderer
     */
    @NotNull
    public ForcefieldRenderer getForcefieldRenderer() {
        return forcefieldRenderer;
    }

    /**
     * Logs a debug message if debug mode is enabled.
     *
     * @param message the message to log
     */
    public void debug(@NotNull String message) {
        if (getConfig().getBoolean("debug", false)) {
            getLogger().info("[DEBUG] " + message);
        }
    }
}
