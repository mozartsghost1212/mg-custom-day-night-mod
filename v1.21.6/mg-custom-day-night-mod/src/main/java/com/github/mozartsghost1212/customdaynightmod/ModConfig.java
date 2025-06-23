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
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("# Custom Day/Night Mod Config\n");
                    writer.write("day_multiplier=1.0\n");
                    writer.write("night_multiplier=1.0\n");
                    writer.write("absolute_day_length=0\n");
                    writer.write("absolute_night_length=0\n");
                    writer.write("log_phase_changes=true\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (FileReader reader = new FileReader(file)) {
            props.load(reader);

            dayMultiplier = Float.parseFloat(props.getProperty("day_multiplier", "1.0"));
            nightMultiplier = Float.parseFloat(props.getProperty("night_multiplier", "1.0"));
            absoluteDayLength = Integer.parseInt(props.getProperty("absolute_day_length", "0"));
            absoluteNightLength = Integer.parseInt(props.getProperty("absolute_night_length", "0"));
            logPhaseChanges = Boolean.parseBoolean(props.getProperty("log_phase_changes", "true"));

            System.out.println("[CustomDayNightMod] Loaded config: dayMultiplier=" + dayMultiplier +
                    ", nightMultiplier=" + nightMultiplier +
                    ", absoluteDayLength=" + absoluteDayLength +
                    ", absoluteNightLength=" + absoluteNightLength +
                    ", logPhaseChanges=" + logPhaseChanges);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}