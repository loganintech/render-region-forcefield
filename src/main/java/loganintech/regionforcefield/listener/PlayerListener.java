package loganintech.regionforcefield.listener;

import loganintech.regionforcefield.RegionForcefieldPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles player events for cleanup.
 */
public class PlayerListener implements Listener {

    private final RegionForcefieldPlugin plugin;

    public PlayerListener(@NotNull RegionForcefieldPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        // Clean up fake blocks when player disconnects
        plugin.getForcefieldRenderer().clearBlocks(event.getPlayer());
    }
}
