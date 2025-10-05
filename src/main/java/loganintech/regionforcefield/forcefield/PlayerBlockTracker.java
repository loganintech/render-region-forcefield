package loganintech.regionforcefield.forcefield;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks fake blocks sent to players so they can be properly cleaned up.
 */
public class PlayerBlockTracker {

    private final Map<UUID, Set<Location>> playerBlocks = new HashMap<>();

    /**
     * Records that a block was sent to a player.
     *
     * @param player   the player
     * @param location the block location
     */
    public void addBlock(@NotNull Player player, @NotNull Location location) {
        playerBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(location.clone());
    }

    /**
     * Gets all blocks that have been sent to a player.
     *
     * @param player the player
     * @return set of block locations
     */
    @NotNull
    public Set<Location> getBlocks(@NotNull Player player) {
        return playerBlocks.getOrDefault(player.getUniqueId(), new HashSet<>());
    }

    /**
     * Clears all tracked blocks for a player.
     *
     * @param player the player
     */
    public void clearPlayer(@NotNull Player player) {
        playerBlocks.remove(player.getUniqueId());
    }

    /**
     * Replaces the tracked blocks for a player with a new set.
     *
     * @param player   the player
     * @param newBlocks the new set of blocks
     */
    public void setBlocks(@NotNull Player player, @NotNull Set<Location> newBlocks) {
        if (newBlocks.isEmpty()) {
            playerBlocks.remove(player.getUniqueId());
        } else {
            playerBlocks.put(player.getUniqueId(), new HashSet<>(newBlocks));
        }
    }

    /**
     * Clears all tracked blocks for all players.
     */
    public void clearAll() {
        playerBlocks.clear();
    }
}
