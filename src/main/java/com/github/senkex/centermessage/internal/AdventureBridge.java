package com.github.senkex.centermessage.internal;

import java.lang.reflect.Method;

/**
 * Optional reflective bridge to Adventure / MiniMessage.
 *
 * <p>When both {@code MiniMessage} and the legacy section serializer are
 * present on the classpath, MiniMessage tags can be converted to the legacy
 * {@code §} format so they render on every server, including 1.7 - 1.15
 * implementations that don't ship Adventure.</p>
 *
 * <p>The legacy serializer is built with {@code .hexColors()} enabled, so
 * gradients and {@code <#RRGGBB>} tags are preserved as Spigot's
 * {@code §x§R§R§G§G§B§B} sequence. Without that flag the colors get
 * silently downgraded to the closest named color.</p>
 */
public final class AdventureBridge {

    private static final boolean MINI_AVAILABLE;
    private static final boolean LEGACY_AVAILABLE;
    private static final Object MINI_INSTANCE;
    private static final Method MINI_DESERIALIZE;
    private static final Object LEGACY_INSTANCE;
    private static final Method LEGACY_SERIALIZE;
    private static final String INIT_ERROR;

    static {
        Object miniInstance = null;
        Method miniDeserialize = null;
        boolean miniOk = false;
        String error = null;
        try {
            final Class<?> mini = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            miniInstance = mini.getMethod("miniMessage").invoke(null);
            // MiniMessage is an interface that extends ComponentDecoder<String, Component>.
            // After erasure, the interface declares `Object deserialize(Object)`. Bridge methods
            // for the concrete signature only live on the implementing class, so we look up
            // the method on the instance class, not on the interface.
            miniDeserialize = miniInstance.getClass().getMethod("deserialize", String.class);
            miniOk = true;
        } catch (Throwable t) {
            error = "MiniMessage: " + t.getClass().getSimpleName() + " " + t.getMessage();
        }

        Object legacyInstance = null;
        Method legacySerialize = null;
        boolean legacyOk = false;
        try {
            final Class<?> legacyCls = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            final Class<?> componentCls = Class.forName("net.kyori.adventure.text.Component");

            Object builder = legacyCls.getMethod("builder").invoke(null);
            final Class<?> builderCls = builder.getClass();

            builder = invokeOptional(builder, builderCls, "character", new Class<?>[]{char.class}, new Object[]{'§'});
            builder = invokeOptional(builder, builderCls, "hexColors", new Class<?>[0], new Object[0]);
            builder = invokeOptional(builder, builderCls, "useUnusualXRepeatedCharacterHexFormat", new Class<?>[0], new Object[0]);

            legacyInstance = builder.getClass().getMethod("build").invoke(builder);
            legacySerialize = legacyCls.getMethod("serialize", componentCls);
            legacyOk = true;
        } catch (Throwable t) {
            if (error == null) {
                error = "LegacySerializer: " + t.getClass().getSimpleName() + " " + t.getMessage();
            }
        }

        MINI_INSTANCE = miniInstance;
        MINI_DESERIALIZE = miniDeserialize;
        LEGACY_INSTANCE = legacyInstance;
        LEGACY_SERIALIZE = legacySerialize;
        MINI_AVAILABLE = miniOk;
        LEGACY_AVAILABLE = legacyOk;
        INIT_ERROR = error;
    }

    private static Object invokeOptional(final Object receiver, final Class<?> cls, final String name,
                                         final Class<?>[] sig, final Object[] args) {
        try {
            for (Method m : cls.getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == sig.length) {
                    return m.invoke(receiver, args);
                }
            }
        } catch (Throwable ignored) {
            // method not available on this Adventure version
        }
        return receiver;
    }

    private AdventureBridge() {}

    /** @return {@code true} if both MiniMessage and the legacy serializer are available */
    public static boolean isFullyAvailable() {
        return MINI_AVAILABLE && LEGACY_AVAILABLE;
    }

    /**
     * @return the first error captured during bridge initialization, or
     *         {@code null} if everything loaded cleanly. Useful for plugin
     *         diagnostics.
     */
    public static String initError() {
        return INIT_ERROR;
    }

    /**
     * Converts a MiniMessage string to legacy {@code §} format. Hex colors
     * coming from {@code <gradient>}, {@code <rainbow>} or {@code <#RRGGBB>}
     * tags are preserved as {@code §x§R§R§G§G§B§B} sequences.
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
