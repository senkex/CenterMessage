package com.github.senkex.centermessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-character pixel widths for the vanilla Minecraft chat font.
 *
 * <p>Values match the default font shipped with the game and do not
 * include the one-pixel separator between consecutive characters.
 * Custom resource packs can extend the table at runtime through
 * {@link #registerWidth(char, int)}.</p>
 */
public final class FontInfo {

    /** Width of the space character in pixels. */
    public static final int SPACE_WIDTH = 3;

    /** Extra pixel inserted between every pair of characters. */
    public static final int CHAR_SPACING = 1;

    /** Extra width added per character when bold formatting is active. */
    public static final int BOLD_EXTRA = 1;

    private static final Map<Character, Integer> WIDTHS = new HashMap<Character, Integer>(160);

    static {
        register("ABCDEFGHJKLMNOPQRSTUVWXYZ", 5);
        register("I", 3);
        register("abcdeghjmnopqrsuvwxyz", 5);
        register("fk", 4);
        register("i", 1);
        register("l", 1);
        register("t", 4);
        register("0123456789", 5);

        WIDTHS.put('!', 1);
        WIDTHS.put('@', 6);
        WIDTHS.put('#', 5);
        WIDTHS.put('$', 5);
        WIDTHS.put('%', 5);
        WIDTHS.put('^', 5);
        WIDTHS.put('&', 5);
        WIDTHS.put('*', 5);
        WIDTHS.put('(', 4);
        WIDTHS.put(')', 4);
        WIDTHS.put('-', 5);
        WIDTHS.put('_', 5);
        WIDTHS.put('+', 5);
        WIDTHS.put('=', 5);
        WIDTHS.put('{', 4);
        WIDTHS.put('}', 4);
        WIDTHS.put('[', 3);
        WIDTHS.put(']', 3);
        WIDTHS.put(':', 1);
        WIDTHS.put(';', 1);
        WIDTHS.put('"', 3);
        WIDTHS.put('\'', 1);
        WIDTHS.put('<', 4);
        WIDTHS.put('>', 4);
        WIDTHS.put('?', 5);
        WIDTHS.put('/', 5);
        WIDTHS.put('\\', 5);
        WIDTHS.put('|', 1);
        WIDTHS.put('~', 5);
        WIDTHS.put('`', 2);
        WIDTHS.put('.', 1);
        WIDTHS.put(',', 1);
        WIDTHS.put(' ', SPACE_WIDTH);

        // Common unicode glyphs used in chat
        WIDTHS.put('¡', 1);
        WIDTHS.put('¿', 5);
        WIDTHS.put('°', 3);
        WIDTHS.put('•', 3);
        WIDTHS.put('●', 5);
        WIDTHS.put('★', 5);
        WIDTHS.put('☆', 5);
        WIDTHS.put('→', 6);
        WIDTHS.put('←', 6);
        WIDTHS.put('↑', 5);
        WIDTHS.put('↓', 5);
        WIDTHS.put('»', 4);
        WIDTHS.put('«', 4);
        WIDTHS.put('§', 5);
        WIDTHS.put('©', 7);
        WIDTHS.put('®', 7);
        WIDTHS.put('™', 7);
        WIDTHS.put('€', 5);
        WIDTHS.put('£', 5);
        WIDTHS.put('¥', 5);

        register("áéíóúÁÉÍÓÚñÑüÜ", 5);
    }

    private FontInfo() {}

    /**
     * Returns the pixel width of the given character, excluding the
     * inter-character spacing.
     *
     * @param c the character to look up
     * @return the glyph width, defaulting to {@code 5} for unknown characters
     */
    public static int widthOf(final char c) {
        final Integer w = WIDTHS.get(c);
        return w != null ? w : 5;
    }

    /**
     * Returns the pixel width of the given character when bold formatting
     * is active. Spaces are not affected by bold.
     *
     * @param c the character to look up
     * @return the bold glyph width
     */
    public static int boldWidthOf(final char c) {
        if (c == ' ') {
            return SPACE_WIDTH;
        }
        return widthOf(c) + BOLD_EXTRA;
    }

    /**
     * Registers a custom width for a character. Useful when shipping a
     * resource pack that ships a non-default font.
     *
     * @param c     the character to override
     * @param width the new width in pixels
     */
    public static void registerWidth(final char c, final int width) {
        WIDTHS.put(c, width);
    }

    private static void register(final String chars, final int width) {
        for (int i = 0; i < chars.length(); i++) {
            WIDTHS.put(chars.charAt(i), width);
        }
    }
}
