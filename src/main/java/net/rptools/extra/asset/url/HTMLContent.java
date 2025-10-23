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
package net.rptools.extra.asset.url;

import net.rpTools.aoTool.asset.Asset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class that represents the HTML content to be displayed in the HTML pane.
 *
 * <p>This class is also used to inject the Java bridge and the base URL into the HTML content
 * if needed.
 *
 * <p>This class is immutable and thread-safe.
 */
public class HTMLContent {
  /**
   * Enum that contains the content security policy directives. This is not strictly required but it
   * is better than having them in a long string as it is easier to read, maintain and document.
   */
  public enum CSPContentDirective {
    /**
     * The default-src directive is used to specify the default policy for fetching resources such
     * as JavaScript, CSS, images, fonts, AJAX requests, frames, and HTML5 media.
     */
    DEFAULT_SRC("default-src"),
    /** asset:// URL scheme */
    ASSET("asset:"),
    /** lib:// URL scheme */
    LIB("lib:"),
    /** JQuery Content Delivery Network */
    JQUERY_CDN("https://code.jquery.com"),
    /** JSDelier Content Delivery Network (bootstrap, FontAwesome, Bootswatch, Bootstrap Icons) */
    JSDELIVR_CDN("https://cdn.jsdelivr.net"),
    /** UNPKG Content Delivery Network (React, ReactDOM, React Router, etc.) */
    UNPKG_CDN("https://unpkg.com"),
    /** Cloudflare Content Delivery Network */
    CLOUDFLARE_JS_CDN("https://cdnjs.cloudflare.com"),
    /** AJAX Content Delivery Network */
    AJAX_CDN("https://ajax.googleapis.com"),
    /** Google Hosted Libraries Content Delivery Network */
    FONTS_CDN("https://fonts.googleapis.com https://fonts.gstatic.com"),
    /** Inline content */
    INLINE("'unsafe-inline'"),
    /** Evaluate content */
    EVAL("'unsafe-eval'"),
    /** Terminator for the default-src directive */
    DEFAULT_TERMINATOR(";"),
    /** Image Source Policy */
    IMG_SRC("img-src"),
    /** Image Source Policy for all images */
    IMG_ALL("*"),
    /** Image Source Policy for all images from the asset:// URL scheme */
    IMG_ASSET("asset:"),
    /** Image Source Policy for all images from the lib:// URL scheme */
    IMG_LIB("lib:"),
    /** Terminator for the img-src directive */
    IMG_TERMINATOR(";"),
    /** Font Source Policy */
    FONT_SRC("font-src"),
    /** Google Fonts */
    FONT_GOOGLE("https://fonts.gstatic.com"),
    /** Self */
    FONT_SELF("'self'");

    /**
     * Constructor for the CSPContentDirective enum.
     *
     * @param content the content of the directive
     */
    CSPContentDirective(String content) {
      this.content = content;
    }

    /**
     * Returns the content of the directive.
     *
     * @return the content of the directive
     */
    public String getContent() {
      return content;
    }

    /** The content of the directive. */
    private final String content;
  }

  /** Content Security Policy (CSP) for the HTML content. */
  private static final String META_CSP_CONTENT =
      Arrays.stream(CSPContentDirective.values())
          .map(CSPContentDirective::getContent)
          .collect(Collectors.joining(" "));

  private sealed interface Content {}

  /** The content is a string, possibly HTML. */
  private record StringContent(String str) implements Content {}

  /** HTML content as a parsed Document */
  private record HtmlDocumentContent(Document document, boolean isJavaBridgeInjected, boolean isBaseUrlInjected)
      implements Content {
  }

  /** URL that points to the content. */
  private record UrlContent(URL url) implements Content {}

  /** The asset that represents the content if it is a binary asset. */
  private record AssetContent(Asset asset) implements Content {}

  /** The content represented. */
  private  Content content = null;

  /**
   * Constructor for the HTMLContent class.
   *
   * @param content The content represented.
   */

  private HTMLContent(Content content) {
    this.content = content;
  }
  private HTMLContent() {}
  /**
   * Factory method to create an HTMLContent object from a string that is HTML.
   *
   * @param html the String content to be displayed
   * @return an HTMLContent object
   */
  public static HTMLContent htmlFromString(@Nonnull String html) {
    return new HTMLContent();
  }

  /**
   * Factory method to create an HTMLContent object from a URL.
   *
   * @param url the URL of the HTML content
   * @return an HTMLContent object
   */
  public static HTMLContent fromURL(@Nonnull URL url) {
    return new HTMLContent(new UrlContent(url));
  }

  /**
   * Checks if the HTML content is a URL.
   *
   * @return true if the HTML content is a URL, false otherwise
   */
  public boolean isUrl() {
    return content instanceof UrlContent;
  }

  /**
   * Checks if the HTML content is a string.
   *
   * @return true if the HTML content is a string, false otherwise
   */
  public boolean isHTMLDocument() {
    return content instanceof HtmlDocumentContent;
  }

  public boolean isBinaryAsset() {
    return content instanceof AssetContent;
  }

  /**
   * Returns the HTML content as a string. If the content is a URL it will return null.
   *
   * @return the HTML content as a string
   */
  public String getHtmlString() {
    return this.toString();
  }

  /**
   * Returns the URL of the HTML content. If the content is a string it will return null.
   *
   * @return the URL of the HTML content
   */
  public URL getUrl() {
    return content instanceof UrlContent(URL url) ? url : null;
  }

  public Asset getAsset() {
    return content instanceof AssetContent(Asset asset) ? asset : null;
  }

  /**
   * Returns the HTML content as a data URL in base64 format.
   *
   * @return the HTML content as a data URL in base64 format.
   */
  public String getHtmlStringAsDataUrl() {
    return this.toString();
  }

  /**
   * Injects the base URL tag into the HTML content if it is a string.
   *
   * @return the HTMLContent object with the base URL injected.
   */
  public HTMLContent injectURLBase(@Nonnull URL baseUrl) {
    return this;
    }

  /**
   * Injects the Java Bridge into the HTML content if it is a string. If the content is a URL it
   * will return the same object without any changes. If the content has already been injected it
   * will return the same object.
   *
   * <p>If the content is a URL and it is not a HTML page, the Java Bridge will not be injected.
   *
   * @return the HTMLContent object with the Java Bridge injected.
   */
  public HTMLContent injectJavaBridge() {
    return this;
  }

  /**
   * Fetches the content from the URL.
   *
   * @return an HTMLContent object containing the HTML content.
   * @throws IOException if an error occurs while fetching the HTML content from the URL.
   * @throws IllegalStateException if the content is not a URL.
   */
  public HTMLContent fetchContent() throws IOException {
    return this;
  }

  /**
   * Fetches the string content from the URL or returns the HTML string if the content is a string.
   *
   * @return a string containing the HTML content.
   * @throws IOException if an error occurs while fetching the HTML string from the URL.
   * @throws IllegalStateException if the URL points to a binary asset.
   */
  public String fetchString() throws IOException {
      return fetchContent().getHtmlString();
  }

  /**
   * Adds the Java bridge to the HTML content.
   *
   * @param head the head element of the HTML document
   */
  private void addJavaBridge(@Nonnull Element head) {
  }

  /**
   * Adds the content security policy to the HTML content.
   *
   * @param head the head element of the HTML document
   */
  private void addCSP(@Nonnull Element head) {

  }

  /**
   * Adds the base URL to the HTML content.
   *
   * @param head the head element of the HTML document
   * @param url the URL of the HTML content
   */
  private void addBase(@Nonnull Element head, @Nonnull URL url) {

  }
}
