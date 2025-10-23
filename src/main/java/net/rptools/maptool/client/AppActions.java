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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.swing.*;

import net.rptools.lib.OsDetection;
import net.rptools.maptool.client.ui.*;

import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;

import net.rptools.maptool.model.player.*;
import net.rptools.maptool.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class acts as a container for a wide variety of {@link Action}s that are used throughout the
 * application. Most of these are added to the main frame menu, but some are added dynamically as
 * needed, sometimes to the frame menu but also to the context menu (the "right-click menu").
 *
 * <p>Each object instantiated from {@link ClientAction} should pass the base message key from the
 * properties file to the parent constructor. This base message key will be used to locate the text
 * that should appear on the menu item as well as the accelerator, mnemonic, and short description
 * strings. (See the {@link I18N} class for more details on how the key is used.
 *
 * <p>In addition, each object should override {@link ClientAction#isAvailable()} and return true if
 * the application is in a state where the Action should be enabled. (The default is <code>true
 * </code>.)
 *
 * <p>Last is the {@link ClientAction#execute(ActionEvent)} method. It is passed the {@link
 * ActionEvent} object that triggered this Action as a parameter. It should perform the necessary
 * work to accomplish the effect of the Action.
 */
public class AppActions {

  private static final Logger log = LogManager.getLogger(AppActions.class);

  private static List<ClientAction> actionList;

  private static List<ClientAction> getActionList() {
    if (actionList == null) {
      actionList = new ArrayList<ClientAction>();
    }
    return actionList;
  }


  public abstract static class ClientAction extends AbstractAction {

    /** Does the code need to guard against bug https://bugs.openjdk.java.net/browse/JDK-8208712. */
    private static final boolean NEEDS_GUARD;

    static {
      String prop = System.getProperty("os.name");
      NEEDS_GUARD =
          "Mac OS X"
              .equals(
                  prop); // MapTool doesnt run on version 8 or less of JDK so no need to check that
    }

    protected ClientAction() {
      getActionList().add(this);
    }

    protected ClientAction(@Nullable KeyStroke accelerator) {
      this();
      if (accelerator != null) {
        putValue(Action.ACCELERATOR_KEY, accelerator);
      }
    }

    /**
     * The last time this action was called via accelerator key. This will only ever be set if
     * {@link #NEEDS_GUARD} is <code>true</code> and the menu item is a JMenuCheckBoxItem
     */
    private long lastAccelInvoke;

    public final void execute(ActionEvent e) {
      if (NEEDS_GUARD && (e != null) && (e.getSource() instanceof JCheckBoxMenuItem)) {
        if (e.getModifiers() == 0) {
          if (TimeUnit.MILLISECONDS.toSeconds(e.getWhen() - lastAccelInvoke) < 1) {
            return; // Nothing to do as its due to the JDK bug
            // https://bugs.openjdk.java.net/browse/JDK-8208712
          }
        } else if ((e.getModifiers() & OsDetection.menuShortcut) != 0) {
          lastAccelInvoke = e.getWhen();
        }
      }
      executeAction();
    }

    /**
     * This convenience function returns the KeyStroke that represents the accelerator key used by
     * the Action. This function can return <code>null</code> because not all Actions have an
     * associated accelerator key defined, but it is currently only called by methods that reference
     * the {CUT,COPY,PASTE}_TOKEN Actions.
     *
     * @return KeyStroke associated with the Action or <code>null</code>
     */
    public final KeyStroke getKeyStroke() {
      return (KeyStroke) getValue(Action.ACCELERATOR_KEY);
    }

    public boolean isAvailable() {
      return true;
    }

    public boolean isSelected() {
      return false;
    }


    protected abstract void executeAction();
  }

  public abstract static class TranslatedClientAction extends ClientAction {
    private final String i18nKey;

    protected TranslatedClientAction(String i18nKey) {
      this(i18nKey, null);
    }

    protected TranslatedClientAction(String i18nKey, @Nullable KeyStroke accelerator) {
      super(accelerator);
      this.i18nKey = i18nKey;
      I18N.setAction(i18nKey, this);
    }

    public final String getI18nKey() {
      return i18nKey;
    }
  }


}
