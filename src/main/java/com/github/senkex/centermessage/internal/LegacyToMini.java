package com.github.senkex.centermessage.internal;

/**
 * Converts legacy {@code §} color codes into their MiniMessage equivalents so
 * the whole input can go through a single MiniMessage parser.
 *
 * <p>Handled tokens:</p>
 * <ul>
 *   <li>{@code §0}-{@code §9}, {@code §a}-{@code §f} → named color tags</li>
 *   <li>{@code §l §m §n §o §k §r} → format / reset tags</li>
 *   <li>{@code §x§R§R§G§G§B§B} → {@code <#RRGGBB>}</li>
 * </ul>
 *
 * <p>Anything else is copied verbatim.</p>
 */
public final class LegacyToMini {

    private LegacyToMini() {}

    public static String convert(final String input) {
        if (input == null || input.isEmpty() || input.indexOf('§') < 0) {
            return input;
        }
        final StringBuilder out = new StringBuilder(input.length() + 16);
        final int n = input.length();
        int i = 0;
        while (i < n) {
            final char c = input.charAt(i);
            if (c == '§' && i + 1 < n) {
                final char raw = input.charAt(i + 1);
                final char code = Character.toLowerCase(raw);

                // §x§R§R§G§G§B§B
                if (code == 'x' && i + 14 <= n && isSpigotHex(input, i)) {
                    out.append("<#");
                    for (int k = 0; k < 6; k++) {
                        out.append(input.charAt(i + 3 + k * 2));
                    }
                    out.append('>');
                    i += 14;
                    continue;
                }

                final String tag = tagFor(code);
                if (tag != null) {
                    out.append('<').append(tag).append('>');
                    i += 2;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    private static String tagFor(final char code) {
        switch (code) {
            case '0': return "black";
            case '1': return "dark_blue";
            case '2': return "dark_green";
            case '3': return "dark_aqua";
            case '4': return "dark_red";
            case '5': return "dark_purple";
            case '6': return "gold";
            case '7': return "gray";
            case '8': return "dark_gray";
            case '9': return "blue";
            case 'a': return "green";
            case 'b': return "aqua";
            case 'c': return "red";
            case 'd': return "light_purple";
            case 'e': return "yellow";
            case 'f': return "white";
            case 'l': return "bold";
            case 'm': return "strikethrough";
            case 'n': return "underlined";
            case 'o': return "italic";
            case 'k': return "obfuscated";
            case 'r': return "reset";
            default:  return null;
        }
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

    private static boolean isHex(final char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}
