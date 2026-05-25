package com.github.senkex.centermessage;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Measures the rendered pixel width of a chat string while ignoring every
 * color or formatting token.
 *
 * <p>Recognized markup that does <b>not</b> contribute to the width:</p>
 * <ul>
 *   <li>Legacy: {@code §x} and {@code &x} (0-9, a-f, k-o, r).</li>
 *   <li>BungeeCord hex: {@code &#RRGGBB} and {@code §#RRGGBB}.</li>
 *   <li>Spigot hex: {@code §x§R§R§G§G§B§B}.</li>
 *   <li>MiniMessage: {@code <color>}, {@code <#RRGGBB>}, {@code <gradient:...>},
 *       {@code <bold>}, {@code <reset>}, etc. Closing tags are honored.</li>
 * </ul>
 *
 * <p>Bold state is tracked across both legacy codes and MiniMessage tags,
 * including nested {@code <bold>} blocks, so the extra pixel per character
 * is applied where it belongs.</p>
 */
public final class TextMeasurer {

    private TextMeasurer() {}

    private static final Set<String> MINI_TAGS = new HashSet<String>(Arrays.asList(
            "color", "c", "colour",
            "gradient", "rainbow", "reset",
            "bold", "b",
            "italic", "i", "em",
            "underlined", "u",
            "strikethrough", "st",
            "obfuscated", "obf",
            "font",
            "hover", "click", "insert",
            "key", "keybind", "lang", "tr", "translate",
            "score", "selector", "nbt",
            "newline", "br",
            "pride", "shadow"
    ));

    private static final Set<String> NAMED_COLORS = new HashSet<String>(Arrays.asList(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "grey", "dark_gray", "dark_grey", "blue", "green", "aqua",
            "red", "light_purple", "yellow", "white"
    ));

    /**
     * Returns the pixel width of the message after stripping every color
     * or formatting token.
     *
     * @param input the raw message
     * @return the rendered width in pixels, or {@code 0} if the input was
     *         {@code null} or empty
     */
    public static int measure(final String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        int px = 0;
        boolean legacyBold = false;
        boolean miniBold = false;
        final Deque<Boolean> miniBoldStack = new ArrayDeque<Boolean>();

        final int n = input.length();
        int i = 0;
        while (i < n) {
            final char c = input.charAt(i);

            // Legacy §x / Spigot §x§R§R§G§G§B§B
            if (c == '§' && i + 1 < n) {
                final char code = Character.toLowerCase(input.charAt(i + 1));
                if (code == 'x' && i + 14 <= n && isSpigotHex(input, i)) {
                    legacyBold = false;
                    i += 14;
                    continue;
                }
                if (isLegacyCode(code)) {
                    if (code == 'l') legacyBold = true;
                    else if (code == 'r' || isColorChar(code)) legacyBold = false;
                    i += 2;
                    continue;
                }
            }

            // Legacy &x / BungeeCord &#RRGGBB
            if (c == '&' && i + 1 < n) {
                final char next = input.charAt(i + 1);
                if (next == '#' && i + 8 <= n && isHexRun(input, i + 2, 6)) {
                    legacyBold = false;
                    i += 8;
                    continue;
                }
                final char code = Character.toLowerCase(next);
                if (isLegacyCode(code)) {
                    if (code == 'l') legacyBold = true;
                    else if (code == 'r' || isColorChar(code)) legacyBold = false;
                    i += 2;
                    continue;
                }
            }

            // MiniMessage <tag>, </tag>
            if (c == '<' && (i == 0 || input.charAt(i - 1) != '\\')) {
                final int close = findTagClose(input, i);
                if (close > i) {
                    final String raw = input.substring(i + 1, close);
                    final String name = tagName(raw);
                    final boolean closing = name.startsWith("/");
                    final String bare = closing ? name.substring(1) : name;
                    if (isRecognizedTag(bare)) {
                        if (bare.equals("bold") || bare.equals("b")) {
                            if (closing) {
                                miniBold = !miniBoldStack.isEmpty() ? miniBoldStack.pop() : false;
                            } else {
                                miniBoldStack.push(miniBold);
                                miniBold = true;
                            }
                        } else if (bare.equals("reset")) {
                            miniBold = false;
                            miniBoldStack.clear();
                            legacyBold = false;
                        }
                        i = close + 1;
                        continue;
                    }
                }
            }

            // Visible glyph
            final boolean bold = legacyBold || miniBold;
            px += (bold ? FontInfo.boldWidthOf(c) : FontInfo.widthOf(c)) + FontInfo.CHAR_SPACING;
            i++;
        }
        return px;
    }

    private static boolean isLegacyCode(final char c) {
        return (c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'f')
                || (c >= 'k' && c <= 'o')
                || c == 'r' || c == 'x';
    }

    private static boolean isColorChar(final char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
    }

    private static boolean isHex(final char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static boolean isHexRun(final String s, final int start, final int len) {
        if (start + len > s.length()) return false;
        for (int k = 0; k < len; k++) {
            if (!isHex(s.charAt(start + k))) return false;
        }
        return true;
    }

    private static boolean isSpigotHex(final String s, final int i) {
        for (int k = 0; k < 6; k++) {
            final int pos = i + 2 + k * 2;
            if (pos + 1 >= s.length()) return false;
            if (s.charAt(pos) != '§') return false;
            if (!isHex(s.charAt(pos + 1))) return false;
        }
        return true;
    }

    private static int findTagClose(final String s, final int open) {
        for (int j = open + 1; j < s.length(); j++) {
            final char ch = s.charAt(j);
            if (ch == '>') return j;
            if (ch == '<') return -1;
        }
        return -1;
    }

    private static String tagName(final String raw) {
        final int colon = raw.indexOf(':');
        final String head = (colon < 0 ? raw : raw.substring(0, colon));
        return head.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isRecognizedTag(final String bare) {
        if (bare.isEmpty()) return false;
        if (bare.charAt(0) == '#' && (bare.length() == 7 || bare.length() == 4)) {
            for (int k = 1; k < bare.length(); k++) {
                if (!isHex(bare.charAt(k))) return false;
            }
            return true;
        }
        return MINI_TAGS.contains(bare) || NAMED_COLORS.contains(bare);
    }
}
