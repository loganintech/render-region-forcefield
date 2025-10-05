package loganintech.regionforcefield.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import loganintech.regionforcefield.RegionForcefieldPlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Checks whether players have permission to enter WorldGuard regions.
 */
public class RegionPermissionChecker {

    private final RegionForcefieldPlugin plugin;
    private final WorldGuardPlugin worldGuard;

    /**
     * Creates a new region permission checker.
     *
     * @param plugin the plugin instance
     */
    public RegionPermissionChecker(@NotNull RegionForcefieldPlugin plugin) {
        this.plugin = plugin;
        this.worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
    }

    /**
     * Gets all regions in a world that the specified player cannot enter.
     *
     * @param player the player to check
     * @param world  the world to check regions in
     * @return a set of regions the player cannot enter
     */
    @NotNull
    public Set<ProtectedRegion> getBlockedRegions(@NotNull Player player, @NotNull World world) {
        Set<ProtectedRegion> blockedRegions = new HashSet<>();

        try {
            // Get the region manager for this world
            RegionManager regionManager = WorldGuard.getInstance()
                    .getPlatform()
                    .getRegionContainer()
                    .get(BukkitAdapter.adapt(world));

            if (regionManager == null) {
                return blockedRegions;
            }

            // Convert Bukkit player to WorldGuard LocalPlayer
            if (worldGuard == null) {
                plugin.getLogger().warning("WorldGuard plugin reference is null!");
                return blockedRegions;
            }

            LocalPlayer localPlayer = worldGuard.wrapPlayer(player);

            // Check each region
            for (ProtectedRegion region : regionManager.getRegions().values()) {
                if (!canEnterRegion(localPlayer, region)) {
                    blockedRegions.add(region);
                    plugin.debug("Player " + player.getName() + " blocked from region: " + region.getId());
                }
            }

            if (!blockedRegions.isEmpty()) {
                plugin.debug("Found " + blockedRegions.size() + " blocked regions for " + player.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking blocked regions: " + e.getMessage());
            e.printStackTrace();
        }

        return blockedRegions;
    }

    /**
     * Checks if a player can actually enter a region.
     * Takes into account entry deny flag, bypass permissions, and member/owner status.
     *
     * @param player the player to check
     * @param region the region to check
     * @return true if the player CAN enter (no forcefield), false if blocked (show forcefield)
     */
    private boolean canEnterRegion(@NotNull LocalPlayer player, @NotNull ProtectedRegion region) {
        // Check the ENTRY flag
        if (region.getFlag(Flags.ENTRY) == com.sk89q.worldguard.protection.flags.StateFlag.State.DENY) {
            // Check if player has bypass permission (includes ops)
            if (player.hasPermission("worldguard.region.bypass." + region.getId()) ||
                player.hasPermission("worldguard.region.bypass.*")) {
                return true;  // Can enter (has bypass), no forcefield
            }

            // Check if the player is a member or owner of the region
            if (region.isMember(player) || region.isOwner(player)) {
                return true;  // Can enter (is member/owner), no forcefield
            }

            // Player cannot enter, show forcefield
            return false;
        }

        // If ENTRY is not explicitly denied, player can enter
        return true;
    }
}
