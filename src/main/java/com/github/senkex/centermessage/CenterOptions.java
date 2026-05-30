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

    /**
     * @return the half-width in pixels used as the centering target
     */
    public int centerPx() {
        return centerPx;
    }

    /**
     * @return {@code true} if legacy {@code &}-codes should be translated
     */
    public boolean parseLegacyAmp() {
        return parseLegacyAmp;
    }

    /**
     * @return {@code true} if {@code &#RRGGBB} hex tokens should be translated
     */
    public boolean parseHexAmp() {
        return parseHexAmp;
    }

    /**
     * @return {@code true} if MiniMessage tags should be parsed
     */
    public boolean parseMiniMessage() {
        return parseMiniMessage;
    }

    /**
     * @return {@code true} if PlaceholderAPI placeholders should be expanded
     *         when a target is provided
     */
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

    /**
     * Creates a new builder defaulting to chat-width and every parser enabled.
     *
     * @return an empty builder
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link CenterOptions}.
     */
    public static final class Builder {
        private int centerPx = 154;
        private boolean legacy = true;
        private boolean hex = true;
        private boolean mini = true;
        private boolean papi = true;

        /**
         * Sets the half-width in pixels used as the centering target.
         *
         * @param px the half-width in pixels
         * @return this builder
         */
        public Builder centerPx(final int px) {
            this.centerPx = px;
            return this;
        }

        /**
         * Enables or disables legacy {@code &}-code parsing.
         *
         * @param v whether legacy {@code &}-codes should be translated
         * @return this builder
         */
        public Builder parseLegacyAmp(final boolean v) {
            this.legacy = v;
            return this;
        }

        /**
         * Enables or disables {@code &#RRGGBB} hex parsing.
         *
         * @param v whether hex tokens should be translated
         * @return this builder
         */
        public Builder parseHexAmp(final boolean v) {
            this.hex = v;
            return this;
        }

        /**
         * Enables or disables MiniMessage parsing.
         *
         * @param v whether MiniMessage tags should be parsed
         * @return this builder
         */
        public Builder parseMiniMessage(final boolean v) {
            this.mini = v;
            return this;
        }

        /**
         * Enables or disables PlaceholderAPI expansion.
         *
         * @param v whether placeholders should be expanded
         * @return this builder
         */
        public Builder parsePlaceholders(final boolean v) {
            this.papi = v;
            return this;
        }

        /**
         * Builds an immutable {@link CenterOptions} from this builder's state.
         *
         * @return the configured options
         */
        @Contract(value = " -> new", pure = true)
        public @NotNull CenterOptions build() {
            return new CenterOptions(centerPx, legacy, hex, mini, papi);
        }
    }
}
