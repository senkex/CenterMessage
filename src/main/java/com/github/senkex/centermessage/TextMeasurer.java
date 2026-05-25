package com.github.senkex.centermessage;

/**
 * Measures the rendered pixel width of a legacy-formatted string, ignoring
 * every {@code §} sequence.
 *
 * <p>The CenterMessage pipeline runs MiniMessage first and converts everything
 * to legacy {@code §} (with hex preserved as {@code §x§R§R§G§G§B§B}) before
 * measuring, so this class only needs to understand the legacy format.</p>
 *
 * <p>Bold formatting is tracked across {@code §l} → next color / {@code §r}
 * pairs so the extra pixel per character is applied where it belongs.</p>
 */
public final class TextMeasurer {

    private TextMeasurer() {}

    /**
     * Returns the pixel width of the message, ignoring every {@code §} sequence.
     *
     * @param input the legacy-formatted string
     * @return the rendered width in pixels, or {@code 0} for {@code null} / empty
     */
    public static int measure(final String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        int px = 0;
        boolean bold = false;

        final int n = input.length();
        int i = 0;
        while (i < n) {
            final char c = input.charAt(i);

            if (c == '§' && i + 1 < n) {
                final char code = Character.toLowerCase(input.charAt(i + 1));
                if (code == 'x' && i + 14 <= n && isSpigotHex(input, i)) {
                    bold = false;
                    i += 14;
                    continue;
                }
                if (isLegacyCode(code)) {
                    if (code == 'l') bold = true;
                    else if (code == 'r' || isColorChar(code)) bold = false;
                    i += 2;
                    continue;
                }
            }

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

    private static boolean isSpigotHex(final String s, final int i) {
        for (int k = 0; k < 6; k++) {
            final int pos = i + 2 + k * 2;
            if (pos + 1 >= s.length()) return false;
            if (s.charAt(pos) != '§') return false;
            if (!isHex(s.charAt(pos + 1))) return false;
        }
        return true;
    }
}
