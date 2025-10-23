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
package net.rptools.maptool.client.ui.theme;

import com.formdev.flatlaf.FlatIconColors;
import com.formdev.flatlaf.FlatLaf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jidesoft.plaf.LookAndFeelFactory;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.swing.*;
import net.rptools.lib.AwtUtil;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.themes.*;
import net.rptools.maptool.events.MapToolEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class used to implement Theme support for MapTool. */
public class ThemeSupport {

  private static final Logger log = LogManager.getLogger(ThemeSupport.class);

  public enum ThemeColor {
    RED(
        "ColorPalette.red",
        Color.decode("#DB5860"),
        Color.decode("#C75450"),
        FlatIconColors.ACTIONS_RED,
        FlatIconColors.ACTIONS_RED_DARK),
    YELLOW(
        "ColorPalette.yellow",
        Color.decode("#EDA200"),
        Color.decode("#F0A732"),
        FlatIconColors.ACTIONS_YELLOW,
        FlatIconColors.ACTIONS_YELLOW_DARK),
    GREEN(
        "ColorPalette.green",
        Color.decode("#59A869"),
        Color.decode("#499C54"),
        FlatIconColors.ACTIONS_GREEN,
        FlatIconColors.ACTIONS_GREEN_DARK),
    BLUE(
        "ColorPalette.blue",
        Color.decode("#389FD6"),
        Color.decode("#3592C4"),
        FlatIconColors.ACTIONS_BLUE,
        FlatIconColors.ACTIONS_BLUE_DARK),
    GREY(
        "ColorPalette.gray",
        Color.decode("#6E6E6E"),
        Color.decode("#AFB1B3"),
        FlatIconColors.ACTIONS_GREY,
        FlatIconColors.ACTIONS_GREY_DARK),
    PURPLE(
        "ColorPalette.purple",
        Color.decode("#B99BF8"),
        Color.decode("#B99BF8"),
        FlatIconColors.OBJECTS_PURPLE,
        FlatIconColors.OBJECTS_PURPLE);

    private final String propertyName;
    private final Color defaultLightColor;
    private final Color defaultDarkColor;

    private final FlatIconColors lightIconColor;
    private final FlatIconColors darkIconColor;

    ThemeColor(
        String propertyName,
        Color defaultLightColor,
        Color defaultDarkColor,
        FlatIconColors lightIconColor,
        FlatIconColors darkIconColor) {
      this.propertyName = propertyName;
      this.defaultLightColor = defaultLightColor;
      this.defaultDarkColor = defaultDarkColor;
      this.lightIconColor = lightIconColor;
      this.darkIconColor = darkIconColor;
    }

    String getPropertyName() {
      return propertyName;
    }
  }

  /** The path to the images detailing the theme. */
  private static final String IMAGE_PATH = "/net/rptools/maptool/client/ui/themes/image/";

  /**
   * Should the chat window use the themes colors.
   *
   * @return true if the chat window should use the themes colors.
   */
  public static boolean shouldUseThemeColorsForChat() {
    return useThemeColorsForChat;
  }

  /**
   * Should the chat window use the themes colors.
   *
   * @param useThemeColorsForChat true if the chat window should use the themes colors.
   */
  public static void setUseThemeColorsForChat(boolean useThemeColorsForChat) {
    if (ThemeSupport.useThemeColorsForChat != useThemeColorsForChat) {
      ThemeSupport.useThemeColorsForChat = useThemeColorsForChat;
      writeTheme();
    }
  }

  /**
   * Record that contains the details about a theme.
   *
   * @param name the name of the theme
   * @param themeClass the class that implements the theme
   * @param imagePath the path to an example image of the theme
   */
  public record ThemeDetails(
      String name, Class<? extends FlatLaf> themeClass, String imagePath, boolean dark) {}

  /** The list of themes that are available. */
  public static final ThemeDetails[] THEMES =
      new ThemeDetails[] {
        new ThemeDetails("Aah", AahLAF.class, "Aah.png", false),
        new ThemeDetails("Aah(Large Print)", AahLAF_LP.class, "Aah-LP.png", false),
        new ThemeDetails("Aah(Small Print)", AahLAF_SP.class, "Aah-SP.png", false),
        new ThemeDetails("Aah(Very Large Print)", AahLAF_VLP.class, "Aah-VLP.png", false),
        new ThemeDetails("Aark", AarkLaF.class, "Aark.png", true),
      };

  /** The current theme being used. */
  private static ThemeDetails currentThemeDetails = THEMES[0];

  /** The theme that will be applied after restart. */
  private static ThemeDetails pendingThemeDetails = currentThemeDetails;

  /** The current look and feel in use. */
  private static FlatLaf currentLaf;

  /** Should the chat window use the colors from the theme. */
  private static boolean useThemeColorsForChat = false;

  private static boolean startupUseThemeColorsForChat = false;

  /**
   * Loads the details of the theme to use.
   *
   * @throws NoSuchMethodException if there is an error finding the theme class.
   * @throws InvocationTargetException if there is an error invoking the theme class.
   * @throws InstantiationException if there is an error instantiating the theme class.
   * @throws IllegalAccessException if there is an error accessing the theme class.
   * @throws UnsupportedLookAndFeelException if the look and feel is not supported.
   */
  public static void loadTheme()
      throws NoSuchMethodException,
          InvocationTargetException,
          InstantiationException,
          IllegalAccessException,
          UnsupportedLookAndFeelException {
    JsonObject theme = readTheme();

    String themeName = theme.getAsJsonPrimitive("theme").getAsString();
    if (theme.has("useThemeColorsForChat")) {
      useThemeColorsForChat = theme.getAsJsonPrimitive("useThemeColorsForChat").getAsBoolean();
      startupUseThemeColorsForChat = useThemeColorsForChat;
    }

    ThemeDetails themeDetails =
        Arrays.stream(THEMES)
            .filter(t -> t.name.equals(themeName))
            .findFirst()
            .orElse(currentThemeDetails);
//    if (AppPreferences.useCustomThemeFontProperties.get()) {
////      ThemeFontTools.flatusInterruptus();
//    }
    if (themeDetails != null) {
      var laf = themeDetails.themeClass.getDeclaredConstructor().newInstance();
      UIManager.setLookAndFeel(themeDetails.themeClass.getDeclaredConstructor().newInstance());
      LookAndFeelFactory.installJideExtension();
      setLaf(laf);
      currentThemeDetails = themeDetails;
      pendingThemeDetails = themeDetails;
    }

    new MapToolEventBus().getMainEventBus().post(new ThemeLoadedEvent(currentThemeDetails));
  }

  /**
   * Sets the look and feel to use.
   *
   * @param laf the look and feel to use.
   */
  private static void setLaf(FlatLaf laf) {
    currentLaf = laf;
  }

  /**
   * Reads the theme from the settings file.
   *
   * @return the theme from the settings file.
   */
  private static JsonObject readTheme() {
    try (InputStreamReader reader =
        new InputStreamReader(new FileInputStream(AppConstants.THEME_CONFIG_FILE))) {
      return JsonParser.parseReader(reader).getAsJsonObject();
    } catch (IOException e) {
      return toJSon(currentThemeDetails);
    }
  }

  /** Writes the theme to the settings file. */
  private static void writeTheme() {
    var json = toJSon(pendingThemeDetails);
    json.addProperty("useThemeColorsForChat", useThemeColorsForChat);
    try (FileWriter writer = new FileWriter(AppConstants.THEME_CONFIG_FILE)) {
      writer.write(json.toString());
    } catch (IOException e) {
      // MapTool.showError("msg.error.cantSaveTheme", e);
    }
  }

  /**
   * Converts the theme details to a JSON object.
   *
   * @param theme the theme details to convert.
   * @return the JSON object.
   */
  private static JsonObject toJSon(ThemeDetails theme) {
    JsonObject json = new JsonObject();
    json.addProperty("theme", theme.name);
    return json;
  }

  /**
   * Sets the theme to use from the name.
   *
   * @param theme the name of the theme to use.
   */
  public static void setTheme(String theme) {
    pendingThemeDetails =
        Arrays.stream(THEMES)
            .filter(t -> t.name.equals(theme))
            .findFirst()
            .orElse(currentThemeDetails);
    writeTheme();
  }

  /**
   * Returns the current theme name.
   *
   * @return the current theme name.
   */
  public static String getThemeName() {
    return currentThemeDetails.name;
  }

  /**
   * Returns the current theme information.
   *
   * @return the current theme information.
   */
  public static ThemeDetails getCurrentThemeDetails() {
    return currentThemeDetails;
  }

  /**
   * Returns if the theme is a dark theme or not.
   *
   * @return if the theme is a dark theme or not.
   */
  public static boolean isDark() {
    return currentLaf.isDark();
  }

  /**
   * Returns an {@link ImageIcon} for an example of the current theme.
   *
   * @param dimension the size of the image.
   * @return an {@link ImageIcon} for an example of the current theme.
   */
  public static ImageIcon getExampleImageIcon(Dimension dimension) {
    return getExampleImageIcon(currentThemeDetails.name, dimension);
  }

  /**
   * Returns an {@link ImageIcon} for an example of the specified theme.
   *
   * @param themeName the name of the theme to get the image for.
   * @param dimension the size of the image.
   * @return an {@link ImageIcon} for an example of the specified theme.
   */
  public static ImageIcon getExampleImageIcon(String themeName, Dimension dimension) {
    ThemeDetails themeDetails =
        Arrays.stream(THEMES).filter(t -> t.name.equals(themeName)).findFirst().orElse(null);
    if (themeDetails == null
        || themeDetails.imagePath == null
        || themeDetails.imagePath.isEmpty()) {
      return new ImageIcon();
    } else {
      var imageLocation = IMAGE_PATH + themeDetails.imagePath;
      log.info("Retrieving resource for theme name={} from location={}", themeName, imageLocation);
      var imageURL = ThemeSupport.class.getResource(imageLocation);
      if (imageURL == null) {
        log.warn(
            "Failed to retrieve resource for theme name={} from url={}, using empty ImageIcon",
            themeName,
            imageURL);
        return new ImageIcon();
      }
      var imageIcon = new ImageIcon(imageURL, themeDetails.name);
      if (dimension != null && dimension.width > 0 && dimension.height > 0) {
        var imageSize = new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight());
        AwtUtil.constrainTo(imageSize, dimension.width, dimension.height);

        imageIcon.setImage(
            imageIcon
                .getImage()
                .getScaledInstance(imageSize.width, imageSize.height, Image.SCALE_AREA_AVERAGING));
      }
      return imageIcon;
    }
  }

  /**
   * Returns if there is a new theme that will be applied after the restart.
   *
   * @return if there is a new theme that will be applied after the restart.
   */
  public static boolean needsRestartForNewTheme() {
    return useThemeColorsForChat != startupUseThemeColorsForChat
        || !pendingThemeDetails.equals(currentThemeDetails);
  }

  /**
   * Returns what the theme will be after a restart of MapTool.
   *
   * @return what the theme will be after a restart of MapTool.
   */
  public static String getThemeAfterRestart() {
    return pendingThemeDetails.name;
  }

  /**
   * Returns one of the named theme colors.
   *
   * @param themeColor the color to return.
   * @return the color.
   */
  public static Color getThemeColor(ThemeColor themeColor) {
    Color color = null;
    if (currentThemeDetails.dark()) {
      color = UIManager.getColor(themeColor.darkIconColor.key);
    }

    if (color == null) {
      color = UIManager.getColor(themeColor.lightIconColor.key);
    }

    if (color == null) {
      if (currentThemeDetails.dark()) {
        color = themeColor.defaultDarkColor;
      } else {
        color = themeColor.defaultLightColor;
      }
    }
    return color;
  }

  public static String getThemeColorHexString(ThemeColor themeColor) {
    return String.format("#%06x", getThemeColor(themeColor).getRGB() & 0x00FFFFFF);
  }
}
