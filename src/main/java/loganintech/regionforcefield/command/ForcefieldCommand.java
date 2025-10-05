package loganintech.regionforcefield.command;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import loganintech.regionforcefield.RegionForcefieldPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Handles the /forcefield command and its subcommands.
 */
public class ForcefieldCommand implements CommandExecutor, TabCompleter {

    private final RegionForcefieldPlugin plugin;

    public ForcefieldCommand(@NotNull RegionForcefieldPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "debug":
                return handleDebug(sender);
            case "reload":
                return handleReload(sender);
            case "status":
                return handleStatus(sender);
            case "info":
                return handleInfo(sender);
            case "test":
                return handleTest(sender);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /forcefield help");
                return true;
        }
    }

    private boolean handleDebug(@NotNull CommandSender sender) {
        if (!sender.hasPermission("regionforcefield.debug")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        boolean currentDebug = plugin.getConfig().getBoolean("debug", false);
        boolean newDebug = !currentDebug;

        plugin.getConfig().set("debug", newDebug);
        plugin.saveConfig();

        sender.sendMessage(ChatColor.GREEN + "Debug mode " +
            (newDebug ? ChatColor.YELLOW + "enabled" : ChatColor.GRAY + "disabled") +
            ChatColor.GREEN + ".");

        plugin.getLogger().info(sender.getName() + " toggled debug mode: " + newDebug);
        return true;
    }

    private boolean handleReload(@NotNull CommandSender sender) {
        if (!sender.hasPermission("regionforcefield.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        try {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
            plugin.getLogger().info(sender.getName() + " reloaded the configuration.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error reloading configuration: " + e.getMessage());
            plugin.getLogger().warning("Error reloading configuration: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    private boolean handleStatus(@NotNull CommandSender sender) {
        if (!sender.hasPermission("regionforcefield.status")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== RegionForcefield Status ===");
        sender.sendMessage(ChatColor.YELLOW + "Version: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Debug Mode: " + ChatColor.WHITE +
            (plugin.getConfig().getBoolean("debug", false) ? "Enabled" : "Disabled"));
        sender.sendMessage(ChatColor.YELLOW + "Update Interval: " + ChatColor.WHITE +
            plugin.getConfig().getLong("update-interval-ticks", 20L) + " ticks");
        sender.sendMessage(ChatColor.YELLOW + "Max Render Distance: " + ChatColor.WHITE +
            plugin.getConfig().getInt("max-render-distance", 100) + " blocks");
        sender.sendMessage(ChatColor.YELLOW + "Particle Spacing: " + ChatColor.WHITE +
            plugin.getConfig().getDouble("particle-spacing", 0.5) + " blocks");
        sender.sendMessage(ChatColor.YELLOW + "Render Walls: " + ChatColor.WHITE +
            (plugin.getConfig().getBoolean("render-walls", true) ? "Yes" : "No"));

        int red = plugin.getConfig().getInt("particle-color.red", 147);
        int green = plugin.getConfig().getInt("particle-color.green", 112);
        int blue = plugin.getConfig().getInt("particle-color.blue", 219);
        sender.sendMessage(ChatColor.YELLOW + "Particle Color: " + ChatColor.WHITE +
            "RGB(" + red + ", " + green + ", " + blue + ")");

        sender.sendMessage(ChatColor.YELLOW + "Online Players: " + ChatColor.WHITE +
            plugin.getServer().getOnlinePlayers().size());

        return true;
    }

    private boolean handleInfo(@NotNull CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (!sender.hasPermission("regionforcefield.info")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        Player player = (Player) sender;

        try {
            // Get regions the player is blocked from
            Set<ProtectedRegion> blockedRegions = plugin.getPermissionChecker()
                .getBlockedRegions(player, player.getWorld());

            sender.sendMessage(ChatColor.GOLD + "=== Region Information ===");
            sender.sendMessage(ChatColor.YELLOW + "World: " + ChatColor.WHITE + player.getWorld().getName());

            // Get all regions in the world
            RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(player.getWorld()));

            if (regionManager != null) {
                int totalRegions = regionManager.getRegions().size();
                sender.sendMessage(ChatColor.YELLOW + "Total Regions: " + ChatColor.WHITE + totalRegions);
                sender.sendMessage(ChatColor.YELLOW + "Blocked Regions: " + ChatColor.WHITE + blockedRegions.size());

                if (!blockedRegions.isEmpty()) {
                    sender.sendMessage(ChatColor.GOLD + "Regions you cannot enter:");
                    for (ProtectedRegion region : blockedRegions) {
                        double distance = calculateDistance(player, region);
                        sender.sendMessage(ChatColor.GRAY + "  - " + ChatColor.WHITE + region.getId() +
                            ChatColor.GRAY + " (distance: " + String.format("%.1f", distance) + " blocks)");
                    }
                } else {
                    sender.sendMessage(ChatColor.GREEN + "You can enter all regions in this world!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "No region manager found for this world.");
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Error retrieving region information: " + e.getMessage());
            plugin.getLogger().warning("Error in info command: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private boolean handleTest(@NotNull CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (!sender.hasPermission("regionforcefield.debug")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Place test blocks in a small box around the player
        sender.sendMessage(ChatColor.YELLOW + "Placing test blocks around you...");

        Location loc = player.getLocation();
        BlockData glassData = Material.PURPLE_STAINED_GLASS_PANE.createBlockData();

        int count = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0) continue; // Skip player location

                Location testLoc = loc.clone().add(x, 0, z);
                if (testLoc.getBlock().getType() == Material.AIR) {
                    player.sendBlockChange(testLoc, glassData);
                    count++;
                }
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Placed " + count + " test blocks around you.");
        sender.sendMessage(ChatColor.GRAY + "These are client-side only and will disappear on relog.");

        return true;
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== RegionForcefield Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/forcefield debug " + ChatColor.GRAY + "- Toggle debug mode");
        sender.sendMessage(ChatColor.YELLOW + "/forcefield reload " + ChatColor.GRAY + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/forcefield status " + ChatColor.GRAY + "- Show plugin status");
        sender.sendMessage(ChatColor.YELLOW + "/forcefield info " + ChatColor.GRAY + "- Show region information");
        sender.sendMessage(ChatColor.YELLOW + "/forcefield test " + ChatColor.GRAY + "- Test block rendering");
        sender.sendMessage(ChatColor.YELLOW + "/forcefield help " + ChatColor.GRAY + "- Show this help message");
    }

    private double calculateDistance(@NotNull Player player, @NotNull ProtectedRegion region) {
        double playerX = player.getLocation().getX();
        double playerY = player.getLocation().getY();
        double playerZ = player.getLocation().getZ();

        double regionCenterX = (region.getMinimumPoint().x() + region.getMaximumPoint().x()) / 2.0;
        double regionCenterY = (region.getMinimumPoint().y() + region.getMaximumPoint().y()) / 2.0;
        double regionCenterZ = (region.getMinimumPoint().z() + region.getMaximumPoint().z()) / 2.0;

        return Math.sqrt(
            Math.pow(playerX - regionCenterX, 2) +
            Math.pow(playerY - regionCenterY, 2) +
            Math.pow(playerZ - regionCenterZ, 2)
        );
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                     @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("debug", "reload", "status", "info", "test", "help");
            String input = args[0].toLowerCase();

            for (String subcommand : subcommands) {
                if (subcommand.startsWith(input)) {
                    completions.add(subcommand);
                }
            }
        }

        return completions;
    }
}
