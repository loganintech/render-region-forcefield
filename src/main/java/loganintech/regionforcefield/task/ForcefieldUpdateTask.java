package loganintech.regionforcefield.task;

import loganintech.regionforcefield.RegionForcefieldPlugin;
import loganintech.regionforcefield.forcefield.ForcefieldRenderer;
import loganintech.regionforcefield.region.RegionPermissionChecker;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Periodic task that updates and renders forcefields for all online players.
 */
public class ForcefieldUpdateTask extends BukkitRunnable {

    private final RegionForcefieldPlugin plugin;
    private final RegionPermissionChecker permissionChecker;
    private final ForcefieldRenderer forcefieldRenderer;
    private final int maxRenderDistance;

    /**
     * Creates a new forcefield update task.
     *
     * @param plugin             the plugin instance
     * @param permissionChecker  the permission checker
     * @param forcefieldRenderer the forcefield renderer
     */
    public ForcefieldUpdateTask(@NotNull RegionForcefieldPlugin plugin,
                                @NotNull RegionPermissionChecker permissionChecker,
                                @NotNull ForcefieldRenderer forcefieldRenderer) {
        this.plugin = plugin;
        this.permissionChecker = permissionChecker;
        this.forcefieldRenderer = forcefieldRenderer;
        this.maxRenderDistance = plugin.getConfig().getInt("max-render-distance", 100);
    }

    @Override
    public void run() {
        try {
            // Iterate through all online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                // Get all regions the player should see forcefields for
                Set<ProtectedRegion> blockedRegions = permissionChecker.getBlockedRegions(player, player.getWorld());

                if (!blockedRegions.isEmpty()) {
                    plugin.debug("Processing " + blockedRegions.size() + " blocked regions for " + player.getName());
                }

                // Collect all blocks that should be rendered for this player
                Set<Location> allBlocks = new HashSet<>();
                int rendered = 0;

                for (ProtectedRegion region : blockedRegions) {
                    if (isRegionNearPlayer(player, region)) {
                        Set<Location> regionBlocks = forcefieldRenderer.renderForcefield(player, region, player.getWorld());
                        allBlocks.addAll(regionBlocks);
                        rendered++;
                    }
                }

                // Update the player's blocks (remove old ones, keep new ones)
                forcefieldRenderer.updateBlocks(player, allBlocks);

                if (rendered > 0) {
                    plugin.debug("Rendered " + rendered + " forcefields (" + allBlocks.size() + " blocks) for " + player.getName());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error in forcefield update task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if a region is near enough to a player to render.
     *
     * @param player the player
     * @param region the region
     * @return true if the region should be rendered for this player
     */
    private boolean isRegionNearPlayer(@NotNull Player player, @NotNull ProtectedRegion region) {
        // Get player's position
        double playerX = player.getLocation().getX();
        double playerY = player.getLocation().getY();
        double playerZ = player.getLocation().getZ();

        // Get region's center (approximate)
        double regionCenterX = (region.getMinimumPoint().x() + region.getMaximumPoint().x()) / 2.0;
        double regionCenterY = (region.getMinimumPoint().y() + region.getMaximumPoint().y()) / 2.0;
        double regionCenterZ = (region.getMinimumPoint().z() + region.getMaximumPoint().z()) / 2.0;

        // Calculate distance
        double distance = Math.sqrt(
            Math.pow(playerX - regionCenterX, 2) +
            Math.pow(playerY - regionCenterY, 2) +
            Math.pow(playerZ - regionCenterZ, 2)
        );

        return distance <= maxRenderDistance;
    }
}
