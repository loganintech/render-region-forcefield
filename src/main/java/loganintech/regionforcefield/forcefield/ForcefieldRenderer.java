package loganintech.regionforcefield.forcefield;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import loganintech.regionforcefield.RegionForcefieldPlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders particle forcefields around protected regions.
 */
public class ForcefieldRenderer {

    private final RegionForcefieldPlugin plugin;
    private final double particleSpacing;
    private final Particle.DustOptions dustOptions;
    private final PlayerBlockTracker blockTracker;
    private final BlockData glassBlockData;

    /**
     * Creates a new forcefield renderer.
     *
     * @param plugin the plugin instance
     */
    public ForcefieldRenderer(@NotNull RegionForcefieldPlugin plugin) {
        this.plugin = plugin;
        this.particleSpacing = plugin.getConfig().getDouble("particle-spacing", 0.5);
        this.blockTracker = new PlayerBlockTracker();

        // Get color from config or use default (purple)
        int red = plugin.getConfig().getInt("particle-color.red", 147);
        int green = plugin.getConfig().getInt("particle-color.green", 112);
        int blue = plugin.getConfig().getInt("particle-color.blue", 219);
        float size = (float) plugin.getConfig().getDouble("particle-size", 1.0);

        this.dustOptions = new Particle.DustOptions(Color.fromRGB(red, green, blue), size);

        // Get block material from config or use purple stained glass pane
        String materialName = plugin.getConfig().getString("block-material", "PURPLE_STAINED_GLASS_PANE");
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid block material '" + materialName + "', using PURPLE_STAINED_GLASS_PANE");
            material = Material.PURPLE_STAINED_GLASS_PANE;
        }
        this.glassBlockData = material.createBlockData();
    }

    /**
     * Gets the block tracker for managing fake blocks.
     *
     * @return the block tracker
     */
    @NotNull
    public PlayerBlockTracker getBlockTracker() {
        return blockTracker;
    }

    /**
     * Renders a forcefield around a region for a specific player.
     *
     * @param player the player to show the forcefield to
     * @param region the region to render
     * @param world  the world the region is in
     * @return set of block locations that were rendered
     */
    @NotNull
    public Set<Location> renderForcefield(@NotNull Player player, @NotNull ProtectedRegion region, @NotNull World world) {
        Set<Location> newBlocks = new HashSet<>();

        try {
            plugin.debug("Rendering forcefield for region " + region.getId() + " to player " + player.getName());

            if (region instanceof ProtectedCuboidRegion) {
                renderCuboidForcefield(player, (ProtectedCuboidRegion) region, world, newBlocks);
            } else if (region instanceof ProtectedPolygonalRegion) {
                renderPolygonalForcefield(player, (ProtectedPolygonalRegion) region, world, newBlocks);
            } else {
                // For other region types, fall back to rendering a bounding box
                plugin.debug("Using bounding box for region type: " + region.getClass().getSimpleName());
                renderBoundingBoxForcefield(player, region, world, newBlocks);
            }

            plugin.debug("Rendered " + newBlocks.size() + " blocks for region " + region.getId());
        } catch (Exception e) {
            plugin.getLogger().warning("Error rendering forcefield for region " + region.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return newBlocks;
    }

    /**
     * Renders a forcefield for a cuboid region.
     */
    private void renderCuboidForcefield(@NotNull Player player, @NotNull ProtectedCuboidRegion region,
                                       @NotNull World world, @NotNull Set<Location> blocks) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        // Render vertical edges
        renderVerticalEdges(player, world, min, max, blocks);

        // Render horizontal edges at top and bottom
        renderHorizontalEdges(player, world, min, max, blocks);

        // Optionally render faces (walls)
        if (plugin.getConfig().getBoolean("render-walls", true)) {
            renderWalls(player, world, min, max, blocks);
        }
    }

    /**
     * Renders a forcefield for a polygonal region.
     */
    private void renderPolygonalForcefield(@NotNull Player player, @NotNull ProtectedPolygonalRegion region,
                                          @NotNull World world, @NotNull Set<Location> blocks) {
        List<BlockVector2> points = region.getPoints();
        int minY = region.getMinimumPoint().y();
        int maxY = region.getMaximumPoint().y();

        // Render vertical walls between each pair of points
        for (int i = 0; i < points.size(); i++) {
            BlockVector2 point1 = points.get(i);
            BlockVector2 point2 = points.get((i + 1) % points.size());

            renderVerticalWall(player, world, point1.x(), point1.z(), point2.x(), point2.z(), minY, maxY, blocks);
        }
    }

    /**
     * Renders a bounding box forcefield for unsupported region types.
     */
    private void renderBoundingBoxForcefield(@NotNull Player player, @NotNull ProtectedRegion region,
                                            @NotNull World world, @NotNull Set<Location> blocks) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        renderVerticalEdges(player, world, min, max, blocks);
        renderHorizontalEdges(player, world, min, max, blocks);
    }

    /**
     * Renders the vertical edges of a cuboid.
     */
    private void renderVerticalEdges(@NotNull Player player, @NotNull World world,
                                     @NotNull BlockVector3 min, @NotNull BlockVector3 max,
                                     @NotNull Set<Location> blocks) {
        // Four vertical edges
        renderLine(player, world, min.x(), min.y(), min.z(), min.x(), max.y(), min.z(), blocks);
        renderLine(player, world, max.x(), min.y(), min.z(), max.x(), max.y(), min.z(), blocks);
        renderLine(player, world, min.x(), min.y(), max.z(), min.x(), max.y(), max.z(), blocks);
        renderLine(player, world, max.x(), min.y(), max.z(), max.x(), max.y(), max.z(), blocks);
    }

    /**
     * Renders the horizontal edges of a cuboid.
     */
    private void renderHorizontalEdges(@NotNull Player player, @NotNull World world,
                                       @NotNull BlockVector3 min, @NotNull BlockVector3 max,
                                       @NotNull Set<Location> blocks) {
        // Bottom edges
        renderLine(player, world, min.x(), min.y(), min.z(), max.x(), min.y(), min.z(), blocks);
        renderLine(player, world, min.x(), min.y(), max.z(), max.x(), min.y(), max.z(), blocks);
        renderLine(player, world, min.x(), min.y(), min.z(), min.x(), min.y(), max.z(), blocks);
        renderLine(player, world, max.x(), min.y(), min.z(), max.x(), min.y(), max.z(), blocks);

        // Top edges
        renderLine(player, world, min.x(), max.y(), min.z(), max.x(), max.y(), min.z(), blocks);
        renderLine(player, world, min.x(), max.y(), max.z(), max.x(), max.y(), max.z(), blocks);
        renderLine(player, world, min.x(), max.y(), min.z(), min.x(), max.y(), max.z(), blocks);
        renderLine(player, world, max.x(), max.y(), min.z(), max.x(), max.y(), max.z(), blocks);
    }

    /**
     * Renders the walls (faces) of a cuboid.
     */
    private void renderWalls(@NotNull Player player, @NotNull World world,
                            @NotNull BlockVector3 min, @NotNull BlockVector3 max,
                            @NotNull Set<Location> blocks) {
        // North wall (min Z)
        renderVerticalWall(player, world, min.x(), min.z(), max.x(), min.z(), min.y(), max.y(), blocks);

        // South wall (max Z)
        renderVerticalWall(player, world, min.x(), max.z(), max.x(), max.z(), min.y(), max.y(), blocks);

        // West wall (min X)
        renderVerticalWall(player, world, min.x(), min.z(), min.x(), max.z(), min.y(), max.y(), blocks);

        // East wall (max X)
        renderVerticalWall(player, world, max.x(), min.z(), max.x(), max.z(), min.y(), max.y(), blocks);
    }

    /**
     * Renders a vertical wall between two points.
     */
    private void renderVerticalWall(@NotNull Player player, @NotNull World world,
                                    double x1, double z1, double x2, double z2,
                                    double minY, double maxY, @NotNull Set<Location> blocks) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(z2 - z1, 2));
        int horizontalSteps = (int) Math.ceil(distance / particleSpacing);
        int verticalSteps = (int) Math.ceil((maxY - minY) / particleSpacing);
        double blockSpacing = plugin.getConfig().getDouble("block-spacing", 1.0);

        for (int i = 0; i <= horizontalSteps; i++) {
            double t = horizontalSteps > 0 ? (double) i / horizontalSteps : 0;
            double x = x1 + (x2 - x1) * t;
            double z = z1 + (z2 - z1) * t;

            for (int j = 0; j <= verticalSteps; j++) {
                double y = minY + (maxY - minY) * ((double) j / verticalSteps);
                spawnParticle(player, world, x, y, z);

                // Place blocks at intervals
                if (i % ((int) Math.max(1, blockSpacing / particleSpacing)) == 0 &&
                    j % ((int) Math.max(1, blockSpacing / particleSpacing)) == 0) {
                    placeBlock(player, world, x, y, z, blocks);
                }
            }
        }
    }

    /**
     * Renders a line of particles between two points.
     */
    private void renderLine(@NotNull Player player, @NotNull World world,
                           double x1, double y1, double z1,
                           double x2, double y2, double z2,
                           @NotNull Set<Location> blocks) {
        double distance = Math.sqrt(
            Math.pow(x2 - x1, 2) +
            Math.pow(y2 - y1, 2) +
            Math.pow(z2 - z1, 2)
        );

        int steps = (int) Math.ceil(distance / particleSpacing);
        double blockSpacing = plugin.getConfig().getDouble("block-spacing", 1.0);

        for (int i = 0; i <= steps; i++) {
            double t = steps > 0 ? (double) i / steps : 0;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;
            double z = z1 + (z2 - z1) * t;

            spawnParticle(player, world, x, y, z);

            // Place blocks at intervals
            if (i % ((int) Math.max(1, blockSpacing / particleSpacing)) == 0) {
                placeBlock(player, world, x, y, z, blocks);
            }
        }
    }

    /**
     * Spawns a single particle at the specified location for a player.
     */
    private void spawnParticle(@NotNull Player player, @NotNull World world, double x, double y, double z) {
        Location location = new Location(world, x, y, z);
        player.spawnParticle(Particle.DUST, location, 1, 0, 0, 0, 0, dustOptions);
    }

    /**
     * Places a fake block at the specified location if it's air.
     *
     * @param player the player to send the block to
     * @param world  the world
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param z      the z coordinate
     * @param blocks the set to add this block location to
     */
    private void placeBlock(@NotNull Player player, @NotNull World world,
                           double x, double y, double z, @NotNull Set<Location> blocks) {
        if (!plugin.getConfig().getBoolean("render-blocks", true)) {
            plugin.debug("Skipping block render (render-blocks is false)");
            return;
        }

        Location location = new Location(world, Math.floor(x), Math.floor(y), Math.floor(z));

        // Only place blocks where there's currently air
        if (location.getBlock().getType() == Material.AIR) {
            player.sendBlockChange(location, glassBlockData);
            blocks.add(location);
            plugin.debug("Placed block at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() +
                        " for player " + player.getName() + " (material: " + glassBlockData.getMaterial() + ")");
        } else {
            plugin.debug("Skipped block at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() +
                        " - not air (is " + location.getBlock().getType() + ")");
        }
    }

    /**
     * Clears all fake blocks for a player by restoring the real blocks.
     *
     * @param player the player
     */
    public void clearBlocks(@NotNull Player player) {
        Set<Location> blocks = blockTracker.getBlocks(player);
        for (Location location : blocks) {
            // Send the real block data back to the player
            player.sendBlockChange(location, location.getBlock().getBlockData());
        }
        blockTracker.clearPlayer(player);
    }

    /**
     * Updates blocks for a player based on new blocks that should be visible.
     *
     * @param player    the player
     * @param newBlocks the new set of blocks to show
     */
    public void updateBlocks(@NotNull Player player, @NotNull Set<Location> newBlocks) {
        Set<Location> oldBlocks = blockTracker.getBlocks(player);

        // Remove blocks that are no longer needed
        for (Location location : oldBlocks) {
            if (!newBlocks.contains(location)) {
                player.sendBlockChange(location, location.getBlock().getBlockData());
            }
        }

        // Update the tracker
        blockTracker.setBlocks(player, newBlocks);
    }
}
