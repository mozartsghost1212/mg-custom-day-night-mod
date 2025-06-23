package com.github.mozartsghost1212.customdaynightmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CustomDayNightCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("customdaynight")
            .requires(source -> source.hasPermissionLevel(4))  // Restrict to Full operators
            .then(CommandManager.literal("reload")
                .executes(ctx -> reload(ctx.getSource())))
            .then(CommandManager.literal("status")
                .executes(ctx -> status(ctx.getSource())))
            .then(CommandManager.literal("togglePhaseLogging")
                .executes(ctx -> togglePhaseLogging(ctx.getSource())))
        );
    }

    private static int reload(ServerCommandSource source) {
        ModConfig.loadConfig();
        source.getServer().getPlayerManager().broadcast(Text.of("[CustomDayNightMod] Configuration reloaded."), false);
        return 1;
    }

    private static int status(ServerCommandSource source) {
        String msg = String.format(
            "[CustomDayNightMod] dayMultiplier=%.2f, nightMultiplier=%.2f, absoluteDayLength=%d, absoluteNightLength=%d, logPhaseChanges=%b",
            ModConfig.dayMultiplier, ModConfig.nightMultiplier,
            ModConfig.absoluteDayLength, ModConfig.absoluteNightLength,
            ModConfig.logPhaseChanges
        );
        source.sendFeedback(() -> Text.of(msg), false);
        return 1;
    }

    private static int togglePhaseLogging(ServerCommandSource source) {
        ModConfig.logPhaseChanges = !ModConfig.logPhaseChanges;
        String msg = "[CustomDayNightMod] logPhaseChanges is now set to " + ModConfig.logPhaseChanges;
        source.getServer().getPlayerManager().broadcast(Text.of(msg), false);
        return 1;
    }
}