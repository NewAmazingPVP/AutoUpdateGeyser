# AutoUpdateGeyser

Automated updates for Geyser and Floodgate across Spigot/Paper/Folia, BungeeCord, and Velocity.

[![Build](https://github.com/NewAmazingPVP/AutoUpdateGeyser/actions/workflows/build.yml/badge.svg)](../../actions)

Spigot page: https://www.spigotmc.org/resources/autoupdategeyser.109632/

## Highlights

- Folia-aware scheduling on Spigot/Paper with safe Bukkit fallback.
- Works on Spigot/Paper/Folia, BungeeCord/Waterfall, and Velocity.
- Periodic checks download the latest Geyser/Floodgate builds.
- First‑time install support for missing plugins (when enabled).
- Unified restart options and messaging across all platforms.
- One command everywhere: `/updategeyser` (permission: `autoupdategeyser.admin`).

## Version

- Current: v7.0.0 (Semantic Versioning)

## Java & Platform Support

- Builds on Java 8 and Java 17 in CI.
    - Java 8 build: Spigot/Paper/Folia + BungeeCord artifact (Velocity sources excluded for maximum compatibility).
    - Java 17 build: Full artifact including Velocity support.
- Runtime expectations:
    - Spigot/Paper: Java 8+ (Folia requires modern Java on the server).
    - BungeeCord/Waterfall: Java 8+
    - Velocity: Java 11+ (server requirement), tested on 3.1+

## Configuration

All platforms share the same keys.

Common options:

- `updates.geyser` (bool): Enable Geyser updates. Default: `true`
- `updates.floodgate` (bool): Enable Floodgate updates. Default: `true`
- `updates.interval` (minutes): Check interval. Default: `60`
- `updates.bootTime` (seconds): Startup delay before first check. Default: `5`
- `updates.autoRestart` (bool): Restart after a successful update. Default: `false`
- `updates.restartDelay` (seconds): Delay between warning and restart. Default: `60`
- `updates.restartMessage` (string): Broadcast when scheduling restart.

Files by platform:

- Spigot/Paper/Folia: `plugins/AutoUpdateGeyser/config.yml`
- BungeeCord: `plugins/AutoUpdateGeyser/config.yml`
- Velocity: `plugins/autoupdategeyser/config.toml`

## Command

- `/updategeyser` — Runs an immediate update check for Geyser and Floodgate.
    - Permission: `autoupdategeyser.admin`

## How It Works

- On a schedule, the plugin checks the official GeyserMC download API for new builds of Geyser and Floodgate.
- If an update (or missing install) is detected for an enabled target, it downloads the latest jar into `plugins/`.
- If `autoRestart` is enabled, it broadcasts `restartMessage` and restarts after `restartDelay` seconds.
    - Spigot/Paper/Folia: runs `restart`
    - BungeeCord: runs `end`
    - Velocity: runs `shutdown`

## CI/CD

Every push builds on Java 8 and 17. Artifacts are uploaded for each. Tagging a release `v*` publishes a GitHub Release
with the jar attached.

## Changelog (since 6.x)

- Added Folia support via scheduler compatibility.
- Made Metrics Folia-safe on Spigot.
- Fixed Spigot restart delay units (seconds).
- Prevent false positives when a download fails.
- Normalized defaults across platforms; safer build tracking.
- Java 8 compatible sources; Velocity included on Java 11+ builds.

## License

AutoUpdateGeyser is released under the MIT License. See `LICENSE` for details.
