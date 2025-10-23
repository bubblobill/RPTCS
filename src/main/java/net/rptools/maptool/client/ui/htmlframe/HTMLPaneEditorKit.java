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
package net.rptools.maptool.client.ui.htmlframe;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTMLEditorKit;

import java.io.*;

@SuppressWarnings("serial")
class HTMLPaneEditorKit extends EditorKit {
  private final HTMLPaneViewFactory viewFactory;

  HTMLPaneEditorKit(HTMLPane htmlPane) {
    viewFactory = new HTMLPaneViewFactory(new HTMLEditorKit.HTMLFactory(), htmlPane);
  }

    @Override
    public String getContentType() {
        return "";
    }

    @Override
  public ViewFactory getViewFactory() {
    return viewFactory;
  }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Caret createCaret() {
        return null;
    }

    @Override
    public Document createDefaultDocument() {
        return null;
    }

    @Override
    public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {

    }

    @Override
    public void write(OutputStream out, Document doc, int pos, int len) throws IOException, BadLocationException {

    }

    @Override
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {

    }

    @Override
    public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {

    }

}
