package com.github.senkex.centermessage;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable configuration for the centering pipeline.
 *
 * <p>Use one of the prefabs or build your own with {@link #builder()}.
 * Every flag controls a single stage of the pipeline; disable the ones
 * you don't need to skip the work.</p>
 *
 * <ul>
 *   <li>{@link #CHAT}  — 154 px, the default chat half-width.</li>
 *   <li>{@link #SIGN}  — 45 px, fits a sign line.</li>
 *   <li>{@link #BOOK}  — 57 px, fits a written book page.</li>
 *   <li>{@link #ANVIL} — 50 px, fits the anvil rename field.</li>
 * </ul>
 */
public final class CenterOptions {

    public static final CenterOptions CHAT = new CenterOptions(154, true, true, true, true);
    public static final CenterOptions SIGN = new CenterOptions(45, true, true, true, false);
    public static final CenterOptions BOOK = new CenterOptions(57, true, true, true, true);
    public static final CenterOptions ANVIL = new CenterOptions(50, true, true, true, false);

    private final int centerPx;
    private final boolean parseLegacyAmp;
    private final boolean parseHexAmp;
    private final boolean parseMiniMessage;
    private final boolean parsePlaceholders;

    private CenterOptions(final int centerPx,
                          final boolean legacy,
                          final boolean hex,
                          final boolean mini,
                          final boolean papi) {
        this.centerPx = centerPx;
        this.parseLegacyAmp = legacy;
        this.parseHexAmp = hex;
        this.parseMiniMessage = mini;
        this.parsePlaceholders = papi;
    }

    public int centerPx() {
        return centerPx;
    }

    public boolean parseLegacyAmp() {
        return parseLegacyAmp;
    }

    public boolean parseHexAmp() {
        return parseHexAmp;
    }

    public boolean parseMiniMessage() {
        return parseMiniMessage;
    }

    public boolean parsePlaceholders() {
        return parsePlaceholders;
    }

    /**
     * Returns a new builder pre-populated with this instance's values.
     *
     * @return a configured builder
     */
    public Builder toBuilder() {
        return new Builder()
                .centerPx(centerPx)
                .parseLegacyAmp(parseLegacyAmp)
                .parseHexAmp(parseHexAmp)
                .parseMiniMessage(parseMiniMessage)
                .parsePlaceholders(parsePlaceholders);
    }

    /** @return an empty builder defaulting to chat-width and every parser enabled */
    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /** Builder for {@link CenterOptions}. */
    public static final class Builder {
        private int centerPx = 154;
        private boolean legacy = true;
        private boolean hex = true;
        private boolean mini = true;
        private boolean papi = true;

        public Builder centerPx(final int px) {
            this.centerPx = px;
            return this;
        }

        public Builder parseLegacyAmp(final boolean v) {
            this.legacy = v;
            return this;
        }

        public Builder parseHexAmp(final boolean v) {
            this.hex = v;
            return this;
        }

        public Builder parseMiniMessage(final boolean v) {
            this.mini = v;
            return this;
        }

        public Builder parsePlaceholders(final boolean v) {
            this.papi = v;
            return this;
        }

        @Contract(value = " -> new", pure = true)
        public @NotNull CenterOptions build() {
            return new CenterOptions(centerPx, legacy, hex, mini, papi);
        }
    }
}
