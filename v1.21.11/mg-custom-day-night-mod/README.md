# ☀️ Custom Day/Night Mod 🌙

> **Take full control of Minecraft's day/night cycle — no client mod needed.**

A lightweight, **server-side only** Fabric mod for Minecraft **1.21.11** that lets you customize exactly how long day and night last in the Overworld. Whether you want marathon building sessions under the sun or short, intense nights — this mod makes it effortless.

[![Fabric](https://img.shields.io/badge/mod%20loader-Fabric-blue)](https://fabricmc.net/)
[![License: MIT](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Modrinth](https://img.shields.io/badge/Modrinth-download-brightgreen)](https://modrinth.com/mod/minecraft-custom-daynight-mod)

---

## ✨ Features

- **Two ways to customize** — use speed multipliers *or* set exact tick-based durations
- **Hot-reload config** — change settings on the fly without restarting the server
- **In-game commands** — check status, reload config, and toggle logging from the chat
- **Vanilla-client friendly** — players don't need to install anything
- **Zero world data changes** — safe to add or remove at any time

---

## 🚀 Getting Started

1. Drop the mod `.jar` into your server's `mods/` folder (requires [Fabric Loader](https://fabricmc.net/) ≥ 0.18.4 and [Fabric API](https://modrinth.com/mod/fabric-api) ≥ 0.141.2).
2. Start the server — a default config file will be created automatically.
3. Edit `config/customdaynightmod.properties` to your liking (see below).
4. Run `/customdaynight reload` in-game to apply changes instantly.

---

## ⚙️ Configuration

**File:** `config/customdaynightmod.properties`

```properties
# Speed multipliers (1.0 = normal speed, 0.5 = half speed / twice as long)
day_multiplier=1.0
night_multiplier=1.0

# Absolute lengths in ticks (overrides multipliers when > 0)
absolute_day_length=0
absolute_night_length=0

# Log day/night transitions to the server console
log_phase_changes=true
```

> **Quick reference:** 1 Minecraft tick = 50 ms · 20 ticks = 1 second · 12,000 ticks = 10 real-time minutes (the default length of both day and night).

### How it works

| Setting | What it does |
|---|---|
| `day_multiplier` / `night_multiplier` | Scales the *speed* of time progression. A value of `0.5` makes that phase last **twice as long**; `2.0` makes it **twice as fast**. |
| `absolute_day_length` / `absolute_night_length` | Sets the phase duration to an **exact number of ticks**. When set to a value greater than `0`, this takes priority over the multiplier. |
| `log_phase_changes` | When `true`, prints a message to the server console each time the world transitions between day and night. |

---

### 📖 Examples

#### Example 1: Extended day for builders (50 min day / 10 min night)

```properties
absolute_day_length=60000
absolute_night_length=12000
```

☀️ Day lasts **50 minutes** (60,000 ticks) — plenty of time to build.  
🌙 Night lasts **10 minutes** (12,000 ticks) — standard length.

#### Example 2: Slow down daytime only (20 min day / 10 min night)

```properties
day_multiplier=0.5
night_multiplier=1.0
```

| Phase | Default | With this config |
|-------|---------|-----------------|
| ☀️ Day | 10 min | **20 min** (half speed → twice as long) |
| 🌙 Night | 10 min | **10 min** (unchanged) |

> **Tip:** You can mix and match — for example, use `absolute_day_length` for a precise daytime duration while using `night_multiplier` to scale the night.

---

## 🔧 Commands

All commands require **operator permission level 4** (full ops).

| Command | Description |
|---------|-------------|
| `/customdaynight reload` | Reload the config file without restarting the server |
| `/customdaynight status` | Display the current day/night settings in chat |
| `/customdaynight togglePhaseLogging` | Toggle day/night transition messages on or off at runtime |

---

## 🔗 Links

- [Modrinth Page](https://modrinth.com/mod/minecraft-custom-daynight-mod)
- [Source Code](https://github.com/mozartsghost1212/mg-custom-day-night-mod)
- [Report Issues](https://github.com/mozartsghost1212/mg-custom-day-night-mod/issues)
- [Buy Me a Coffee ☕](https://coff.ee/mozartsghost1212)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).