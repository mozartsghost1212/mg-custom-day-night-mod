package com.github.mozartsghost1212.customdaynightmod;

import java.io.*;
import java.util.Properties;

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