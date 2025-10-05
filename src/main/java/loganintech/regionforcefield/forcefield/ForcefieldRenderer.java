package loganintech.regionforcefield.forcefield;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Renders particle forcefields around protected regions.
 */
public class ForcefieldRenderer {

    private final Plugin plugin;
    private final double particleSpacing;
    private final Particle.DustOptions dustOptions;

    /**
     * Creates a new forcefield renderer.
     *
     * @param plugin the plugin instance
     */
    public ForcefieldRenderer(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.particleSpacing = plugin.getConfig().getDouble("particle-spacing", 0.5);

        // Get color from config or use default (cyan)
        int red = plugin.getConfig().getInt("particle-color.red", 0);
        int green = plugin.getConfig().getInt("particle-color.green", 255);
        int blue = plugin.getConfig().getInt("particle-color.blue", 255);
        float size = (float) plugin.getConfig().getDouble("particle-size", 1.0);

        this.dustOptions = new Particle.DustOptions(Color.fromRGB(red, green, blue), size);
    }

    /**
     * Renders a forcefield around a region for a specific player.
     *
     * @param player the player to show the forcefield to
     * @param region the region to render
     * @param world  the world the region is in
     */
    public void renderForcefield(@NotNull Player player, @NotNull ProtectedRegion region, @NotNull World world) {
        if (region instanceof ProtectedCuboidRegion) {
            renderCuboidForcefield(player, (ProtectedCuboidRegion) region, world);
        } else if (region instanceof ProtectedPolygonalRegion) {
            renderPolygonalForcefield(player, (ProtectedPolygonalRegion) region, world);
        } else {
            // For other region types, fall back to rendering a bounding box
            renderBoundingBoxForcefield(player, region, world);
        }
    }

    /**
     * Renders a forcefield for a cuboid region.
     */
    private void renderCuboidForcefield(@NotNull Player player, @NotNull ProtectedCuboidRegion region, @NotNull World world) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        // Render vertical edges
        renderVerticalEdges(player, world, min, max);

        // Render horizontal edges at top and bottom
        renderHorizontalEdges(player, world, min, max);

        // Optionally render faces (walls)
        if (plugin.getConfig().getBoolean("render-walls", true)) {
            renderWalls(player, world, min, max);
        }
    }

    /**
     * Renders a forcefield for a polygonal region.
     */
    private void renderPolygonalForcefield(@NotNull Player player, @NotNull ProtectedPolygonalRegion region, @NotNull World world) {
        List<BlockVector2> points = region.getPoints();
        int minY = region.getMinimumPoint().y();
        int maxY = region.getMaximumPoint().y();

        // Render vertical walls between each pair of points
        for (int i = 0; i < points.size(); i++) {
            BlockVector2 point1 = points.get(i);
            BlockVector2 point2 = points.get((i + 1) % points.size());

            renderVerticalWall(player, world, point1.x(), point1.z(), point2.x(), point2.z(), minY, maxY);
        }
    }

    /**
     * Renders a bounding box forcefield for unsupported region types.
     */
    private void renderBoundingBoxForcefield(@NotNull Player player, @NotNull ProtectedRegion region, @NotNull World world) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        renderVerticalEdges(player, world, min, max);
        renderHorizontalEdges(player, world, min, max);
    }

    /**
     * Renders the vertical edges of a cuboid.
     */
    private void renderVerticalEdges(@NotNull Player player, @NotNull World world, @NotNull BlockVector3 min, @NotNull BlockVector3 max) {
        // Four vertical edges
        renderLine(player, world, min.x(), min.y(), min.z(), min.x(), max.y(), min.z());
        renderLine(player, world, max.x(), min.y(), min.z(), max.x(), max.y(), min.z());
        renderLine(player, world, min.x(), min.y(), max.z(), min.x(), max.y(), max.z());
        renderLine(player, world, max.x(), min.y(), max.z(), max.x(), max.y(), max.z());
    }

    /**
     * Renders the horizontal edges of a cuboid.
     */
    private void renderHorizontalEdges(@NotNull Player player, @NotNull World world, @NotNull BlockVector3 min, @NotNull BlockVector3 max) {
        // Bottom edges
        renderLine(player, world, min.x(), min.y(), min.z(), max.x(), min.y(), min.z());
        renderLine(player, world, min.x(), min.y(), max.z(), max.x(), min.y(), max.z());
        renderLine(player, world, min.x(), min.y(), min.z(), min.x(), min.y(), max.z());
        renderLine(player, world, max.x(), min.y(), min.z(), max.x(), min.y(), max.z());

        // Top edges
        renderLine(player, world, min.x(), max.y(), min.z(), max.x(), max.y(), min.z());
        renderLine(player, world, min.x(), max.y(), max.z(), max.x(), max.y(), max.z());
        renderLine(player, world, min.x(), max.y(), min.z(), min.x(), max.y(), max.z());
        renderLine(player, world, max.x(), max.y(), min.z(), max.x(), max.y(), max.z());
    }

    /**
     * Renders the walls (faces) of a cuboid.
     */
    private void renderWalls(@NotNull Player player, @NotNull World world, @NotNull BlockVector3 min, @NotNull BlockVector3 max) {
        // North wall (min Z)
        renderVerticalWall(player, world, min.x(), min.z(), max.x(), min.z(), min.y(), max.y());

        // South wall (max Z)
        renderVerticalWall(player, world, min.x(), max.z(), max.x(), max.z(), min.y(), max.y());

        // West wall (min X)
        renderVerticalWall(player, world, min.x(), min.z(), min.x(), max.z(), min.y(), max.y());

        // East wall (max X)
        renderVerticalWall(player, world, max.x(), min.z(), max.x(), max.z(), min.y(), max.y());
    }

    /**
     * Renders a vertical wall between two points.
     */
    private void renderVerticalWall(@NotNull Player player, @NotNull World world,
                                    double x1, double z1, double x2, double z2,
                                    double minY, double maxY) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(z2 - z1, 2));
        int horizontalSteps = (int) Math.ceil(distance / particleSpacing);
        int verticalSteps = (int) Math.ceil((maxY - minY) / particleSpacing);

        for (int i = 0; i <= horizontalSteps; i++) {
            double t = horizontalSteps > 0 ? (double) i / horizontalSteps : 0;
            double x = x1 + (x2 - x1) * t;
            double z = z1 + (z2 - z1) * t;

            for (int j = 0; j <= verticalSteps; j++) {
                double y = minY + (maxY - minY) * ((double) j / verticalSteps);
                spawnParticle(player, world, x, y, z);
            }
        }
    }

    /**
     * Renders a line of particles between two points.
     */
    private void renderLine(@NotNull Player player, @NotNull World world,
                           double x1, double y1, double z1,
                           double x2, double y2, double z2) {
        double distance = Math.sqrt(
            Math.pow(x2 - x1, 2) +
            Math.pow(y2 - y1, 2) +
            Math.pow(z2 - z1, 2)
        );

        int steps = (int) Math.ceil(distance / particleSpacing);

        for (int i = 0; i <= steps; i++) {
            double t = steps > 0 ? (double) i / steps : 0;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;
            double z = z1 + (z2 - z1) * t;

            spawnParticle(player, world, x, y, z);
        }
    }

    /**
     * Spawns a single particle at the specified location for a player.
     */
    private void spawnParticle(@NotNull Player player, @NotNull World world, double x, double y, double z) {
        Location location = new Location(world, x, y, z);
        player.spawnParticle(Particle.DUST, location, 1, 0, 0, 0, 0, dustOptions);
    }
}
