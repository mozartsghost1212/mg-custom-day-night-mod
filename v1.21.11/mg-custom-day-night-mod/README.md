# Custom Day/Night Mod

A server-side only Fabric mod for Minecraft **1.21.11** that allows you to configure the **length of day and night**.

GitHub: [https://github.com/mozartsghost1212](https://github.com/mozartsghost1212)

## Features

- Configure day and night length using either:
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
day_multiplier=1.0
night_multiplier=1.0
absolute_day_length=0
absolute_night_length=0
log_phase_changes=true
```
The configuration file for this mod lets you control how long day and night last in Minecraft’s Overworld, and whether you want to see messages in the console when the game switches between day and night.

**Note: A Minecraft tick is 50 milliseconds / 20 ticks per second.**

### Example: 50 minute day, 10 minute night

```
day_multiplier=1.0
night_multiplier=1.0
absolute_day_length=60000
absolute_night_length=12000
```

This configuration will make the day last 50 minutes (60,000 ticks) and the night last 10 minutes (12,000 ticks).

### Example: Day progresses 50% slower than night

```
day_multiplier=0.5
night_multiplier=1.0
absolute_day_length=0
absolute_night_length=0
```

With this configuration, the day will last twice as long as normal (progressing at half speed), while the night will remain at the default speed.

**What is the actual time in minutes with these multipliers?**

- **Day:**  
  Default Minecraft day is 12,000 ticks (10 minutes).  
  With `day_multiplier=0.5`, the day lasts twice as long:  
  10 minutes × (1 / 0.5) = **20 minutes**

- **Night:**  
  Default Minecraft night is 12,000 ticks (10 minutes).  
  With `night_multiplier=1.0`, the night remains:  
  10 minutes × (1 / 1.0) = **10 minutes**

So, with these settings, each Minecraft day will last **20 minutes** and each night will last **10 minutes**.

## Commands

- `/customdaynight reload` → Reloads the config file
- `/customdaynight status` → Displays current settings
- `/customdaynight togglePhaseLogging` → Enable/disable phase transition messages **at runtime**
