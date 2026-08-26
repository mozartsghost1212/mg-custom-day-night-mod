# ☀️ Custom Day/Night Mod 🌙

> **Take full control of Minecraft's day/night cycle — no client mod needed.**

A lightweight, **server-side only** Fabric mod for Minecraft **26.2** that lets you customize exactly how long day and night last in the Overworld. Whether you want marathon building sessions under the sun or short, intense nights — this mod makes it effortless.

[![Fabric](https://img.shields.io/badge/mod%20loader-Fabric-blue)](https://fabricmc.net/)
[![License: MIT](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Modrinth](https://img.shields.io/badge/Modrinth-download-brightgreen)](https://modrinth.com/mod/minecraft-custom-daynight-mod)

---

## ✨ Features

- **Two ways to customize** — use speed multipliers *or* set exact tick-based durations
- **Interactive chat menu** — click buttons in chat to adjust settings live, no client mod required
- **Live duration preview** — the menu shows the resulting day, night, and full cycle lengths in human-readable time
- **Hot-reload config** — change settings on the fly without restarting the server
- **Save & restore defaults** — persist changes to disk or reset everything with one click
- **Safety limits** — values are clamped to safe ranges to prevent server crashes
- **In-game commands** — check status, reload config, set values, and toggle logging from the chat
- **Version-stamped logging** — all console and chat messages include the mod name and version
- **Vanilla-client friendly** — players don't need to install anything
- **Zero world data changes** — safe to add or remove at any time

---

## 🚀 Getting Started

1. Drop the mod `.jar` into your server's `mods/` folder (requires [Fabric Loader](https://fabricmc.net/) ≥ 0.19.3 and [Fabric API](https://modrinth.com/mod/fabric-api) ≥ 0.158.0).
2. Start the server — a default config file will be created automatically.
3. Run `/customdaynight` in-game — the **interactive settings menu** prints in chat. **Press T to open chat**, then click any button and press **Enter** to apply.
4. Alternatively, edit `config/customdaynightmod.properties` directly (see below).
5. Click **[Save]** in the menu (then Enter) to persist changes to disk.

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

### ⚠️ Safety Limits

To prevent server instability, all values are automatically clamped to safe ranges:

| Setting | Min | Max | Notes |
|---------|-----|-----|-------|
| `day_multiplier` / `night_multiplier` | 0.01 | 100.0 | Prevents time freeze or extreme speed |
| `absolute_day_length` / `absolute_night_length` | 20 ticks (1 s) | 2,400,000 ticks (~33 h 20 m) | Set to `0` to disable (use multiplier instead) |

If a value outside the allowed range is entered via command or config file, it is silently clamped and a warning is shown in chat.

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

### Interactive Menu

| Command | Description |
|---------|-------------|
| `/customdaynight` | Open the interactive settings menu (same as `/customdaynight menu`) |
| `/customdaynight menu` | Open the interactive settings menu |

The menu displays all current settings, grouped into **Speed multipliers** and **Fixed lengths (override)**. Each row has clickable **[−]** and **[+]** buttons that pre-fill the target command in your chat bar, plus a **[✎]** button that pre-fills a blank value for typing. Action buttons **[Save]**, **[Reload]**, and **[Defaults]** work the same way. **Click a button, then press Enter** to apply.

> **Prefer minutes over ticks?** Use `/customdaynight duration day <minutes>` or `/customdaynight duration night <minutes>` — accepts decimals (e.g. `12.5`).

> **Why not one-click?** Vanilla clients show a *"Confirm Command Execution"* dialog on every `run_command` click that requires operator permissions. Pre-filling the chat bar avoids that popup and keeps the workflow snappy: click → Enter.

Hover any value to see what it means and its allowed range. Fixed lengths display **OFF** when set to `0`, meaning the multiplier above controls that phase instead.

A **duration summary** at the bottom shows the effective day, night, and full cycle lengths (e.g. *☀ Day: 10m 0s · ☽ Night: 10m 0s · ⏱ Full Cycle: 20m 0s*).

> **Tip:** Press **T** to open chat, click any button, then press **Enter**.

### Configuration Commands

| Command | Description |
|---------|-------------|
| `/customdaynight duration day <minutes>` | Set the day phase length in **real-time minutes** (accepts decimals, e.g. `12.5`). Uses fixed length under the hood. |
| `/customdaynight duration night <minutes>` | Set the night phase length in **real-time minutes**. |
| `/customdaynight set dayMultiplier <value>` | Set the day speed multiplier |
| `/customdaynight set nightMultiplier <value>` | Set the night speed multiplier |
| `/customdaynight set absoluteDayLength <value>` | Set the absolute day length in ticks (0 to disable) |
| `/customdaynight set absoluteNightLength <value>` | Set the absolute night length in ticks (0 to disable) |

### Utility Commands

| Command | Description |
|---------|-------------|
| `/customdaynight reload` | Reload the config file from disk (discards unsaved changes) |
| `/customdaynight save` | Save the current in-memory settings to the config file |
| `/customdaynight defaults` | Reset all settings to default values (does not save to disk) |
| `/customdaynight status` | Display the current settings menu |
| `/customdaynight togglePhaseLogging` | Toggle day/night transition messages on or off |

---

## 🔗 Links

- [Modrinth Page](https://modrinth.com/mod/minecraft-custom-daynight-mod)
- [Source Code](https://github.com/mozartsghost1212/mg-custom-day-night-mod)
- [Report Issues](https://github.com/mozartsghost1212/mg-custom-day-night-mod/issues)
- [Buy Me a Coffee ☕](https://coff.ee/mozartsghost1212)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).