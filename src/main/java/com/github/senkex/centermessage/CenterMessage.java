package com.github.senkex.centermessage;

import com.github.senkex.centermessage.internal.AdventureBridge;
import com.github.senkex.centermessage.internal.PlaceholderBridge;
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
 * <p>All methods are stateless. Pixel width is measured against the vanilla
 * Minecraft font, ignoring every color or formatting token: legacy
 * {@code &x} / {@code §x}, BungeeCord hex {@code &#RRGGBB}, Spigot hex
 * {@code §x§R§R§G§G§B§B} and MiniMessage tags such as {@code <color>},
 * {@code <gradient>}, {@code <bold>} or {@code <reset>}. Only visible
 * glyphs contribute to the leading padding.</p>
 *
 * <p><b>Minimum Minecraft version:</b> 1.7. Hex colors only render on
 * 1.16+ clients; on older versions the codes are stripped from the width
 * calculation but the spacing stays correct.</p>
 *
 * <p><b>Quick start:</b></p>
 * <pre>{@code
 * CenterMessage.send(player, "&aHello &b<bold>World</bold>");
 *
 * String line = CenterMessage.center("&#FF55AA Centered &ltext");
 *
 * CenterMessage.sendBlock(player,
 *     "<center>&6Header</center>\n" +
 *     "Not centered\n" +
 *     "<center>&7Footer</center>");
 * }</pre>
 *
 * <p><b>Optional integrations</b> (detected at runtime, no hard dependency):</p>
 * <ul>
 *   <li><b>PlaceholderAPI</b> — methods that take a {@link Player} or
 *       {@link OfflinePlayer} expand {@code %placeholders%} automatically.</li>
 *   <li><b>Adventure / MiniMessage</b> — when present in the classpath,
 *       MiniMessage tags are converted to legacy {@code §} before sending.</li>
 * </ul>
 *
 * <p>Developed by <b>Senkex</b></p>
 */
public final class CenterMessage {

    /** Default chat half-width in pixels (320 px chat / 2). */
    public static final int CHAT_CENTER_PX = 154;

    private static final char SECTION = '§';
    private static final Pattern AMP_HEX = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern CENTER_TAG = Pattern.compile("(?is)<center>(.*?)</center>");

    private CenterMessage() {
        throw new UnsupportedOperationException("Facade class");
    }

    /**
     * Centers the given message using {@link CenterOptions#CHAT}.
     *
     * @param message the raw message, may contain any supported color format
     * @return the centered message, or an empty string if the input was {@code null}
     */
    public static String center(final String message) {
        return center(message, CenterOptions.CHAT, null);
    }

    /**
     * Centers the given message using a custom half-width in pixels.
     *
     * @param message  the raw message
     * @param centerPx the half-width to center against (chat = 154)
     * @return the centered message
     */
    public static String center(final String message, final int centerPx) {
        return center(message, CenterOptions.builder().centerPx(centerPx).build(), null);
    }

    /**
     * Centers the given message using the provided options.
     *
     * <p>If {@code papiTarget} is not {@code null} and PlaceholderAPI is
     * installed on the server, placeholders are expanded against that
     * target before the width is measured.</p>
     *
     * @param message    the raw message
     * @param options    the centering configuration
     * @param papiTarget the placeholder target, or {@code null} to skip expansion
     * @return the centered message
     */
    public static String center(final String message, final CenterOptions options, final OfflinePlayer papiTarget) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        final String processed = prepare(message, options, papiTarget);
        final int width = TextMeasurer.measure(processed);
        final int toCompensate = options.centerPx() - (width / 2);
        if (toCompensate <= 0) {
            return processed;
        }

        final int spaceStep = FontInfo.SPACE_WIDTH + FontInfo.CHAR_SPACING;
        final int spaces = toCompensate / spaceStep;
        if (spaces <= 0) {
            return processed;
        }

        final StringBuilder sb = new StringBuilder(processed.length() + spaces);
        for (int i = 0; i < spaces; i++) {
            sb.append(' ');
        }
        return sb.append(processed).toString();
    }

    /**
     * Centers every line of the message independently. Lines are split on
     * {@code \n} and {@code \r\n}.
     *
     * @param message the raw message
     * @return the centered, newline-joined result
     */
    public static String centerLines(final String message) {
        return centerLines(message, CenterOptions.CHAT, null);
    }

    /**
     * Centers every line of the message independently using the given options.
     *
     * @param message    the raw message
     * @param options    the centering configuration
     * @param papiTarget the placeholder target, or {@code null} to skip expansion
     * @return the centered, newline-joined result
     */
    public static String centerLines(final String message, final CenterOptions options, final OfflinePlayer papiTarget) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        final String[] lines = message.split("\\r?\\n", -1);
        final StringBuilder out = new StringBuilder(message.length() + 32);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) out.append('\n');
            out.append(center(lines[i], options, papiTarget));
        }
        return out.toString();
    }

    /**
     * Replaces every {@code <center>...</center>} block with its centered
     * version. Content outside of a block is left untouched.
     *
     * @param message the raw message
     * @return the rewritten message
     */
    public static String processCenterTags(final String message) {
        return processCenterTags(message, CenterOptions.CHAT, null);
    }

    /**
     * Replaces every {@code <center>...</center>} block with its centered
     * version using the given options.
     *
     * @param message    the raw message
     * @param options    the centering configuration
     * @param papiTarget the placeholder target, or {@code null} to skip expansion
     * @return the rewritten message
     */
    public static String processCenterTags(final String message, final CenterOptions options, final OfflinePlayer papiTarget) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        final Matcher m = CENTER_TAG.matcher(message);
        final StringBuffer out = new StringBuffer(message.length() + 16);
        while (m.find()) {
            final String inner = m.group(1);
            final String centered = centerLines(inner, options, papiTarget);
            m.appendReplacement(out, Matcher.quoteReplacement(centered));
        }
        m.appendTail(out);
        return out.toString();
    }

    /**
     * Sends a centered message to the given recipient using
     * {@link CenterOptions#CHAT}.
     *
     * @param sender  the recipient
     * @param message the raw message
     */
    public static void send(final CommandSender sender, final String message) {
        send(sender, message, CenterOptions.CHAT);
    }

    /**
     * Sends a centered message to the given recipient using the provided options.
     *
     * @param sender  the recipient
     * @param message the raw message
     * @param options the centering configuration
     */
    public static void send(final CommandSender sender, final String message, final CenterOptions options) {
        final OfflinePlayer target = (sender instanceof OfflinePlayer) ? (OfflinePlayer) sender : null;
        sender.sendMessage(center(message, options, target));
    }

    /**
     * Sends a multi-line message, honoring {@code <center>...</center>} blocks.
     * Lines outside of a block are sent verbatim.
     *
     * @param sender  the recipient
     * @param message the raw message
     */
    public static void sendBlock(final CommandSender sender, final String message) {
        sendBlock(sender, message, CenterOptions.CHAT);
    }

    /**
     * Sends a multi-line message, honoring {@code <center>...</center>} blocks,
     * using the given options.
     *
     * @param sender  the recipient
     * @param message the raw message
     * @param options the centering configuration
     */
    public static void sendBlock(final CommandSender sender, final String message, final CenterOptions options) {
        if (message == null) {
            return;
        }
        final OfflinePlayer target = (sender instanceof OfflinePlayer) ? (OfflinePlayer) sender : null;
        final String processed = processCenterTags(message, options, target);
        for (String line : processed.split("\\r?\\n", -1)) {
            sender.sendMessage(line);
        }
    }

    /**
     * Sends a centered message to every online player.
     *
     * @param message the raw message
     */
    public static void broadcast(final String message) {
        broadcast(message, CenterOptions.CHAT);
    }

    /**
     * Sends a centered message to every online player using the given options.
     *
     * @param message the raw message
     * @param options the centering configuration
     */
    public static void broadcast(final String message, final CenterOptions options) {
        if (message == null) {
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(center(message, options, p));
        }
    }

    /**
     * Translates every supported color format to legacy {@code §}.
     *
     * <p>Drop-in replacement for
     * {@link ChatColor#translateAlternateColorCodes(char, String)} that also
     * understands {@code &#RRGGBB} and, when Adventure is on the classpath,
     * MiniMessage tags.</p>
     *
     * @param message the raw message
     * @return the translated message
     */
    public static String colorize(final String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return prepare(message, CenterOptions.CHAT, null);
    }

    /**
     * Converts BungeeCord-style {@code &#RRGGBB} sequences to the Spigot
     * {@code §x§R§R§G§G§B§B} format. Other tokens are left untouched.
     *
     * @param input the raw message
     * @return the converted message
     */
    public static String translateAmpHex(final String input) {
        final Matcher m = AMP_HEX.matcher(input);
        if (!m.find()) {
            return input;
        }
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

    private static String prepare(final String message, final CenterOptions options, final OfflinePlayer papiTarget) {
        String s = message;

        if (options.parsePlaceholders() && papiTarget != null && PlaceholderBridge.isAvailable()) {
            s = PlaceholderBridge.apply(papiTarget, s);
        }
        if (options.parseMiniMessage() && hasMiniTag(s) && AdventureBridge.isFullyAvailable()) {
            s = AdventureBridge.miniToLegacy(s);
        }
        if (options.parseHexAmp()) {
            s = translateAmpHex(s);
        }
        if (options.parseLegacyAmp()) {
            s = ChatColor.translateAlternateColorCodes('&', s);
        }
        return s;
    }

    private static boolean hasMiniTag(final String s) {
        final int lt = s.indexOf('<');
        if (lt < 0) {
            return false;
        }
        return s.indexOf('>', lt + 1) > lt;
    }
}
