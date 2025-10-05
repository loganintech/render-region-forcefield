# RegionForcefield

A Minecraft plugin that renders visible particle forcefields around WorldGuard regions that players cannot enter.

## Features

- Automatically detects WorldGuard regions with `entry deny` flag
- Renders **visible glass pane barriers** and particle effects for blocked regions
- Only shows forcefields to players who **actually cannot enter** (respects bypass permissions and ops)
- Glass panes only placed where there's currently air (doesn't cover existing blocks)
- Configurable particle color, size, spacing, and render distance
- Configurable block material (glass panes, barriers, etc.)
- Supports cuboid and polygonal region types
- Performance-optimized with distance-based rendering
- Automatic cleanup when players move away or disconnect
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
3. Players who cannot enter the region will see a purple forcefield around it

### Commands

- `/forcefield debug` or `/ff debug` - Toggle debug mode on/off
- `/forcefield reload` or `/ff reload` - Reload the configuration file
- `/forcefield status` or `/ff status` - View plugin status and settings
- `/forcefield info` or `/ff info` - View information about blocked regions nearby
- `/forcefield help` or `/ff help` - Show command help

### Permissions

Players who are members or owners of a region can enter it even if entry is denied for others. These players will not see the forcefield.

- `regionforcefield.*` - Grants all permissions (default: op)
- `regionforcefield.command` - Allows use of `/forcefield` command (default: true)
- `regionforcefield.debug` - Allows toggling debug mode (default: op)
- `regionforcefield.reload` - Allows reloading config (default: op)
- `regionforcefield.status` - Allows viewing status (default: op)
- `regionforcefield.info` - Allows viewing region info (default: true)

## Configuration

Edit `plugins/RegionForcefield/config.yml`:

```yaml
# Enable debug logging (useful for troubleshooting)
debug: false

# Update frequency (20 ticks = 1 second)
update-interval-ticks: 20

# Maximum render distance in blocks
max-render-distance: 100

# Render walls or just edges
render-walls: true

# Particle rendering
render-particles: true
particle-spacing: 0.5
particle-color:
  red: 147
  green: 112
  blue: 219
particle-size: 1.0

# Block rendering
render-blocks: true
block-spacing: 1.0
block-material: PURPLE_STAINED_GLASS_PANE
```

## Performance Tips

- Reduce `max-render-distance` for servers with many regions
- Set `render-particles: false` to disable particles and only show blocks
- Set `render-blocks: false` to disable glass panes and only use particles
- Increase `particle-spacing` to reduce particle count
- Increase `block-spacing` to reduce block count
- Set `render-walls: false` to only show edges (not faces)
- Increase `update-interval-ticks` if you don't need real-time updates
- Use `BARRIER` blocks instead of glass panes (less visible but lighter)

## Troubleshooting

### Forcefields not appearing

1. Enable debug mode: `/forcefield debug` or set `debug: true` in config.yml
2. Check the server console for debug messages
3. Use `/forcefield info` to see if regions are being detected
4. Verify the region has `entry deny` set: `/rg info <region>`
5. **Check you can't actually enter** - ops and members won't see forcefields
6. Ensure you're within render distance of the region (default: 100 blocks)
7. Try setting `render-blocks: true` if you only see particles
8. Check that locations aren't already occupied by blocks

### Plugin won't load

1. Verify WorldGuard 7.0.14 or higher is installed
2. Check server logs for error messages
3. Ensure you're running Paper 1.21.8 or higher
4. Verify Java 21 is being used

### Performance issues

1. Increase `particle-spacing` (less particles)
2. Reduce `max-render-distance`
3. Set `render-walls: false` (edges only)
4. Increase `update-interval-ticks` (update less frequently)

## Building from Source

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`

## License

This project is provided as-is for educational purposes.

## Support

For issues or feature requests, please open an issue on the project repository.
