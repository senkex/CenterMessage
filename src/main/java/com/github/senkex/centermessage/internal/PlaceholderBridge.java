package com.github.senkex.centermessage.internal;

import java.lang.reflect.Method;

/**
 * Optional reflective bridge to PlaceholderAPI.
 *
 * <p>The library never declares a hard dependency on PlaceholderAPI; this
 * class probes the classpath on load and silently disables itself when
 * the {@code me.clip.placeholderapi.PlaceholderAPI} class is missing.</p>
 */
public final class PlaceholderBridge {

    private static final boolean AVAILABLE;
    private static final Method SET_PLACEHOLDERS;

    static {
        Method method = null;
        boolean available = false;
        try {
            final Class<?> api = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            final Class<?> offlinePlayer = Class.forName("org.bukkit.OfflinePlayer");
            method = api.getMethod("setPlaceholders", offlinePlayer, String.class);
            available = true;
        } catch (Throwable ignored) {
            // PlaceholderAPI not present
        }
        SET_PLACEHOLDERS = method;
        AVAILABLE = available;
    }

    private PlaceholderBridge() {}

    /** @return {@code true} if PlaceholderAPI was found on the classpath */
    public static boolean isAvailable() {
        return AVAILABLE;
    }

    /**
     * Expands placeholders in the given text against the given target.
     *
     * @param offlinePlayer the placeholder target (an {@code OfflinePlayer})
     * @param text          the text to expand
     * @return the expanded text, or the input untouched if PlaceholderAPI
     *         is missing or the call fails
     */
    public static String apply(final Object offlinePlayer, final String text) {
        if (!AVAILABLE || text == null) {
            return text;
        }
        try {
            return (String) SET_PLACEHOLDERS.invoke(null, offlinePlayer, text);
        } catch (Throwable t) {
            return text;
        }
    }
}
