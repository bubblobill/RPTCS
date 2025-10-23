/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client;

import java.awt.Color;
import java.io.File;
import java.util.prefs.Preferences;

import net.rptools.lib.image.RenderQuality;

import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.preferences.Preference;
import net.rptools.maptool.util.preferences.PreferenceStore;

/**
 * Manages and persists user preferences for the application.
 */
public class AppPreferences {
    private static final PreferenceStore store = null;
    private static final boolean PRINT_KEYS_MISSING_I18N_ON_STARTUP = false; // for finding pesky wabbitses

    public static PreferenceStore getAppPreferenceStore() {
        return store;
    }

    public static final Preference<Boolean> fillSelectionBox = null;

    public static final Preference<Color> chatColor = null;

    public static final Preference<Boolean> saveReminder = null;

    public static final Preference.Numeric<Integer> autoSaveIncrement = null;

    public static final Preference.Numeric<Integer> chatAutoSaveTimeInMinutes = null;

    public static final Preference<String> chatFilenameFormat = null;

    public static final Preference<String> tokenNumberDisplay = null;

    public static final Preference<String> duplicateTokenNumber = null;

    public static final Preference<String> newTokenNaming = null;

    public static final Preference<Boolean> useHaloColorOnVisionOverlay = null;

    public static final Preference<Boolean> mapVisibilityWarning = null;

    public static final Preference<Boolean> autoRevealVisionOnGMMovement = null;

    public static final Preference.Numeric<Integer> haloOverlayOpacity = null;

    public static final Preference.Numeric<Integer> auraOverlayOpacity = null;

    public static final Preference.Numeric<Integer> lightOverlayOpacity = null;

    public static final Preference.Numeric<Integer> lumensOverlayOpacity = null;

    public static final Preference.Numeric<Integer> fogOverlayOpacity = null;

    public static final Preference.Numeric<Integer> lumensOverlayBorderThickness = null;

    public static final Preference<Boolean> lumensOverlayShowByDefault = null;

    public static final Preference<Boolean> lightsShowByDefault = null;

    public static final Preference.Numeric<Integer> haloLineWidth = null;

    public static final Preference.Numeric<Integer> typingNotificationDurationInSeconds = null;

    public static final Preference<Boolean> chatNotificationBackground = null;

    public static final Preference<Boolean> useToolTipForInlineRoll = null;

    public static final Preference<Boolean> suppressToolTipsForMacroLinks = null;

    public static final Preference<Color> chatNotificationColor = null;

    public static final Preference<Color> trustedPrefixBackground = null;

    public static final Preference<Color> trustedPrefixForeground = null;

    public static final Preference.Numeric<Integer> toolTipInitialDelay = null;

    public static final Preference.Numeric<Integer> toolTipDismissDelay = null;

    public static final Preference<Boolean> openEditorForNewMacro = null;

    public static final Preference<Boolean> allowPlayerMacroEditsDefault = null;

    public static final Preference.Numeric<Integer> portraitSize = null;

    public static final Preference.Numeric<Integer> thumbnailSize = null;

    public static final Preference<Boolean> showSmilies = null;

    public static final Preference<Boolean> showDialogOnNewToken = null;

    public static final Preference<Boolean> showAvatarInChat = null;

    public static final Preference<Boolean> playSystemSounds = null;

    public static final Preference<Boolean> playSystemSoundsOnlyWhenNotFocused = null;

    public static final Preference<Boolean> playStreams = null;

    public static final Preference<Boolean> syrinscapeActive = null;

    public static final Preference.Numeric<Integer> fontSize = null;

    public static final Preference<Color> defaultGridColor = null;

    public static final Preference.Numeric<Integer> defaultGridSize = null;

    public static final Preference.Numeric<Double> defaultUnitsPerCell = null;

    public static final Preference<Boolean> faceVertex = null;

    public static final Preference<Boolean> faceEdge = null;

    public static final Preference.Numeric<Integer> defaultVisionDistance = null;

//    public static final Preference<Zone.VisionType> defaultVisionType;
//
//    public static final Preference<MapSortType> mapSortType;
//
//    public static final Preference<UvttLosImportType> uvttLosImportType;

    public static final Preference<Boolean> useSoftFogEdges = null;

    public static final Preference<Boolean> newMapsHaveFow = null;

    public static final Preference<Boolean> newTokensVisible = null;

    public static final Preference<Boolean> newMapsVisible = null;

    public static final Preference<Boolean> newObjectsVisible = null;

    public static final Preference<Boolean> newBackgroundsVisible = null;

    public static final Preference<File> saveDirectory = null;

    public static final Preference<File> tokenSaveDirectory = null;

    public static final Preference<File> mapSaveDirectory = null;

    public static final Preference<File> addOnLoadDirectory = null;

    public static final Preference<File> loadDirectory = null;

    public static final Preference<RenderQuality> renderQuality = null;

    /**
     * The background color to use for NPC map labels.
     */
    public static final Preference<Color> npcMapLabelBackground = null;

    /**
     * The foreground color to use for NPC map labels.
     */
    public static final Preference<Color> npcMapLabelForeground = null;

    /**
     * The border color to use for NPC map labels.
     */
    public static final Preference<Color> npcMapLabelBorder = null;

    /**
     * The background color to use for PC map labels.
     */
    public static final Preference<Color> pcMapLabelBackground = null;

    /**
     * The foreground color to use for PC map labels.
     */
    public static final Preference<Color> pcMapLabelForeground = null;

    /**
     * The border color to use for PC map labels.
     */
    public static final Preference<Color> pcMapLabelBorder = null;

    /**
     * The background color to use for Non-Visible Token map labels.
     */
    public static final Preference<Color> nonVisibleTokenMapLabelBackground = null;

    /**
     * The foreground color to use for Non-Visible Token map labels.
     */
    public static final Preference<Color> nonVisibleTokenMapLabelForeground = null;

    /**
     * The border color to use for Non-Visible Token map labels.
     */
    public static final Preference<Color> nonVisibleTokenMapLabelBorder = null;

    /**
     * The background color to use for drawing labels.
     */
    public static final Preference<Color> drawingMapLabelBackgroundColor = null;

    /**
     * The foreground color to use for drawing labels.
     */
    public static final Preference<Color> drawingMapLabelForegroundColor = null;

    /**
     * The border color to use for drawing labels.
     */
    public static final Preference<Color> drawingMapLabelBorderColor = null;

    /**
     * The background color to use for template labels.
     */
    public static final Preference<Color> templateMapLabelBackgroundColor = null;

    /**
     * The foreground color to use for template labels.
     */
    public static final Preference<Color> templateMapLabelForegroundColor = null;

    /**
     * The border color to use for template labels.
     */
    public static final Preference<Color> templateMapLabelBorderColor = null;

    /**
     * The font size to use for token map labels.
     */
    public static final Preference.Numeric<Integer> mapLabelFontSize = null;

    /**
     * The width of the border for token map labels, in pixels.
     */
    public static final Preference.Numeric<Integer> mapLabelBorderWidth = null;

    /**
     * The size of the border arc for token map labels.
     */
    public static final Preference.Numeric<Integer> mapLabelBorderArc = null;

    /**
     * {@code true} if borders should be shown around map labels, {@code false} otherwise.
     */
    public static final Preference<Boolean> mapLabelShowBorder = null;

    public static final Preference.Numeric<Integer> webEndpointPort = null;

    public static final Preference<Boolean> tokensWarnWhenDeleted = null;

    public static final Preference<Boolean> drawingsWarnWhenDeleted = null;

    public static final Preference<Boolean> tokensSnapWhileDragging = null;

    public static final Preference<Boolean> hideMousePointerWhileDragging = null;

    public static final Preference<Boolean> hideTokenStackIndicator = null;

    public static final Preference<Boolean> tokensStartSnapToGrid = null;

    public static final Preference<Boolean> objectsStartSnapToGrid = null;

    public static final Preference<Boolean> backgroundsStartSnapToGrid = null;

    public static final Preference<Boolean> tokensStartFreesize = null;

    public static final Preference<Boolean> objectsStartFreesize = null;

    public static final Preference<Boolean> backgroundsStartFreesize = null;

    public static final Preference<String> defaultGridType = null;

    public static final Preference<Boolean> showStatSheet = null;

    public static final Preference<Boolean> showStatSheetRequiresModifierKey = null;

    public static final Preference<Boolean> showPortrait = null;
    public static final Preference<Color> facingArrowBGColour = null;

    public static final Preference<Color> facingArrowBorderColour = null;

    public static final Preference<Boolean> forceFacingArrow = null;

    public static final Preference<Boolean> fitGmView = null;

    public static final Preference<String> defaultUserName = null;

//    public static final Preference<WalkerMetric> movementMetric = null;

    public static final Preference.Numeric<Integer> frameRateCap = null;

    /* Scroll status bar information messages that exceed the available size */
    public static final Preference<Boolean> scrollStatusMessages = null;
    /* Scroll status bar scrolling speed */
    public static final Preference.Numeric<Double> scrollStatusSpeed = null;
    /* Scroll status bar scrolling start delay */
    public static final Preference.Numeric<Double> scrollStatusStartDelay = null;
    /* Scroll status bar scrolling end pause */
    public static final Preference.Numeric<Double> scrollStatusEndPause = null;
    /* Status bar temporary notification duration */
    public static final Preference.Numeric<Double> scrollStatusTempDuration = null;

    public static final Preference.Numeric<Integer> upnpDiscoveryTimeout = null;

    public static final Preference<String> fileSyncPath = null;

    public static final Preference<Boolean> skipAutoUpdate = null;

    public static final Preference<String> skipAutoUpdateRelease = null;

    public static final Preference<Boolean> allowExternalMacroAccess = null;

    public static final Preference<Boolean> loadMruCampaignAtStart = null;

    public static final Preference<Boolean> initiativePanelShowsTokenImage = null;

    public static final Preference<Boolean> initiativePanelShowsTokenState = null;

    public static final Preference<Boolean> initiativePanelShowsInitiative = null;

    public static final Preference<Boolean> initiativePanelShowsInitiativeOnLine2 = null;

    public static final Preference<Boolean> initiativePanelHidesNpcs = null;

    public static final Preference<Boolean> initiativePanelAllowsOwnerPermissions = null;

    public static final Preference<Boolean> initiativeMovementLocked = null;

    public static final Preference<Boolean> showInitiativeGainedMessage = null;

    public static final Preference<Boolean> initiativePanelWarnWhenResettingRoundCounter = null;

    public static final Preference<Boolean> pathfindingEnabled = null;

    public static final Preference<Boolean> pathfindingBlockedByVbl = null;

    public static final Preference<String> defaultMacroEditorTheme = null;

    public static final Preference<String> iconTheme = null;

    public static final Preference<Boolean> useCustomThemeFontProperties = null;
}
