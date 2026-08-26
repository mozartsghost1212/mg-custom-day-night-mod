package com.github.mozartsghost1212.customdaynightmod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * ModConfig handles the configuration for the Custom Day/Night Mod.
 * <p>
 * This class manages the loading and saving of configuration properties
 * such as day and night multipliers, absolute day/night lengths, and
 * logging preferences. The configuration is stored in a properties file
 * located at "config/customdaynightmod.properties".
 * </p>
 *
 * <ul>
 *   <li><b>dayMultiplier</b>: Multiplier for the length of the day phase.</li>
 *   <li><b>nightMultiplier</b>: Multiplier for the length of the night phase.</li>
 *   <li><b>absoluteDayLength</b>: Absolute length of the day phase (overrides multiplier if set).</li>
 *   <li><b>absoluteNightLength</b>: Absolute length of the night phase (overrides multiplier if set).</li>
 *   <li><b>logPhaseChanges</b>: Whether to log day/night phase changes.</li>
 * </ul>
 *
 * The {@link #loadConfig()} method loads the configuration from the file,
 * creating it with default values if it does not exist.
 */
public class ModConfig {
    // ── Bounds ──
    public static final float MIN_MULTIPLIER = 0.01f;
    public static final float MAX_MULTIPLIER = 100.0f;
    /** Minimum absolute length when enabled (>0). 20 ticks = 1 second. */
    public static final int MIN_ABSOLUTE_LENGTH = 20;
    public static final int MAX_ABSOLUTE_LENGTH = 2_400_000; // ~2h real-time

    public static float dayMultiplier = 1.0f;
    public static float nightMultiplier = 1.0f;
    public static int absoluteDayLength = 0;
    public static int absoluteNightLength = 0;
    public static boolean logPhaseChanges = true;

    private static final String CONFIG_PATH = "config/customdaynightmod.properties";

    public static void loadConfig() {
        Properties props = new Properties();

        File file = new File(CONFIG_PATH);
        if (!file.exists()) {
            saveConfig(); // Create default config file
        }

        try (FileReader reader = new FileReader(file)) {
            props.load(reader);

            dayMultiplier = Float.parseFloat(props.getProperty("day_multiplier", "1.0"));
            nightMultiplier = Float.parseFloat(props.getProperty("night_multiplier", "1.0"));
            absoluteDayLength = Integer.parseInt(props.getProperty("absolute_day_length", "0"));
            absoluteNightLength = Integer.parseInt(props.getProperty("absolute_night_length", "0"));
            logPhaseChanges = Boolean.parseBoolean(props.getProperty("log_phase_changes", "true"));

            clampValues();

            System.out.println(CustomDayNightMod.LOG_PREFIX + " Loaded config: dayMultiplier=" + dayMultiplier +
                    ", nightMultiplier=" + nightMultiplier +
                    ", absoluteDayLength=" + absoluteDayLength +
                    ", absoluteNightLength=" + absoluteNightLength +
                    ", logPhaseChanges=" + logPhaseChanges);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the current configuration values to the properties file on disk.
     */
    public static void saveConfig() {
        File file = new File(CONFIG_PATH);
        try {
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("# Custom Day/Night Mod Config\n");
                writer.write("day_multiplier=" + dayMultiplier + "\n");
                writer.write("night_multiplier=" + nightMultiplier + "\n");
                writer.write("absolute_day_length=" + absoluteDayLength + "\n");
                writer.write("absolute_night_length=" + absoluteNightLength + "\n");
                writer.write("log_phase_changes=" + logPhaseChanges + "\n");
            }
            System.out.println(CustomDayNightMod.LOG_PREFIX + " Config saved to disk.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets all configuration values to their defaults (does not save to disk).
     */
    public static void resetDefaults() {
        dayMultiplier = 1.0f;
        nightMultiplier = 1.0f;
        absoluteDayLength = 0;
        absoluteNightLength = 0;
        logPhaseChanges = true;
    }

    /**
     * Clamps all values to safe ranges to prevent crashes.
     * Absolute lengths of 0 are allowed (meaning "disabled").
     */
    public static void clampValues() {
        dayMultiplier = Math.max(MIN_MULTIPLIER, Math.min(MAX_MULTIPLIER, dayMultiplier));
        nightMultiplier = Math.max(MIN_MULTIPLIER, Math.min(MAX_MULTIPLIER, nightMultiplier));
        if (absoluteDayLength != 0) {
            absoluteDayLength = Math.max(MIN_ABSOLUTE_LENGTH, Math.min(MAX_ABSOLUTE_LENGTH, absoluteDayLength));
        }
        if (absoluteNightLength != 0) {
            absoluteNightLength = Math.max(MIN_ABSOLUTE_LENGTH, Math.min(MAX_ABSOLUTE_LENGTH, absoluteNightLength));
        }
    }

    /** Clamps a multiplier value to safe bounds. */
    public static float clampMultiplier(float value) {
        return Math.max(MIN_MULTIPLIER, Math.min(MAX_MULTIPLIER, value));
    }

    /** Clamps an absolute length value to safe bounds (0 = disabled). */
    public static int clampAbsoluteLength(int value) {
        if (value <= 0) return 0;
        return Math.max(MIN_ABSOLUTE_LENGTH, Math.min(MAX_ABSOLUTE_LENGTH, value));
    }
}