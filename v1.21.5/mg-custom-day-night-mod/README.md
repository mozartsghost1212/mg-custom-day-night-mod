# Custom Day/Night Mod

A server-side only Fabric mod for Minecraft **1.21.5** that allows you to configure the **length of day and night**.

GitHub: [https://github.com/mozartsghost1212](https://github.com/mozartsghost1212)

## Features

- Configure day and night length using:
  - `day_multiplier` and `night_multiplier`
  - OR `absolute_day_length` and `absolute_night_length` (in ticks)
- Live config reload via `/customdaynight reload`
- Check current settings via `/customdaynight status`
- Dynamically toggle phase transition messages via `/customdaynight togglePhaseLogging`
- No client mod required — works with vanilla clients
- No change to world save data

## Configuration

File: `config/customdaynightmod.properties`

```
# If absolute_*_length > 0, it takes priority over multiplier
day_multiplier=1.0
night_multiplier=1.0
absolute_day_length=0
absolute_night_length=0
log_phase_changes=true
```

### Example: 10 min day, 5 min night

```
absolute_day_length=12000
absolute_night_length=6000
```

## Commands

- `/customdaynight reload` → Reloads the config file
- `/customdaynight status` → Displays current settings
- `/customdaynight togglePhaseLogging` → Enable/disable phase transition messages **at runtime**

## Building

```bash
./gradlew build
```

Result: `build/libs/customdaynightmod-1.0.0.jar`

## License

MIT © 2025 mozartsghost1212

## What is Fabric Loom?

**Fabric Loom** is the Gradle plugin used to build Fabric mods.

It takes care of:
- Downloading the correct version of Minecraft and Fabric Loader
- Applying Yarn mappings so your code can refer to Minecraft classes/fields by name
- Remapping your compiled `.jar` to be compatible with Fabric Loader
- Producing `.jar` files ready for use on Fabric servers or clients

Without `fabric-loom`, a mod `.jar` would not be usable in Minecraft because class names and method names would not match the game's internals.

**In short: `fabric-loom` makes your mod compatible with Minecraft + Fabric Loader.**

See: [https://github.com/FabricMC/fabric-loom](https://github.com/FabricMC/fabric-loom)