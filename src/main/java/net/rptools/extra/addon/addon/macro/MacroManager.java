package net.rptools.extra.addon.addon.macro;

public class MacroManager {
    public enum Scope {
        /** Not tied to the campaign and persists between campaigns. */
        CLIENT,
        /** Tied to the campaign and does not persist between campaign loads. */
        CAMPAIGN,
        /** Tied to an addon. */
        ADDON
    }
}
