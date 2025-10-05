package loganintech.regionforcefield.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Checks whether players have permission to enter WorldGuard regions.
 */
public class RegionPermissionChecker {

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

        // Get the region manager for this world
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(world));

        if (regionManager == null) {
            return blockedRegions;
        }

        // Convert Bukkit player to WorldGuard LocalPlayer
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        // Check each region
        for (ProtectedRegion region : regionManager.getRegions().values()) {
            if (!canEnterRegion(localPlayer, region)) {
                blockedRegions.add(region);
            }
        }

        return blockedRegions;
    }

    /**
     * Checks if a player can enter a specific region.
     *
     * @param player the player to check
     * @param region the region to check
     * @return true if the player can enter, false otherwise
     */
    private boolean canEnterRegion(@NotNull LocalPlayer player, @NotNull ProtectedRegion region) {
        // Check the ENTRY flag
        // If ENTRY is set to DENY, the player cannot enter unless they have bypass permissions
        if (region.getFlag(Flags.ENTRY) == com.sk89q.worldguard.protection.flags.StateFlag.State.DENY) {
            // Check if the player is a member or owner of the region
            return region.isMember(player) || region.isOwner(player);
        }

        // If ENTRY is not explicitly denied, the player can enter
        return true;
    }
}
