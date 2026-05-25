package com.github.senkex.centermessage.internal;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

/**
 * Direct (non-reflective) hook into PlaceholderAPI.
 *
 * <p>This class is kept in its own compilation unit on purpose. The JVM only
 * loads it the first time one of its methods is called, so on servers without
 * PlaceholderAPI the {@code me.clip.placeholderapi.*} classes are never
 * referenced and the absence is harmless.</p>
 *
 * <p>Callers are expected to gate every call with
 * {@code Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")} so this
 * class is only touched when PAPI is actually present.</p>
 */
public final class PlaceholderHook {

    private PlaceholderHook() {}

    /**
     * Expands PAPI placeholders against the given target.
     *
     * @param target the placeholder target
     * @param text   the text to expand
     * @return the expanded text
     */
    public static String apply(final OfflinePlayer target, final String text) {
        return PlaceholderAPI.setPlaceholders(target, text);
    }
}
