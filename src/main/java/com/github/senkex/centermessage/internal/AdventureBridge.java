package com.github.senkex.centermessage.internal;

import java.lang.reflect.Method;

/**
 * Optional reflective bridge to Adventure / MiniMessage.
 *
 * <p>When both {@code MiniMessage} and the legacy section serializer are
 * present on the classpath, MiniMessage tags can be converted to the legacy
 * {@code §} format so they render on every server, including 1.7 - 1.15
 * implementations that don't ship Adventure.</p>
 */
public final class AdventureBridge {

    private static final boolean MINI_AVAILABLE;
    private static final boolean LEGACY_AVAILABLE;
    private static final Object MINI_INSTANCE;
    private static final Method MINI_DESERIALIZE;
    private static final Object LEGACY_INSTANCE;
    private static final Method LEGACY_SERIALIZE;

    static {
        Object miniInstance = null;
        Method miniDeserialize = null;
        boolean miniOk = false;
        try {
            final Class<?> mini = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            miniInstance = mini.getMethod("miniMessage").invoke(null);
            miniDeserialize = mini.getMethod("deserialize", String.class);
            miniOk = true;
        } catch (Throwable ignored) {
            // MiniMessage not present
        }

        Object legacyInstance = null;
        Method legacySerialize = null;
        boolean legacyOk = false;
        try {
            final Class<?> legacy = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            legacyInstance = legacy.getMethod("legacySection").invoke(null);
            final Class<?> component = Class.forName("net.kyori.adventure.text.Component");
            legacySerialize = legacy.getMethod("serialize", component);
            legacyOk = true;
        } catch (Throwable ignored) {
            // Legacy serializer not present
        }

        MINI_INSTANCE = miniInstance;
        MINI_DESERIALIZE = miniDeserialize;
        LEGACY_INSTANCE = legacyInstance;
        LEGACY_SERIALIZE = legacySerialize;
        MINI_AVAILABLE = miniOk;
        LEGACY_AVAILABLE = legacyOk;
    }

    private AdventureBridge() {}

    /** @return {@code true} if both MiniMessage and the legacy serializer are available */
    public static boolean isFullyAvailable() {
        return MINI_AVAILABLE && LEGACY_AVAILABLE;
    }

    /**
     * Converts a MiniMessage string to legacy {@code §} format.
     *
     * @param input the MiniMessage-formatted string
     * @return the legacy-formatted result, or the input untouched if Adventure
     *         is missing or the call fails
     */
    public static String miniToLegacy(final String input) {
        if (input == null || !isFullyAvailable()) {
            return input;
        }
        try {
            final Object component = MINI_DESERIALIZE.invoke(MINI_INSTANCE, input);
            return (String) LEGACY_SERIALIZE.invoke(LEGACY_INSTANCE, component);
        } catch (Throwable t) {
            return input;
        }
    }
}
