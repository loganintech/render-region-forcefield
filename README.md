# RegionForcefield

A Minecraft plugin that renders visible particle forcefields around WorldGuard regions that players cannot enter.

## Features

- Automatically detects WorldGuard regions with `entry deny` flag
- Renders particle forcefields only visible to players who cannot enter
- Configurable particle color, size, spacing, and render distance
- Supports cuboid and polygonal region types
- Performance-optimized with distance-based rendering
- Clean, readable, and well-documented code

## Requirements

- Paper 1.21.8 or higher
- WorldGuard 7.0.14 or higher
- Java 21

## Installation

1. Build the plugin:
   ```bash
   ./gradlew build
   ```

2. Copy `build/libs/RegionForcefield-1.0.0.jar` to your server's `plugins/` folder

3. Restart your server

4. Configure the plugin by editing `plugins/RegionForcefield/config.yml`

## Usage

Once installed, the plugin automatically works with your existing WorldGuard regions:

1. Create a WorldGuard region: `/rg define myregion`
2. Set the entry flag to deny: `/rg flag myregion entry deny`
3. Players who cannot enter the region will see a cyan forcefield around it

### Permissions

Players who are members or owners of a region can enter it even if entry is denied for others. These players will not see the forcefield.

## Configuration

Edit `plugins/RegionForcefield/config.yml`:

```yaml
# Update frequency (20 ticks = 1 second)
update-interval-ticks: 20

# Maximum render distance in blocks
max-render-distance: 100

# Spacing between particles
particle-spacing: 0.5

# Render walls or just edges
render-walls: true

# Particle color (RGB 0-255)
particle-color:
  red: 0
  green: 255
  blue: 255

# Particle size (0.5-2.0 recommended)
particle-size: 1.0
```

## Performance Tips

- Reduce `max-render-distance` for servers with many regions
- Increase `particle-spacing` to reduce particle count
- Set `render-walls: false` to only show edges
- Increase `update-interval-ticks` if you don't need real-time updates

## Building from Source

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`

## License

This project is provided as-is for educational purposes.

## Support

For issues or feature requests, please open an issue on the project repository.
