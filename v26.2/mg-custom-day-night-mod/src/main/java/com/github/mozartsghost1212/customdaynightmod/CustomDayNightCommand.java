package com.github.mozartsghost1212.customdaynightmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

/**
 * Handles registration and execution of custom commands for the Custom Day/Night Mod.
 * <p>
 * Provides the following commands for server operators:
 * <ul>
 *     <li><b>/customdaynight menu</b>: Opens an interactive chat-based configuration menu.</li>
 *     <li><b>/customdaynight reload</b>: Reloads the mod configuration from disk.</li>
 *     <li><b>/customdaynight save</b>: Saves the current configuration to disk.</li>
 *     <li><b>/customdaynight defaults</b>: Resets all settings to default values.</li>
 *     <li><b>/customdaynight status</b>: Displays the current configuration values.</li>
 *     <li><b>/customdaynight togglePhaseLogging</b>: Toggles logging of day/night phase changes.</li>
 *     <li><b>/customdaynight set &lt;property&gt; &lt;value&gt;</b>: Sets a configuration property.</li>
 * </ul>
 * <p>
 * The interactive menu uses clickable chat text components so no client-side mod is required.
 * All commands require operator permission level 4.
 */
public class CustomDayNightCommand {

    private static final float MULTIPLIER_STEP = 0.25f;
    private static final int ABSOLUTE_LENGTH_STEP = 1000;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("customdaynight")
            .requires(Commands.hasPermission(Commands.LEVEL_OWNERS)) // full ops
            .executes(ctx -> showMenu(ctx.getSource()))
            .then(Commands.literal("menu")
                .executes(ctx -> showMenu(ctx.getSource())))
            .then(Commands.literal("reload")
                .executes(ctx -> reload(ctx.getSource())))
            .then(Commands.literal("save")
                .executes(ctx -> save(ctx.getSource())))
            .then(Commands.literal("defaults")
                .executes(ctx -> resetDefaults(ctx.getSource())))
            .then(Commands.literal("status")
                .executes(ctx -> status(ctx.getSource())))
            .then(Commands.literal("togglePhaseLogging")
                .executes(ctx -> togglePhaseLogging(ctx.getSource())))
            .then(Commands.literal("set")
                .then(Commands.literal("dayMultiplier")
                    .then(Commands.argument("value", FloatArgumentType.floatArg(0.01f))
                        .executes(ctx -> setDayMultiplier(ctx.getSource(), FloatArgumentType.getFloat(ctx, "value")))))
                .then(Commands.literal("nightMultiplier")
                    .then(Commands.argument("value", FloatArgumentType.floatArg(0.01f))
                        .executes(ctx -> setNightMultiplier(ctx.getSource(), FloatArgumentType.getFloat(ctx, "value")))))
                .then(Commands.literal("absoluteDayLength")
                    .then(Commands.argument("value", IntegerArgumentType.integer(0))
                        .executes(ctx -> setAbsoluteDayLength(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
                .then(Commands.literal("absoluteNightLength")
                    .then(Commands.argument("value", IntegerArgumentType.integer(0))
                        .executes(ctx -> setAbsoluteNightLength(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
            )
        );
    }

    // ── Interactive Menu ──────────────────────────────────────────────

    private static final int DEFAULT_HALF_CYCLE = 12000; // vanilla day or night length in ticks
    private static final int TICKS_PER_SECOND = 20;

    private static int showMenu(CommandSourceStack source) {
        MutableComponent divider = Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD);
        MutableComponent thinDiv = Component.literal("  ──────────────────────────────────").withStyle(ChatFormatting.DARK_GRAY);
        MutableComponent title = Component.literal("  MozartsGhost1212 Custom Day/Night Mod").withStyle(ChatFormatting.YELLOW);

        send(source, divider);
        send(source, title);
        send(source, divider);

        // Day Multiplier
        send(source, createFloatRow("Day Multiplier", ModConfig.dayMultiplier, "dayMultiplier", MULTIPLIER_STEP));

        // Night Multiplier
        send(source, createFloatRow("Night Multiplier", ModConfig.nightMultiplier, "nightMultiplier", MULTIPLIER_STEP));

        send(source, Component.empty());

        // Absolute Day Length
        send(source, createIntRow("Abs. Day Length", ModConfig.absoluteDayLength, "absoluteDayLength", ABSOLUTE_LENGTH_STEP));

        // Absolute Night Length
        send(source, createIntRow("Abs. Night Length", ModConfig.absoluteNightLength, "absoluteNightLength", ABSOLUTE_LENGTH_STEP));

        send(source, Component.empty());

        // Log Phase Changes
        send(source, createBoolRow("Phase Logging", ModConfig.logPhaseChanges));

        send(source, thinDiv);

        // ── Result Summary ──
        int dayTicks = computeEffectiveTicks(ModConfig.absoluteDayLength, ModConfig.dayMultiplier);
        int nightTicks = computeEffectiveTicks(ModConfig.absoluteNightLength, ModConfig.nightMultiplier);
        int totalTicks = dayTicks + nightTicks;

        send(source, Component.literal("  ☀ Day: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(formatDuration(dayTicks)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal("  (" + dayTicks + " ticks)").withStyle(ChatFormatting.DARK_GRAY)));

        send(source, Component.literal("  ☽ Night: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(formatDuration(nightTicks)).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD))
            .append(Component.literal("  (" + nightTicks + " ticks)").withStyle(ChatFormatting.DARK_GRAY)));

        send(source, Component.literal("  ⏱ Full Cycle: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(formatDuration(totalTicks)).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
            .append(Component.literal("  (" + totalTicks + " ticks)").withStyle(ChatFormatting.DARK_GRAY)));

        send(source, thinDiv);

        // Action buttons
        MutableComponent actions = Component.literal("  ")
            .append(createButton("[Save]", "/customdaynight save", "Save settings to file", ChatFormatting.GREEN))
            .append(Component.literal("  "))
            .append(createButton("[Reload]", "/customdaynight reload", "Reload settings from file", ChatFormatting.AQUA))
            .append(Component.literal("  "))
            .append(createButton("[Defaults]", "/customdaynight defaults", "Reset to default values", ChatFormatting.RED));

        send(source, actions);
        send(source, Component.literal("  ").append(
            Component.literal("Tip: Press T to open chat, click a button, then press Enter.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
        send(source, divider);

        return 1;
    }

    // ── Duration Helpers ──────────────────────────────────────────────

    /**
     * Computes the effective real-time duration in ticks for a phase.
     * If absoluteLength > 0, it is used directly. Otherwise, the vanilla
     * half-cycle (12 000 ticks) is scaled by the inverse of the multiplier.
     */
    private static int computeEffectiveTicks(int absoluteLength, float multiplier) {
        if (absoluteLength > 0) {
            return absoluteLength;
        }
        // multiplier scales speed, so real-time duration = base / multiplier
        return Math.round(DEFAULT_HALF_CYCLE / multiplier);
    }

    /**
     * Formats a tick count into a human-readable duration string.
     * Examples: "10m 0s", "1h 23m 20s", "30s"
     */
    private static String formatDuration(int ticks) {
        int totalSeconds = ticks / TICKS_PER_SECOND;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    // ── Menu Component Helpers ────────────────────────────────────────

    private static void send(CommandSourceStack source, Component text) {
        source.sendSuccess(() -> text, false);
    }

    private static MutableComponent createButton(String label, String command, String tooltip, ChatFormatting color) {
        return Component.literal(label)
            .withStyle(color, ChatFormatting.BOLD)
            .withStyle(style -> style
                .withClickEvent(new ClickEvent.SuggestCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(tooltip + " (press Enter to confirm)"))));
    }

    private static MutableComponent createSuggestButton(String label, String command, String tooltip, ChatFormatting color) {
        return Component.literal(label)
            .withStyle(color)
            .withStyle(style -> style
                .withClickEvent(new ClickEvent.SuggestCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(tooltip + " (press Enter to confirm)"))));
    }

    private static MutableComponent createFloatRow(String label, float value, String property, float step) {
        float decreased = Math.max(ModConfig.MIN_MULTIPLIER, value - step);
        float increased = Math.min(ModConfig.MAX_MULTIPLIER, value + step);
        // Format to avoid floating-point noise (e.g. 1.7500001)
        String decStr = String.format("%.2f", decreased);
        String incStr = String.format("%.2f", increased);

        return Component.literal("  " + label + ": ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.2f", value)).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
            .append(Component.literal("  "))
            .append(createButton("[-]", "/customdaynight set " + property + " " + decStr,
                "Decrease to " + decStr, ChatFormatting.RED))
            .append(Component.literal(" "))
            .append(createButton("[+]", "/customdaynight set " + property + " " + incStr,
                "Increase to " + incStr, ChatFormatting.GREEN))
            .append(Component.literal(" "))
            .append(createSuggestButton("[✎]", "/customdaynight set " + property + " ",
                "Type a custom value", ChatFormatting.YELLOW));
    }

    private static MutableComponent createIntRow(String label, int value, String property, int step) {
        // Allow 0 (disabled); otherwise clamp to MIN_ABSOLUTE_LENGTH
        int decreased = (value - step <= 0) ? 0 : Math.max(ModConfig.MIN_ABSOLUTE_LENGTH, value - step);
        int increased = Math.min(ModConfig.MAX_ABSOLUTE_LENGTH, value + step);

        return Component.literal("  " + label + ": ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
            .append(Component.literal("  "))
            .append(createButton("[-]", "/customdaynight set " + property + " " + decreased,
                "Decrease to " + decreased, ChatFormatting.RED))
            .append(Component.literal(" "))
            .append(createButton("[+]", "/customdaynight set " + property + " " + increased,
                "Increase to " + increased, ChatFormatting.GREEN))
            .append(Component.literal(" "))
            .append(createSuggestButton("[✎]", "/customdaynight set " + property + " ",
                "Type a custom value", ChatFormatting.YELLOW));
    }

    private static MutableComponent createBoolRow(String label, boolean value) {
        return Component.literal("  " + label + ": ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(value ? "ON" : "OFF")
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
            .append(Component.literal("  "))
            .append(createButton("[Toggle]", "/customdaynight togglePhaseLogging",
                "Toggle phase logging on/off", ChatFormatting.YELLOW));
    }

    // ── Set Commands (update value, then re-display menu) ─────────────

    private static int setDayMultiplier(CommandSourceStack source, float value) {
        float clamped = ModConfig.clampMultiplier(value);
        ModConfig.dayMultiplier = clamped;
        if (clamped != value) {
            source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " Value clamped to "
                + String.format("%.2f", clamped) + " (allowed: " + ModConfig.MIN_MULTIPLIER + "–" + ModConfig.MAX_MULTIPLIER + ")").withStyle(ChatFormatting.RED), true);
        }
        source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " Day multiplier set to "
            + String.format("%.2f", clamped)).withStyle(ChatFormatting.GREEN), true);
        return showMenu(source);
    }

    private static int setNightMultiplier(CommandSourceStack source, float value) {
        float clamped = ModConfig.clampMultiplier(value);
        ModConfig.nightMultiplier = clamped;
        if (clamped != value) {
            source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " Value clamped to "
                + String.format("%.2f", clamped) + " (allowed: " + ModConfig.MIN_MULTIPLIER + "–" + ModConfig.MAX_MULTIPLIER + ")").withStyle(ChatFormatting.RED), true);
        }
        source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " Night multiplier set to "
            + String.format("%.2f", clamped)).withStyle(ChatFormatting.GREEN), true);
        return showMenu(source);
    }

    private static int setAbsoluteDayLength(CommandSourceStack source, int value) {
        int clamped = ModConfig.clampAbsoluteLength(value);
        ModConfig.absoluteDayLength = clamped;
        if (clamped != value) {
            source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " Value clamped to "
                + clamped + " (min: " + ModConfig.MIN_ABSOLUTE_LENGTH + ", max: " + ModConfig.MAX_ABSOLUTE_LENGTH + ", 0=off)").withStyle(ChatFormatting.RED), true);
        }
        source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " Absolute day length set to "
            + clamped).withStyle(ChatFormatting.GREEN), true);
        return showMenu(source);
    }

    private static int setAbsoluteNightLength(CommandSourceStack source, int value) {
        int clamped = ModConfig.clampAbsoluteLength(value);
        ModConfig.absoluteNightLength = clamped;
        if (clamped != value) {
            source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " Value clamped to "
                + clamped + " (min: " + ModConfig.MIN_ABSOLUTE_LENGTH + ", max: " + ModConfig.MAX_ABSOLUTE_LENGTH + ", 0=off)").withStyle(ChatFormatting.RED), true);
        }
        source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " Absolute night length set to "
            + clamped).withStyle(ChatFormatting.GREEN), true);
        return showMenu(source);
    }

    // ── Action Commands ───────────────────────────────────────────────

    private static int reload(CommandSourceStack source) {
        ModConfig.loadConfig();
        source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " Configuration reloaded from file.")
            .withStyle(ChatFormatting.AQUA), true);
        return showMenu(source);
    }

    private static int save(CommandSourceStack source) {
        ModConfig.saveConfig();
        source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " Configuration saved to file.")
            .withStyle(ChatFormatting.GREEN), true);
        return showMenu(source);
    }

    private static int resetDefaults(CommandSourceStack source) {
        ModConfig.resetDefaults();
        source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " All settings reset to defaults.")
            .withStyle(ChatFormatting.RED), true);
        return showMenu(source);
    }

    private static int status(CommandSourceStack source) {
        return showMenu(source);
    }

    private static int togglePhaseLogging(CommandSourceStack source) {
        ModConfig.logPhaseChanges = !ModConfig.logPhaseChanges;
        source.sendSuccess(() -> Component.literal(CustomDayNightMod.LOG_PREFIX + " Phase logging is now "
            + (ModConfig.logPhaseChanges ? "ON" : "OFF")).withStyle(ChatFormatting.YELLOW), true);
        return showMenu(source);
    }
}
