<div align="center">

# AutoUpdateGeyser v7.0.0

Keep Geyser and Floodgate up-to-date — automatically and safely.

[![SpigotMC](https://img.shields.io/badge/SpigotMC-Resource-orange)](https://www.spigotmc.org/resources/autoupdategeyser.109632/)
![Platforms](https://img.shields.io/badge/Platforms-Spigot%20%7C%20Paper%20%7C%20Folia%20%7C%20Velocity%20%7C%20BungeeCord-5A67D8)
![MC](https://img.shields.io/badge/Minecraft-1.8%E2%86%92Latest-2EA043)
![Java](https://img.shields.io/badge/Java-8%2B-1F6FEB)
![License](https://img.shields.io/badge/License-MIT-0E8A16)

</div>

> TL;DR
> Drop the jar in plugins/, choose whether to manage Geyser and/or Floodgate, and the plugin will fetch new builds on a schedule. Optional restart after download.

---

## Table of Contents

* [Highlights](#highlights)
* [Supported Platforms](#supported-platforms)
* [Requirements](#requirements)
* [Installation](#installation)
* [Quick Start](#quick-start)
* [Configuration](#configuration)
* [Commands & Permissions](#commands--permissions)
* [How It Works](#how-it-works)
* [Building from Source](#building-from-source)
* [License](#license)

---

## Highlights

* Uses the official GeyserMC download API for both Geyser and Floodgate.
* First-time install: can download missing jars if enabled in config.
* Non-blocking update checks; Folia-safe scheduling on Spigot/Paper.
* Optional broadcast + delayed restart after successful downloads.
* Works across Spigot, Paper, Folia, Velocity, and BungeeCord.

## Supported Platforms

* Spigot

* Paper

* Folia

* Velocity

* BungeeCord (and Waterfall)

## Requirements

* Java 8 or newer.

* Internet access to reach the GeyserMC download endpoints.

Notes:

* Folia requires a modern Paper/Folia server build. This plugin uses Folia-safe schedulers where required.

* Velocity generally runs on Java 11+; this plugin’s Velocity module targets Velocity 3.1+.

## Installation

1. Download the latest jar from the [Spigot resource page](https://www.spigotmc.org/resources/autoupdategeyser.109632/).

2. Place it into your server/proxy `plugins/` folder.

3. Start the server to generate the default configuration.

4. Edit the config (see below) and restart if desired.

## Quick Start

1. Decide which components to manage:

   * `updates.geyser: true|false`

   * `updates.floodgate: true|false`

2. Keep the default interval (60 minutes) or set your own.

3. Optional: enable `updates.autoRestart` and set a `restartDelay`.

4. Trigger a manual check: `/updategeyser` (permission: `autoupdategeyser.admin`).

## Configuration

Configuration files are per-platform but share the same keys.

Spigot/Paper/Folia and BungeeCord (`config.yml`):

```yaml
updates:
  geyser: true
  floodgate: true
  interval: 60          # minutes
  bootTime: 5           # seconds after startup before first check
  autoRestart: false
  restartDelay: 60      # seconds
  restartMessage: "Server is restarting shortly!"
```

Velocity (`config.toml`):

```toml
[updates]
geyser = true
floodgate = true
interval = 60          # minutes
bootTime = 5           # seconds after startup before first check
autoRestart = false
restartDelay = 60      # seconds
restartMessage = "Server is restarting shortly!"
```

## Commands & Permissions

* `/updategeyser` — Runs an immediate update check for Geyser and Floodgate.

  * Permission: `autoupdategeyser.admin`

## How It Works

* On a schedule, the plugin queries the GeyserMC API for the latest build numbers.

* If a new build is available (or the plugin is missing), it downloads the correct platform jar to `plugins/`.

* The last applied build is tracked in `builds.yml` inside the plugin data folder.

* If `autoRestart` is enabled, the plugin broadcasts `restartMessage` and restarts after `restartDelay` seconds.

  * Spigot/Paper/Folia: runs `restart`

  * BungeeCord: runs `end`

  * Velocity: runs `shutdown`

## Building from Source

Build with Maven:

```bash
mvn -DskipTests package
```

## License

AutoUpdateGeyser is licensed under the MIT License. See `LICENSE` for details.
