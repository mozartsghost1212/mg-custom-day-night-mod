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
            .then(Commands.literal("duration")
                .then(Commands.literal("day")
                    .then(Commands.argument("minutes", FloatArgumentType.floatArg(0.0f))
                        .executes(ctx -> setDurationMinutes(ctx.getSource(), "day", FloatArgumentType.getFloat(ctx, "minutes")))))
                .then(Commands.literal("night")
                    .then(Commands.argument("minutes", FloatArgumentType.floatArg(0.0f))
                        .executes(ctx -> setDurationMinutes(ctx.getSource(), "night", FloatArgumentType.getFloat(ctx, "minutes")))))
            )
        );
    }

    // ── Interactive Menu ──────────────────────────────────────────────

    private static final int DEFAULT_HALF_CYCLE = 12000; // vanilla day or night length in ticks
    private static final int TICKS_PER_SECOND = 20;

    private static int showMenu(CommandSourceStack source) {
        MutableComponent divider = Component.literal("═══════════════════════════════════").withStyle(ChatFormatting.GOLD);
        MutableComponent thinDiv = Component.literal("  ──────────────────────────────────").withStyle(ChatFormatting.DARK_GRAY);
        String modVersion = CustomDayNightMod.LOG_PREFIX.replace("[CustomDayNightMod ", "").replace("]", "");
        MutableComponent title = Component.literal("  Custom Day/Night Mod  ").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
            .append(Component.literal(modVersion).withStyle(ChatFormatting.DARK_GRAY));

        send(source, divider);
        send(source, title);
        send(source, divider);

        // ── Speed multipliers section ──
        send(source, sectionHeader("Speed multipliers  ", "lower = longer phase, higher = faster phase"));
        send(source, createFloatRow("Day speed", ModConfig.dayMultiplier, "dayMultiplier", MULTIPLIER_STEP));
        send(source, createFloatRow("Night speed", ModConfig.nightMultiplier, "nightMultiplier", MULTIPLIER_STEP));

        send(source, Component.empty());

        // ── Fixed lengths section (override the multipliers when > 0) ──
        send(source, sectionHeader("Fixed lengths (override)  ", "OFF = use multiplier above; otherwise phase lasts exactly this many ticks"));
        send(source, createIntRow("Day length", ModConfig.absoluteDayLength, "absoluteDayLength", ABSOLUTE_LENGTH_STEP));
        send(source, createIntRow("Night length", ModConfig.absoluteNightLength, "absoluteNightLength", ABSOLUTE_LENGTH_STEP));

        send(source, Component.empty());

        // ── Logging section ──
        send(source, createBoolRow("Log phase changes", ModConfig.logPhaseChanges));

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

        // Buttons pre-fill the chat bar; player presses Enter to confirm.
        // (Vanilla clients show a "Confirm Command Execution" dialog on run_command clicks that require op perms.)
        MutableComponent actions = Component.literal("  ")
            .append(createSuggestButton("[Save]", "/customdaynight save", "Write current settings to config file", ChatFormatting.GREEN))
            .append(Component.literal("  "))
            .append(createSuggestButton("[Reload]", "/customdaynight reload", "Discard unsaved changes and reload from file", ChatFormatting.AQUA))
            .append(Component.literal("  "))
            .append(createSuggestButton("[Defaults]", "/customdaynight defaults", "Reset all values to defaults (not yet saved)", ChatFormatting.RED));

        send(source, actions);
        send(source, Component.literal("  Press ").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
            .append(Component.literal("T").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD, ChatFormatting.ITALIC))
            .append(Component.literal(" to open chat, click a button, then press Enter.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
        send(source, divider);

        return 1;
    }

    private static MutableComponent sectionHeader(String label, String tooltip) {
        return Component.literal("  ").append(
            Component.literal(label).withStyle(ChatFormatting.YELLOW)
                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(tooltip)))))
            .append(Component.literal("(?)").withStyle(ChatFormatting.DARK_GRAY)
                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(tooltip)))));
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

    /** Button that pre-fills the chat bar so the player can press Enter to confirm.
     *  We avoid ClickEvent.RunCommand because vanilla clients pop a "Confirm Command Execution" dialog
     *  on every click when the target command requires elevated permissions. */
    private static MutableComponent createSuggestButton(String label, String command, String tooltip, ChatFormatting color) {
        return Component.literal(label)
            .withStyle(color, ChatFormatting.BOLD)
            .withStyle(style -> style
                .withClickEvent(new ClickEvent.SuggestCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal(tooltip + "\n(pre-fills chat — press Enter to confirm)"))));
    }

    private static MutableComponent createFloatRow(String label, float value, String property, float step) {
        float decreased = Math.max(ModConfig.MIN_MULTIPLIER, value - step);
        float increased = Math.min(ModConfig.MAX_MULTIPLIER, value + step);
        // Format to avoid floating-point noise (e.g. 1.7500001)
        String decStr = String.format("%.2f", decreased);
        String incStr = String.format("%.2f", increased);

        MutableComponent valueDisplay = Component.literal(String.format("%.2f", value))
            .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)
            .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(
                "1.00 = vanilla speed\n0.50 = twice as long\n2.00 = twice as fast\nallowed range: "
                + ModConfig.MIN_MULTIPLIER + " \u2013 " + ModConfig.MAX_MULTIPLIER))));

        return Component.literal("  " + label + ": ").withStyle(ChatFormatting.GRAY)
            .append(valueDisplay)
            .append(Component.literal("  "))
            .append(createSuggestButton("[\u2212]", "/customdaynight set " + property + " " + decStr,
                "Set to " + decStr, ChatFormatting.RED))
            .append(Component.literal(" "))
            .append(createSuggestButton("[+]", "/customdaynight set " + property + " " + incStr,
                "Set to " + incStr, ChatFormatting.GREEN))
            .append(Component.literal(" "))
            .append(createSuggestButton("[\u270E]", "/customdaynight set " + property + " ",
                "Type a custom value", ChatFormatting.YELLOW));
    }

    private static MutableComponent createIntRow(String label, int value, String property, int step) {
        // Allow 0 (disabled); otherwise clamp to MIN_ABSOLUTE_LENGTH
        int decreased = (value - step <= 0) ? 0 : Math.max(ModConfig.MIN_ABSOLUTE_LENGTH, value - step);
        int increased = Math.min(ModConfig.MAX_ABSOLUTE_LENGTH, value + step);

        MutableComponent valueDisplay;
        if (value == 0) {
            valueDisplay = Component.literal("OFF")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)
                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(
                    "0 ticks = disabled. The multiplier above controls this phase."))));
        } else {
            valueDisplay = Component.literal(value + " ticks")
                .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)
                .append(Component.literal("  (" + formatDuration(value) + ")").withStyle(ChatFormatting.DARK_GRAY))
                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(
                    "Phase lasts exactly " + value + " ticks (" + formatDuration(value) + ")\n"
                    + "allowed range: " + ModConfig.MIN_ABSOLUTE_LENGTH + " \u2013 " + ModConfig.MAX_ABSOLUTE_LENGTH + " ticks\n"
                    + "set to 0 to disable and use the multiplier instead"))));
        }

        return Component.literal("  " + label + ": ").withStyle(ChatFormatting.GRAY)
            .append(valueDisplay)
            .append(Component.literal("  "))
            .append(createSuggestButton("[\u2212]", "/customdaynight set " + property + " " + decreased,
                decreased == 0 ? "Disable (use multiplier)"
                               : "Set to " + decreased + " ticks (" + formatDuration(decreased) + ")",
                ChatFormatting.RED))
            .append(Component.literal(" "))
            .append(createSuggestButton("[+]", "/customdaynight set " + property + " " + increased,
                "Set to " + increased + " ticks (" + formatDuration(increased) + ")", ChatFormatting.GREEN))
            .append(Component.literal(" "))
            .append(createSuggestButton("[\u270E]", "/customdaynight set " + property + " ",
                "Type a custom value in ticks (0 to disable)", ChatFormatting.YELLOW));
    }

    private static String formatMinutes(float minutes) {
        if (minutes >= 60f && minutes == Math.round(minutes) && ((int) minutes) % 60 == 0) {
            return ((int) (minutes / 60f)) + "h";
        }
        if (minutes == Math.round(minutes)) {
            return ((int) minutes) + "m";
        }
        return String.format("%.2f", minutes) + "m";
    }

    private static MutableComponent createBoolRow(String label, boolean value) {
        return Component.literal("  " + label + ": ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(value ? "ON" : "OFF")
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
            .append(Component.literal("  "))
            .append(createSuggestButton("[Toggle]", "/customdaynight togglePhaseLogging",
                "Turn console messages for day/night transitions " + (value ? "OFF" : "ON"), ChatFormatting.YELLOW));
    }

    // ── Set Commands (update value, then re-display menu) ─────────────

    private static int setDurationMinutes(CommandSourceStack source, String phase, float minutes) {
        int ticks = Math.round(minutes * 60f * TICKS_PER_SECOND);
        int clamped = ModConfig.clampAbsoluteLength(ticks);
        String label;
        if ("day".equals(phase)) {
            ModConfig.absoluteDayLength = clamped;
            label = "Day length";
        } else {
            ModConfig.absoluteNightLength = clamped;
            label = "Night length";
        }
        final int appliedTicks = clamped;
        final boolean wasClamped = clamped != ticks;
        source.sendSuccess(() -> {
            String appliedText = (appliedTicks == 0)
                ? "OFF (using multiplier)"
                : formatDuration(appliedTicks) + "  (" + appliedTicks + " ticks)";
            String suffix = wasClamped
                ? "  (clamped from " + formatMinutes(minutes) + "; allowed: 0 or "
                    + ModConfig.MIN_ABSOLUTE_LENGTH + "\u2013" + ModConfig.MAX_ABSOLUTE_LENGTH + " ticks)"
                : "";
            ChatFormatting color = wasClamped ? ChatFormatting.YELLOW : ChatFormatting.GREEN;
            return Component.literal(CustomDayNightMod.LOG_PREFIX + " " + label + " set to "
                + appliedText + suffix).withStyle(color);
        }, true);
        return showMenu(source);
    }

    private static int setDayMultiplier(CommandSourceStack source, float value) {
        float clamped = ModConfig.clampMultiplier(value);
        ModConfig.dayMultiplier = clamped;
        source.sendSuccess(() -> multiplierFeedback("Day speed", value, clamped), true);
        return showMenu(source);
    }

    private static int setNightMultiplier(CommandSourceStack source, float value) {
        float clamped = ModConfig.clampMultiplier(value);
        ModConfig.nightMultiplier = clamped;
        source.sendSuccess(() -> multiplierFeedback("Night speed", value, clamped), true);
        return showMenu(source);
    }

    private static int setAbsoluteDayLength(CommandSourceStack source, int value) {
        int clamped = ModConfig.clampAbsoluteLength(value);
        ModConfig.absoluteDayLength = clamped;
        source.sendSuccess(() -> lengthFeedback("Day length", value, clamped), true);
        return showMenu(source);
    }

    private static int setAbsoluteNightLength(CommandSourceStack source, int value) {
        int clamped = ModConfig.clampAbsoluteLength(value);
        ModConfig.absoluteNightLength = clamped;
        source.sendSuccess(() -> lengthFeedback("Night length", value, clamped), true);
        return showMenu(source);
    }

    private static MutableComponent multiplierFeedback(String label, float requested, float applied) {
        String suffix = (requested == applied)
            ? ""
            : "  (clamped from " + String.format("%.2f", requested) + " to stay within "
                + ModConfig.MIN_MULTIPLIER + "\u2013" + ModConfig.MAX_MULTIPLIER + ")";
        ChatFormatting color = (requested == applied) ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
        return Component.literal(CustomDayNightMod.LOG_PREFIX + " " + label + " set to "
            + String.format("%.2f", applied) + suffix).withStyle(color);
    }

    private static MutableComponent lengthFeedback(String label, int requested, int applied) {
        String appliedText = (applied == 0) ? "OFF (using multiplier)" : applied + " ticks (" + formatDuration(applied) + ")";
        String suffix = (requested == applied)
            ? ""
            : "  (clamped from " + requested + "; allowed: 0 or "
                + ModConfig.MIN_ABSOLUTE_LENGTH + "\u2013" + ModConfig.MAX_ABSOLUTE_LENGTH + " ticks)";
        ChatFormatting color = (requested == applied) ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
        return Component.literal(CustomDayNightMod.LOG_PREFIX + " " + label + " set to "
            + appliedText + suffix).withStyle(color);
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
