package com.github.mozartsghost1212.customdaynightmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("customdaynight")
            .requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)) // full ops
            .executes(ctx -> showMenu(ctx.getSource()))
            .then(CommandManager.literal("menu")
                .executes(ctx -> showMenu(ctx.getSource())))
            .then(CommandManager.literal("reload")
                .executes(ctx -> reload(ctx.getSource())))
            .then(CommandManager.literal("save")
                .executes(ctx -> save(ctx.getSource())))
            .then(CommandManager.literal("defaults")
                .executes(ctx -> resetDefaults(ctx.getSource())))
            .then(CommandManager.literal("status")
                .executes(ctx -> status(ctx.getSource())))
            .then(CommandManager.literal("togglePhaseLogging")
                .executes(ctx -> togglePhaseLogging(ctx.getSource())))
            .then(CommandManager.literal("set")
                .then(CommandManager.literal("dayMultiplier")
                    .then(CommandManager.argument("value", FloatArgumentType.floatArg(0.01f))
                        .executes(ctx -> setDayMultiplier(ctx.getSource(), FloatArgumentType.getFloat(ctx, "value")))))
                .then(CommandManager.literal("nightMultiplier")
                    .then(CommandManager.argument("value", FloatArgumentType.floatArg(0.01f))
                        .executes(ctx -> setNightMultiplier(ctx.getSource(), FloatArgumentType.getFloat(ctx, "value")))))
                .then(CommandManager.literal("absoluteDayLength")
                    .then(CommandManager.argument("value", IntegerArgumentType.integer(0))
                        .executes(ctx -> setAbsoluteDayLength(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
                .then(CommandManager.literal("absoluteNightLength")
                    .then(CommandManager.argument("value", IntegerArgumentType.integer(0))
                        .executes(ctx -> setAbsoluteNightLength(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
            )
        );
    }

    // ── Interactive Menu ──────────────────────────────────────────────

    private static final int DEFAULT_HALF_CYCLE = 12000; // vanilla day or night length in ticks
    private static final int TICKS_PER_SECOND = 20;

    private static int showMenu(ServerCommandSource source) {
        MutableText divider = Text.literal("═══════════════════════════════════").formatted(Formatting.GOLD);
        MutableText thinDiv = Text.literal("  ──────────────────────────────────").formatted(Formatting.DARK_GRAY);
        MutableText title = Text.literal("  MozartsGhost1212 Custom Day/Night Mod").formatted(Formatting.YELLOW);

        send(source, divider);
        send(source, title);
        send(source, divider);

        // Day Multiplier
        send(source, createFloatRow("Day Multiplier", ModConfig.dayMultiplier, "dayMultiplier", MULTIPLIER_STEP));

        // Night Multiplier
        send(source, createFloatRow("Night Multiplier", ModConfig.nightMultiplier, "nightMultiplier", MULTIPLIER_STEP));

        send(source, Text.empty());

        // Absolute Day Length
        send(source, createIntRow("Abs. Day Length", ModConfig.absoluteDayLength, "absoluteDayLength", ABSOLUTE_LENGTH_STEP));

        // Absolute Night Length
        send(source, createIntRow("Abs. Night Length", ModConfig.absoluteNightLength, "absoluteNightLength", ABSOLUTE_LENGTH_STEP));

        send(source, Text.empty());

        // Log Phase Changes
        send(source, createBoolRow("Phase Logging", ModConfig.logPhaseChanges));

        send(source, thinDiv);

        // ── Result Summary ──
        int dayTicks = computeEffectiveTicks(ModConfig.absoluteDayLength, ModConfig.dayMultiplier);
        int nightTicks = computeEffectiveTicks(ModConfig.absoluteNightLength, ModConfig.nightMultiplier);
        int totalTicks = dayTicks + nightTicks;

        send(source, Text.literal("  ☀ Day: ").formatted(Formatting.GRAY)
            .append(Text.literal(formatDuration(dayTicks)).formatted(Formatting.GOLD, Formatting.BOLD))
            .append(Text.literal("  (" + dayTicks + " ticks)").formatted(Formatting.DARK_GRAY)));

        send(source, Text.literal("  ☽ Night: ").formatted(Formatting.GRAY)
            .append(Text.literal(formatDuration(nightTicks)).formatted(Formatting.BLUE, Formatting.BOLD))
            .append(Text.literal("  (" + nightTicks + " ticks)").formatted(Formatting.DARK_GRAY)));

        send(source, Text.literal("  ⏱ Full Cycle: ").formatted(Formatting.GRAY)
            .append(Text.literal(formatDuration(totalTicks)).formatted(Formatting.WHITE, Formatting.BOLD))
            .append(Text.literal("  (" + totalTicks + " ticks)").formatted(Formatting.DARK_GRAY)));

        send(source, thinDiv);

        // Action buttons
        MutableText actions = Text.literal("  ")
            .append(createButton("[Save]", "/customdaynight save", "Save settings to file", Formatting.GREEN))
            .append(Text.literal("  "))
            .append(createButton("[Reload]", "/customdaynight reload", "Reload settings from file", Formatting.AQUA))
            .append(Text.literal("  "))
            .append(createButton("[Defaults]", "/customdaynight defaults", "Reset to default values", Formatting.RED));

        send(source, actions);
        send(source, Text.literal("  ").append(
            Text.literal("Tip: Press T to open chat, then click buttons above.").formatted(Formatting.DARK_GRAY, Formatting.ITALIC)));
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

    private static void send(ServerCommandSource source, Text text) {
        source.sendFeedback(() -> text, false);
    }

    private static MutableText createButton(String label, String command, String tooltip, Formatting color) {
        return Text.literal(label)
            .formatted(color, Formatting.BOLD)
            .styled(style -> style
                .withClickEvent(new ClickEvent.RunCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal(tooltip))));
    }

    private static MutableText createSuggestButton(String label, String command, String tooltip, Formatting color) {
        return Text.literal(label)
            .formatted(color)
            .styled(style -> style
                .withClickEvent(new ClickEvent.SuggestCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal(tooltip))));
    }

    private static MutableText createFloatRow(String label, float value, String property, float step) {
        float decreased = Math.max(ModConfig.MIN_MULTIPLIER, value - step);
        float increased = Math.min(ModConfig.MAX_MULTIPLIER, value + step);
        // Format to avoid floating-point noise (e.g. 1.7500001)
        String decStr = String.format("%.2f", decreased);
        String incStr = String.format("%.2f", increased);

        return Text.literal("  " + label + ": ").formatted(Formatting.GRAY)
            .append(Text.literal(String.format("%.2f", value)).formatted(Formatting.WHITE, Formatting.BOLD))
            .append(Text.literal("  "))
            .append(createButton("[-]", "/customdaynight set " + property + " " + decStr,
                "Decrease to " + decStr, Formatting.RED))
            .append(Text.literal(" "))
            .append(createButton("[+]", "/customdaynight set " + property + " " + incStr,
                "Increase to " + incStr, Formatting.GREEN))
            .append(Text.literal(" "))
            .append(createSuggestButton("[✎]", "/customdaynight set " + property + " ",
                "Type a custom value", Formatting.YELLOW));
    }

    private static MutableText createIntRow(String label, int value, String property, int step) {
        // Allow 0 (disabled); otherwise clamp to MIN_ABSOLUTE_LENGTH
        int decreased = (value - step <= 0) ? 0 : Math.max(ModConfig.MIN_ABSOLUTE_LENGTH, value - step);
        int increased = Math.min(ModConfig.MAX_ABSOLUTE_LENGTH, value + step);

        return Text.literal("  " + label + ": ").formatted(Formatting.GRAY)
            .append(Text.literal(String.valueOf(value)).formatted(Formatting.WHITE, Formatting.BOLD))
            .append(Text.literal("  "))
            .append(createButton("[-]", "/customdaynight set " + property + " " + decreased,
                "Decrease to " + decreased, Formatting.RED))
            .append(Text.literal(" "))
            .append(createButton("[+]", "/customdaynight set " + property + " " + increased,
                "Increase to " + increased, Formatting.GREEN))
            .append(Text.literal(" "))
            .append(createSuggestButton("[✎]", "/customdaynight set " + property + " ",
                "Type a custom value", Formatting.YELLOW));
    }

    private static MutableText createBoolRow(String label, boolean value) {
        return Text.literal("  " + label + ": ").formatted(Formatting.GRAY)
            .append(Text.literal(value ? "ON" : "OFF")
                .formatted(value ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
            .append(Text.literal("  "))
            .append(createButton("[Toggle]", "/customdaynight togglePhaseLogging",
                "Toggle phase logging on/off", Formatting.YELLOW));
    }

    // ── Set Commands (update value, then re-display menu) ─────────────

    private static int setDayMultiplier(ServerCommandSource source, float value) {
        float clamped = ModConfig.clampMultiplier(value);
        ModConfig.dayMultiplier = clamped;
        if (clamped != value) {
            source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " Value clamped to "
                + String.format("%.2f", clamped) + " (allowed: " + ModConfig.MIN_MULTIPLIER + "–" + ModConfig.MAX_MULTIPLIER + ")").formatted(Formatting.RED), true);
        }
        source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " Day multiplier set to "
            + String.format("%.2f", clamped)).formatted(Formatting.GREEN), true);
        return showMenu(source);
    }

    private static int setNightMultiplier(ServerCommandSource source, float value) {
        float clamped = ModConfig.clampMultiplier(value);
        ModConfig.nightMultiplier = clamped;
        if (clamped != value) {
            source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " Value clamped to "
                + String.format("%.2f", clamped) + " (allowed: " + ModConfig.MIN_MULTIPLIER + "–" + ModConfig.MAX_MULTIPLIER + ")").formatted(Formatting.RED), true);
        }
        source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " Night multiplier set to "
            + String.format("%.2f", clamped)).formatted(Formatting.GREEN), true);
        return showMenu(source);
    }

    private static int setAbsoluteDayLength(ServerCommandSource source, int value) {
        int clamped = ModConfig.clampAbsoluteLength(value);
        ModConfig.absoluteDayLength = clamped;
        if (clamped != value) {
            source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " Value clamped to "
                + clamped + " (min: " + ModConfig.MIN_ABSOLUTE_LENGTH + ", max: " + ModConfig.MAX_ABSOLUTE_LENGTH + ", 0=off)").formatted(Formatting.RED), true);
        }
        source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " Absolute day length set to "
            + clamped).formatted(Formatting.GREEN), true);
        return showMenu(source);
    }

    private static int setAbsoluteNightLength(ServerCommandSource source, int value) {
        int clamped = ModConfig.clampAbsoluteLength(value);
        ModConfig.absoluteNightLength = clamped;
        if (clamped != value) {
            source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " Value clamped to "
                + clamped + " (min: " + ModConfig.MIN_ABSOLUTE_LENGTH + ", max: " + ModConfig.MAX_ABSOLUTE_LENGTH + ", 0=off)").formatted(Formatting.RED), true);
        }
        source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " Absolute night length set to "
            + clamped).formatted(Formatting.GREEN), true);
        return showMenu(source);
    }

    // ── Action Commands ───────────────────────────────────────────────

    private static int reload(ServerCommandSource source) {
        ModConfig.loadConfig();
        source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " Configuration reloaded from file.")
            .formatted(Formatting.AQUA), true);
        return showMenu(source);
    }

    private static int save(ServerCommandSource source) {
        ModConfig.saveConfig();
        source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " Configuration saved to file.")
            .formatted(Formatting.GREEN), true);
        return showMenu(source);
    }

    private static int resetDefaults(ServerCommandSource source) {
        ModConfig.resetDefaults();
        source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " All settings reset to defaults.")
            .formatted(Formatting.RED), true);
        return showMenu(source);
    }

    private static int status(ServerCommandSource source) {
        return showMenu(source);
    }

    private static int togglePhaseLogging(ServerCommandSource source) {
        ModConfig.logPhaseChanges = !ModConfig.logPhaseChanges;
        source.sendFeedback(() -> Text.literal(CustomDayNightMod.LOG_PREFIX + " Phase logging is now "
            + (ModConfig.logPhaseChanges ? "ON" : "OFF")).formatted(Formatting.YELLOW), true);
        return showMenu(source);
    }
}