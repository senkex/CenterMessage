package com.github.senkex.centermessage;

import com.github.senkex.centermessage.internal.LegacyToMini;
import com.github.senkex.centermessage.internal.PlaceholderHook;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static facade and main entry point for the CenterMessage library.
 *
 * <p>Built on top of Adventure / MiniMessage. The pipeline first unifies every
 * supported color format into MiniMessage and then deserializes it once, so
 * mixing {@code &a}, {@code &#FF55AA}, {@code §c}, {@code <bold>} and
 * {@code <gradient:...>} in the same string is fully supported.</p>
 *
 * <p><b>Minimum Minecraft version:</b> 1.16. Hex colors require 1.16+ on the
 * client; Adventure / MiniMessage are required at runtime (Paper ships them
 * embedded, Spigot users should shade {@code adventure-platform-bukkit}).</p>
 *
 * <p><b>Quick start:</b></p>
 * <pre>{@code
 * CenterMessage.send(player, "&aHello &b<bold>World</bold>");
 *
 * String line = CenterMessage.center("&#FF55AA Centered &ltext");
 * player.sendMessage(line);
 *
 * Component comp = CenterMessage.centerComponent("<gradient:#f00:#0f0>Hi</gradient>");
 * player.sendMessage(comp); // Paper / Adventure-native sender
 * }</pre>
 *
 * <p><b>Optional integration:</b> PlaceholderAPI. Methods that take a
 * {@link Player} / {@link OfflinePlayer} expand {@code %placeholders%}
 * automatically when PAPI is installed.</p>
 *
 * <p>Developed by <b>Senkex</b></p>
 */
public final class CenterMessage {

    /**
     * Default chat half-width in pixels (320 px chat / 2).
     */
    public static final int CHAT_CENTER_PX = 154;

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final char SECTION = '§';
    private static final Pattern AMP_HEX = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern CENTER_TAG = Pattern.compile("(?is)<center>(.*?)</center>");

    private CenterMessage() {
        throw new UnsupportedOperationException("Facade class");
    }

    /**
     * Centers the message using {@link CenterOptions#CHAT} and no PlaceholderAPI target.
     *
     * @param message the raw message, may mix legacy, hex and MiniMessage
     * @return the centered legacy string
     * @see #center(String, CenterOptions, OfflinePlayer)
     */
    public static String center(final String message) {
        return center(message, CenterOptions.CHAT, null);
    }

    /**
     * Centers the message using a custom half-width in pixels.
     *
     * @param message  the raw message, may mix legacy, hex and MiniMessage
     * @param centerPx the half-width in pixels to center around
     * @return the centered legacy string
     * @see #center(String, CenterOptions, OfflinePlayer)
     */
    public static String center(final String message, final int centerPx) {
        return center(message, CenterOptions.builder().centerPx(centerPx).build(), null);
    }

    /**
     * Centers the message using the provided options and returns it as a
     * legacy {@code §}-formatted string.
     *
     * @param message    the raw message, may mix legacy, hex and MiniMessage
     * @param options    the centering configuration
     * @param papiTarget the PlaceholderAPI target, or {@code null} to skip
     * @return the centered legacy string
     */
    public static String center(final String message, final CenterOptions options, final OfflinePlayer papiTarget) {
        if (message == null || message.isEmpty()) return "";
        return LEGACY.serialize(centerComponent(message, options, papiTarget));
    }

    /**
     * Centers the message using {@link CenterOptions#CHAT} and no PlaceholderAPI target.
     *
     * @param message the raw message
     * @return the centered component
     * @see #centerComponent(String, CenterOptions, OfflinePlayer)
     */
    public static Component centerComponent(final String message) {
        return centerComponent(message, CenterOptions.CHAT, null);
    }

    /**
     * Centers the message using the given options and no PlaceholderAPI target.
     *
     * @param message the raw message
     * @param options the centering configuration
     * @return the centered component
     * @see #centerComponent(String, CenterOptions, OfflinePlayer)
     */
    public static Component centerComponent(final String message, final CenterOptions options) {
        return centerComponent(message, options, null);
    }

    /**
     * Centers the message and returns it as an Adventure {@link Component},
     * ready to be sent through an {@link Audience}.
     *
     * @param message    the raw message
     * @param options    the centering configuration
     * @param papiTarget the PlaceholderAPI target, or {@code null} to skip
     * @return the centered component
     */
    public static Component centerComponent(final String message, final CenterOptions options, final OfflinePlayer papiTarget) {
        if (message == null || message.isEmpty()) return Component.empty();
        return centerComponent(parse(message, options, papiTarget), options);
    }

    /**
     * Centers an existing {@link Component} using {@link CenterOptions#CHAT}.
     *
     * @param component the component to center
     * @return the centered component
     */
    public static Component centerComponent(final Component component) {
        return centerComponent(component, CenterOptions.CHAT);
    }

    /**
     * Centers an existing {@link Component}.
     *
     * @param component the component to center
     * @param options   the centering configuration
     * @return the centered component
     */
    public static Component centerComponent(final Component component, final CenterOptions options) {
        if (component == null) return Component.empty();
        final int width = TextMeasurer.measure(LEGACY.serialize(component));
        final int spaces = spacesFor(width, options.centerPx());
        if (spaces <= 0) return component;
        return Component.text(repeat(spaces)).append(component);
    }

    /**
     * Centers every line independently. Lines are split on {@code \r?\n}.
     *
     * @param message the multi-line raw message
     * @return the message with every line centered
     */
    public static String centerLines(final String message) {
        return centerLines(message, CenterOptions.CHAT, null);
    }

    /**
     * Centers every line independently using the given options.
     *
     * @param message    the multi-line raw message
     * @param options    the centering configuration
     * @param papiTarget the PlaceholderAPI target, or {@code null} to skip
     * @return the message with every line centered
     * @see #centerLines(String)
     */
    public static String centerLines(final String message, final CenterOptions options, final OfflinePlayer papiTarget) {
        if (message == null || message.isEmpty()) return "";
        final String[] lines = message.split("\\r?\\n", -1);
        final StringBuilder out = new StringBuilder(message.length() + 32);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) out.append('\n');
            out.append(center(lines[i], options, papiTarget));
        }
        return out.toString();
    }

    /**
     * Replaces every {@code <center>...</center>} block with its centered version.
     *
     * @param message the raw message containing zero or more {@code <center>} blocks
     * @return the message with every block centered
     */
    public static String processCenterTags(final String message) {
        return processCenterTags(message, CenterOptions.CHAT, null);
    }

    /**
     * Replaces every {@code <center>...</center>} block with its centered version
     * using the given options.
     *
     * @param message    the raw message containing zero or more {@code <center>} blocks
     * @param options    the centering configuration
     * @param papiTarget the PlaceholderAPI target, or {@code null} to skip
     * @return the message with every block centered
     * @see #processCenterTags(String)
     */
    public static String processCenterTags(final String message, final CenterOptions options, final OfflinePlayer papiTarget) {
        if (message == null || message.isEmpty()) return "";
        final Matcher m = CENTER_TAG.matcher(message);
        final StringBuffer out = new StringBuffer(message.length() + 16);
        while (m.find()) {
            final String centered = centerLines(m.group(1), options, papiTarget);
            m.appendReplacement(out, Matcher.quoteReplacement(centered));
        }
        m.appendTail(out);
        return out.toString();
    }

    /**
     * Sends a centered message to the given recipient.
     * Uses Adventure when the sender supports it.
     *
     * @param sender  the recipient (player, console, etc.)
     * @param message the raw message
     */
    public static void send(final CommandSender sender, final String message) {
        send(sender, message, CenterOptions.CHAT);
    }

    /**
     * Sends a centered message to the given recipient using the given options.
     *
     * @param sender  the recipient (player, console, etc.)
     * @param message the raw message
     * @param options the centering configuration
     * @see #send(CommandSender, String)
     */
    public static void send(final CommandSender sender, final String message, final CenterOptions options) {
        final OfflinePlayer target = (sender instanceof OfflinePlayer) ? (OfflinePlayer) sender : null;
        if (sender instanceof Audience) {
            ((Audience) sender).sendMessage(centerComponent(message, options, target));
        } else {
            sender.sendMessage(center(message, options, target));
        }
    }

    /**
     * Sends a pre-built component, centered, to a sender.
     *
     * @param sender    the recipient
     * @param component the component to center and send
     */
    public static void send(final CommandSender sender, final Component component) {
        if (sender instanceof Audience) {
            ((Audience) sender).sendMessage(centerComponent(component));
        } else {
            sender.sendMessage(LEGACY.serialize(centerComponent(component)));
        }
    }

    /**
     * Sends a centered string to an Adventure audience.
     *
     * @param audience the audience to send to
     * @param message  the raw message
     */
    public static void send(final Audience audience, final String message) {
        audience.sendMessage(centerComponent(message));
    }

    /**
     * Sends a multi-line message, honoring {@code <center>...</center>} blocks.
     *
     * @param sender  the recipient
     * @param message the raw multi-line message
     */
    public static void sendBlock(final CommandSender sender, final String message) {
        sendBlock(sender, message, CenterOptions.CHAT);
    }

    /**
     * Sends a multi-line message honoring {@code <center>...</center>} blocks,
     * using the given options.
     *
     * @param sender  the recipient
     * @param message the raw multi-line message
     * @param options the centering configuration
     * @see #sendBlock(CommandSender, String)
     */
    public static void sendBlock(final CommandSender sender, final String message, final CenterOptions options) {
        if (message == null) return;
        final OfflinePlayer target = (sender instanceof OfflinePlayer) ? (OfflinePlayer) sender : null;
        final String processed = processCenterTags(message, options, target);
        for (String line : processed.split("\\r?\\n", -1)) {
            sender.sendMessage(line);
        }
    }

    /**
     * Broadcasts a centered message to every online player.
     *
     * @param message the raw message
     */
    public static void broadcast(final String message) {
        broadcast(message, CenterOptions.CHAT);
    }

    /**
     * Broadcasts a centered message to every online player using the given options.
     *
     * @param message the raw message
     * @param options the centering configuration
     * @see #broadcast(String)
     */
    public static void broadcast(final String message, final CenterOptions options) {
        if (message == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p instanceof Audience) {
                ((Audience) p).sendMessage(centerComponent(message, options, p));
            } else {
                p.sendMessage(center(message, options, p));
            }
        }
    }

    /**
     * Translates every supported color format to a legacy {@code §} string.
     * Drop-in replacement for {@link ChatColor#translateAlternateColorCodes(char, String)}
     * that also understands {@code &#RRGGBB} and MiniMessage tags.
     *
     * @param message the raw message
     * @return the legacy {@code §}-formatted string
     */
    public static String colorize(final String message) {
        if (message == null || message.isEmpty()) return "";
        return LEGACY.serialize(parse(message, CenterOptions.CHAT, null));
    }

    /**
     * Parses any supported color format into a {@link Component} using
     * {@link CenterOptions#CHAT} and no PlaceholderAPI target.
     *
     * @param message the raw message
     * @return the parsed component
     */
    public static Component parse(final String message) {
        return parse(message, CenterOptions.CHAT, null);
    }

    /**
     * Parses any supported color format into a {@link Component} using the
     * given options and (optionally) expanding PlaceholderAPI placeholders.
     *
     * @param message    the raw message
     * @param options    the parsing configuration
     * @param papiTarget the PlaceholderAPI target, or {@code null} to skip
     * @return the parsed component
     */
    public static Component parse(final String message, final CenterOptions options, final OfflinePlayer papiTarget) {
        if (message == null || message.isEmpty()) return Component.empty();

        String s = message;

        if (options.parsePlaceholders() && papiTarget != null
                && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            s = PlaceholderHook.apply(papiTarget, s);
        }
        if (options.parseHexAmp()) {
            s = translateAmpHex(s);
        }
        if (options.parseLegacyAmp()) {
            s = ChatColor.translateAlternateColorCodes('&', s);
        }
        // Unify legacy § codes into MiniMessage tags so a single parser handles everything.
        s = LegacyToMini.convert(s);
        if (options.parseMiniMessage()) {
            return MINI.deserialize(s);
        }
        return Component.text(s);
    }

    /**
     * Converts {@code &#RRGGBB} to the Spigot {@code §x§R§R§G§G§B§B} form.
     *
     * @param input the message possibly containing {@code &#RRGGBB} tokens
     * @return the message with hex tokens expanded to Spigot's legacy form
     */
    public static String translateAmpHex(final String input) {
        final Matcher m = AMP_HEX.matcher(input);
        if (!m.find()) return input;
        final StringBuffer sb = new StringBuffer(input.length() + 16);
        m.reset();
        while (m.find()) {
            final String hex = m.group(1);
            final StringBuilder rep = new StringBuilder(14);
            rep.append(SECTION).append('x');
            for (int i = 0; i < 6; i++) {
                rep.append(SECTION).append(hex.charAt(i));
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(rep.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static int spacesFor(final int width, final int centerPx) {
        final int toCompensate = centerPx - (width / 2);
        if (toCompensate <= 0) return 0;
        final int step = FontInfo.SPACE_WIDTH + FontInfo.CHAR_SPACING;
        return toCompensate / step;
    }

    private static String repeat(final int n) {
        final StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(' ');
        return sb.toString();
    }
}
