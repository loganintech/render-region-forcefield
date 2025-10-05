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
        // Save default config
        saveDefaultConfig();

        // Initialize components
        this.permissionChecker = new RegionPermissionChecker();
        this.forcefieldRenderer = new ForcefieldRenderer(this);

        // Start the periodic update task
        this.updateTask = new ForcefieldUpdateTask(this, permissionChecker, forcefieldRenderer);
        long updateInterval = getConfig().getLong("update-interval-ticks", 20L);
        updateTask.runTaskTimer(this, 0L, updateInterval);

        getLogger().info("RegionForcefield has been enabled!");
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
}
