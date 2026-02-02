package com.github.mozartsghost1212.customdaynightmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

/**
 * The {@code CustomDayNightMod} class implements a Minecraft Fabric mod that allows customization
 * of the day and night cycle lengths in the game. It listens to server tick events and adjusts the
 * world's time progression based on configurable multipliers or absolute lengths for day and night phases.
 * 
 * <p>Features:
 * <ul>
 *   <li>Customizable day and night lengths via multipliers or absolute tick values.</li>
 *   <li>Logs phase changes (day/night) if enabled in the configuration.</li>
 *   <li>Registers custom commands for in-game configuration.</li>
 * </ul>
 * 
 * <p>Configuration is loaded from {@code ModConfig}, and the mod operates only in the Overworld dimension.
 * 
 * <p>Implements {@link net.fabricmc.api.ModInitializer} to hook into the mod initialization lifecycle.
 */
public class CustomDayNightMod implements ModInitializer {

    private enum Phase { DAY, NIGHT }
    private Phase previousPhase = null;

    @Override
    public void onInitialize() {
        System.out.println("[CustomDayNightMod] Initializing Custom Day/Night Mod...");
        ModConfig.loadConfig();

        ServerTickEvents.START_SERVER_TICK.register(server -> onServerTick(server));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CustomDayNightCommand.register(dispatcher);
        });

        System.out.println("[CustomDayNightMod] Custom Day/Night Mod successfully registered!");
    }

    /**
     * Handles the server tick event to control the day and night cycle in the Overworld.
     * <p>
     * This method adjusts the world's time progression based on custom configuration settings,
     * allowing for custom day and night lengths or multipliers. It also logs phase changes
     * (from day to night or vice versa) if enabled in the configuration.
     * </p>
     *
     * @param server The {@link MinecraftServer} instance for which the tick is being processed.
     */
    private void onServerTick(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            if (world.getRegistryKey() == ServerWorld.OVERWORLD) {
                long time = world.getTimeOfDay() % 24000L;
                float multiplier;
                Phase currentPhase = (time < 12000L) ? Phase.DAY : Phase.NIGHT;

                if (previousPhase != currentPhase) {
                    if (ModConfig.logPhaseChanges) {
                        if (currentPhase == Phase.DAY) {
                            if (ModConfig.absoluteDayLength > 0) {
                                System.out.println("[CustomDayNightMod] Entering DAY phase... (absolute length: " + ModConfig.absoluteDayLength + " ticks)");
                            } else {
                                System.out.println("[CustomDayNightMod] Entering DAY phase... (multiplier: " + ModConfig.dayMultiplier + ")");
                            }
                        } else {
                            if (ModConfig.absoluteNightLength > 0) {
                                System.out.println("[CustomDayNightMod] Entering NIGHT phase... (absolute length: " + ModConfig.absoluteNightLength + " ticks)");
                            } else {
                                System.out.println("[CustomDayNightMod] Entering NIGHT phase... (multiplier: " + ModConfig.nightMultiplier + ")");
                            }
                        }
                    }
                    previousPhase = currentPhase;
                }

                if (ModConfig.absoluteDayLength > 0 && time < 12000L) {
                    multiplier = (24000f / 2) / ModConfig.absoluteDayLength;
                } else if (ModConfig.absoluteNightLength > 0 && time >= 12000L) {
                    multiplier = (24000f / 2) / ModConfig.absoluteNightLength;
                } else {
                    multiplier = (time < 12000L) ? ModConfig.dayMultiplier : ModConfig.nightMultiplier;
                }

                long newTime = world.getTimeOfDay() + (long) multiplier;
                world.setTimeOfDay(newTime);
            }
        }
    }
}