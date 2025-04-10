# ⚔️ rKitPvP

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/github/license/Rhydium/rKitPvP)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21%2B-blueviolet)]()

> ⚠️ This plugin is still under heavy development and should not be used in production environments.

A lightweight, fully customizable KitPvP plugin for Minecraft servers. Includes coins, kits, combat logging prevention, scoreboards, and much more – perfect for PvP-based minigame servers!

---

## ✨ Features

- 📊 **Scoreboard** – Live stats and info
- ⚔️ **Custom Death & Respawn System**
- 💰 **Coin System** – Earn coins by killing players
- 🎒 **Kit Selection GUI** – Easy-to-use menu
- 🚫 **Anti-Combat Logging** – Temporary bans for combat loggers
- 🌍 **WorldGuard Integration** – Define spawn and PvP zones

---

## 🧩 Dependencies

Make sure the following plugins are installed:

- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
- [WorldEdit](https://enginehub.org/worldedit/)
- [WorldGuard](https://enginehub.org/worldguard/)

> ⚠️ This plugin has only been tested with **Minecraft API version 1.21**. There is no compatibility with earlier versions.

---

## 📥 Installation

1. Download the latest `.jar` file from the [Releases](https://github.com/Rhydium/rKitPvP/releases) page.
2. Place it in your server's `/plugins` folder.
3. Start your server.
4. Configure the plugin via the generated `config.yml`.

---

## 🧾 Commands & Permissions

| Command | Description | Permission |
|--------|-------------|------------|
| `/kitpvp build` | Toggle build mode | `kitpvp.use`, `kitpvp.admin.build` |
| `/kitpvp setspawn` | Set the spawn location for KitPvP | `kitpvp.use`, `kitpvp.admin.setspawn` |
| `/spawn` | Teleport to the spawn (5 sec cooldown) | `kitpvp.player.spawn` |

> ⚠️ All subcommands under `/kitpvp` require `kitpvp.use`.

---

## 🔧 Configuration Example

```yaml
# Anti Combatlog settings
combat:
  tag-duration: 15    # Duration in seconds players stay in combat
  ban-duration: 10m   # Temporary ban duration after logging out in combat

# Coins related settings
kill-reward: 10       # Coins rewarded per kill

# Regions (WorldGuard)
spawn-region: "spawn"
pvp-region: "pvp"

# Respawn settings
respawn:
  countdown: 3
  death-title: "&cYOU DIED!"
  death-subtitle: "&eRespawning in %seconds%..."
  respawn-title: "&aReady to fight."
  respawn-subtitle: ""

# Database configuration
database:
  type: 'sqlite'
  mysql:
    host: 'localhost'
    port: '3306'
    database: 'mythica_kitpvp'
    username: 'root'
    password: 'password'

# Custom messages
messages:
  combat-tagged: "&cYou are now in combat!"
  combat-untagged: "&aYou are no longer in combat!"
```

---

## 📣 Contributing

Contributions are welcome! If you have ideas, feature requests, or bug reports, feel free to open an issue or pull request.

---

Made with ❤️ for the Minecraft community by Rhydium.
