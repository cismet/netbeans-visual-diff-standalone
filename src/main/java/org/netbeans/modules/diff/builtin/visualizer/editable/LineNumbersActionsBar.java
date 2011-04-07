/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.api.diff.Difference;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

/**
 * Draws both line numbers and diff actions for a decorated editor pane.
 *
 * @author   Maros Sandor
 * @version  $Revision$, $Date$
 */
class LineNumbersActionsBar extends JPanel implements Scrollable,
    MouseMotionListener,
    MouseListener,
    PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final int ACTIONS_BAR_WIDTH = 16;
    private static final int LINES_BORDER_WIDTH = 4;
    private static final Point POINT_ZERO = new Point(0, 0);

    //~ Instance fields --------------------------------------------------------

    private final Image insertImage = org.openide.util.Utilities.loadImage(
            "org/netbeans/modules/diff/builtin/visualizer/editable/insert.png"); // NOI18N
    private final Image removeImage = org.openide.util.Utilities.loadImage(
            "org/netbeans/modules/diff/builtin/visualizer/editable/remove.png"); // NOI18N

    private final Image insertActiveImage = org.openide.util.Utilities.loadImage(
            "org/netbeans/modules/diff/builtin/visualizer/editable/insert_active.png"); // NOI18N
    private final Image removeActiveImage = org.openide.util.Utilities.loadImage(
            "org/netbeans/modules/diff/builtin/visualizer/editable/remove_active.png"); // NOI18N

    private final DiffContentPanel master;
    private final boolean actionsEnabled;
    private final int actionIconsHeight;
    private final int actionIconsWidth;

    private final String lineNumberPadding = "        "; // NOI18N

    private int linesWidth;
    private int actionsWidth;

    private Color linesColor;
    private int linesCount;
    private int maxNumberCount;

    private Point lastMousePosition = POINT_ZERO;
    private HotSpot lastHotSpot = null;

    private List<HotSpot> hotspots = new ArrayList<HotSpot>(0);

    private int oldLinesWidth;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LineNumbersActionsBar object.
     *
     * @param  master          DOCUMENT ME!
     * @param  actionsEnabled  DOCUMENT ME!
     */
    public LineNumbersActionsBar(final DiffContentPanel master, final boolean actionsEnabled) {
        this.master = master;
        this.actionsEnabled = actionsEnabled;
        actionsWidth = actionsEnabled ? ACTIONS_BAR_WIDTH : 0;
        actionIconsHeight = insertImage.getHeight(this);
        actionIconsWidth = insertImage.getWidth(this);
        setOpaque(true);
        setToolTipText(""); // NOI18N
        master.getMaster().addPropertyChangeListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void addNotify() {
        super.addNotify();
        initUI();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Font getLinesFont() {
        final String mimeType = DocumentUtilities.getMimeType(master.getEditorPane());
        final FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        final Coloring col = Coloring.fromAttributeSet(fcs.getFontColors(FontColorNames.LINE_NUMBER_COLORING));
        Font font = col.getFont();
        if (font == null) {
            font = Coloring.fromAttributeSet(fcs.getFontColors(FontColorNames.DEFAULT_COLORING)).getFont();
        }
        return font;
    }

    /**
     * DOCUMENT ME!
     */
    private void initUI() {
        final String mimeType = DocumentUtilities.getMimeType(master.getEditorPane());
        final FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        final AttributeSet attrs = fcs.getFontColors(FontColorNames.LINE_NUMBER_COLORING);
        final AttributeSet defAttrs = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);

        linesColor = (Color)attrs.getAttribute(StyleConstants.Foreground);
        if (linesColor == null) {
            linesColor = (Color)defAttrs.getAttribute(StyleConstants.Foreground);
        }
        Color bg = (Color)attrs.getAttribute(StyleConstants.Background);
        if (bg == null) {
            bg = (Color)defAttrs.getAttribute(StyleConstants.Background);
        }
        setBackground(bg);

        updateStateOnDocumentChange();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   p  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private HotSpot getHotspotAt(final Point p) {
        for (final HotSpot hotspot : hotspots) {
            if (hotspot.getRect().contains(p)) {
                return hotspot;
            }
        }
        return null;
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
        final Point p = event.getPoint();
        final HotSpot spot = getHotspotAt(p);
        if (spot == null) {
            return null;
        }
        final Difference diff = spot.getDiff();
        if (diff.getType() == Difference.ADD) {
            return NbBundle.getMessage(LineNumbersActionsBar.class, "TT_DiffPanel_Remove");  // NOI18N
        } else if (diff.getType() == Difference.CHANGE) {
            return NbBundle.getMessage(LineNumbersActionsBar.class, "TT_DiffPanel_Replace"); // NOI18N
        } else {
            return NbBundle.getMessage(LineNumbersActionsBar.class, "TT_DiffPanel_Insert");  // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  spot  DOCUMENT ME!
     */
    private void performAction(final HotSpot spot) {
        master.getMaster().rollback(spot.getDiff());
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if (!e.isPopupTrigger()) {
            final HotSpot spot = getHotspotAt(e.getPoint());
            if (spot != null) {
                performAction(spot);
            }
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        // not interested
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        // not interested
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        // not interested
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        lastMousePosition = POINT_ZERO;
        if (lastHotSpot != null) {
            repaint(lastHotSpot.getRect());
        }
        lastHotSpot = null;
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        final Point p = e.getPoint();
        lastMousePosition = p;
        final HotSpot spot = getHotspotAt(p);
        if (lastHotSpot != spot) {
            repaint((lastHotSpot == null) ? spot.getRect() : lastHotSpot.getRect());
        }
        lastHotSpot = spot;
        setCursor((spot != null) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        // not interested
    }

    /**
     * DOCUMENT ME!
     */
    void onUISettingsChanged() {
        initUI();
        updateStateOnDocumentChange();
        repaint();
    }

    /**
     * DOCUMENT ME!
     */
    private void updateStateOnDocumentChange() {
        assert SwingUtilities.isEventDispatchThread();
        final StyledDocument doc = (StyledDocument)master.getEditorPane().getDocument();
        final int lastOffset = doc.getEndPosition().getOffset();
        linesCount = org.openide.text.NbDocument.findLineNumber(doc, lastOffset);

        final Graphics g = getGraphics();
        if (g != null) {
            checkLinesWidth(g);
        }
        maxNumberCount = getNumberCount(linesCount);
        revalidate();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   g  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean checkLinesWidth(final Graphics g) {
        final FontMetrics fm = g.getFontMetrics(getLinesFont());
        final Rectangle2D rect = fm.getStringBounds(Integer.toString(linesCount), g);
        linesWidth = (int)rect.getWidth() + (LINES_BORDER_WIDTH * 2);
        if (linesWidth != oldLinesWidth) {
            oldLinesWidth = linesWidth;
            revalidate();
            repaint();
            return true;
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   n  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getNumberCount(int n) {
        int nc = 0;
        for (; n > 0; n /= 10, nc++) {
            ;
        }
        return nc;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        final Dimension dim = master.getEditorPane().getPreferredScrollableViewportSize();
        return new Dimension(getBarWidth(), dim.height);
    }

    @Override
    public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
        return master.getEditorPane().getScrollableUnitIncrement(visibleRect, orientation, direction); // 123
    }

    @Override
    public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
        return master.getEditorPane().getScrollableBlockIncrement(visibleRect, orientation, direction);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getBarWidth(), Integer.MAX_VALUE >> 2);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getBarWidth() {
        return actionsWidth + linesWidth;
    }

    /**
     * DOCUMENT ME!
     */
    public void onDiffSetChanged() {
        updateStateOnDocumentChange();
        repaint();
    }

    @Override
    protected void paintComponent(final Graphics gr) {
        final Graphics2D g = (Graphics2D)gr;
        final Rectangle clip = g.getClipBounds();
        final Stroke cs = g.getStroke();

        if (checkLinesWidth(gr)) {
            return;
        }

        final String mimeType = DocumentUtilities.getMimeType(master.getEditorPane());
        final FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        final Map renderingHints = (Map)fcs.getFontColors(FontColorNames.DEFAULT_COLORING)
                    .getAttribute(EditorStyleConstants.RenderingHints);
        if (!renderingHints.isEmpty()) {
            g.addRenderingHints(renderingHints);
        }

        final EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(master.getEditorPane());
        int lineHeight = editorUI.getLineHeight();

        g.setColor(getBackground());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        g.setColor(Color.LIGHT_GRAY);
        final int x = master.isFirst() ? 0 : (getBarWidth() - 1);
        g.drawLine(x, clip.y, x, clip.y + clip.height - 1);

        final DiffViewManager.DecoratedDifference[] diffs = master.getMaster().getManager().getDecorations();

        final int actionsYOffset = (lineHeight - actionIconsHeight) / 2;
        final int offset = linesWidth;

        final int currentDifference = master.getMaster().getCurrentDifference();
        final List<HotSpot> newActionIcons = new ArrayList<HotSpot>();
        if (master.isFirst()) {
            int idx = 0;
            for (final DiffViewManager.DecoratedDifference dd : diffs) {
                g.setColor(master.getMaster().getColorLines());
                g.setStroke((currentDifference == idx) ? master.getMaster().getBoldStroke() : cs);
                g.drawLine(0, dd.getTopLeft(), clip.width, dd.getTopLeft());
                if (dd.getBottomLeft() != -1) {
                    g.drawLine(0, dd.getBottomLeft(), clip.width, dd.getBottomLeft());
                }
                if (actionsEnabled && dd.canRollback()) {
                    if (dd.getDiff().getType() != Difference.ADD) {
                        final Rectangle hotSpot = new Rectangle(
                                1,
                                dd.getTopLeft()
                                        + actionsYOffset,
                                actionIconsWidth,
                                actionIconsHeight);
                        if (hotSpot.contains(lastMousePosition) || (idx == currentDifference)) {
                            g.drawImage(insertActiveImage, hotSpot.x, hotSpot.y, this);
                        } else {
                            g.drawImage(insertImage, hotSpot.x, hotSpot.y, this);
                        }
                        newActionIcons.add(new HotSpot(hotSpot, dd.getDiff()));
                    }
                }
                idx++;
            }
        } else {
            int idx = 0;
            for (final DiffViewManager.DecoratedDifference dd : diffs) {
                g.setColor(master.getMaster().getColorLines());
                g.setStroke((currentDifference == idx) ? master.getMaster().getBoldStroke() : cs);
                g.drawLine(clip.x, dd.getTopRight(), clip.x + clip.width, dd.getTopRight());
                if (dd.getBottomRight() != -1) {
                    g.drawLine(clip.x, dd.getBottomRight(), clip.x + clip.width, dd.getBottomRight());
                }
                if (actionsEnabled && dd.canRollback()) {
                    if (dd.getDiff().getType() == Difference.ADD) {
                        final Rectangle hotSpot = new Rectangle(offset + 1,
                                dd.getTopRight()
                                        + actionsYOffset,
                                actionIconsWidth,
                                actionIconsHeight);
                        if (hotSpot.contains(lastMousePosition) || (idx == currentDifference)) {
                            g.drawImage(removeActiveImage, hotSpot.x, hotSpot.y, this);
                        } else {
                            g.drawImage(removeImage, hotSpot.x, hotSpot.y, this);
                        }
                        newActionIcons.add(new HotSpot(hotSpot, dd.getDiff()));
                    }
                }
                idx++;
            }
        }

        hotspots = newActionIcons;

        int linesXOffset = master.isFirst() ? actionsWidth : 0;
        linesXOffset += LINES_BORDER_WIDTH;

        g.setFont(getLinesFont());
        g.setColor(linesColor);
        try {
            final View rootView = Utilities.getDocumentView(master.getEditorPane());
            int lineNumber = Utilities.getLineOffset((BaseDocument)master.getEditorPane().getDocument(),
                    master.getEditorPane().viewToModel(new Point(clip.x, clip.y)));
            if (lineNumber > 0) {
                --lineNumber;
            }
            View view = rootView.getView(lineNumber);
            final Rectangle rec = master.getEditorPane().modelToView(view.getStartOffset());
            if (rec == null) {
                return;
            }
            int yOffset;
            int linesDrawn = (clip.height / lineHeight) + 4; // draw past clipping rectangle to avoid partially drawn
                                                             // numbers
            final int docLines = Utilities.getRowCount((BaseDocument)master.getEditorPane().getDocument());
            if ((lineNumber + linesDrawn) > docLines) {
                linesDrawn = docLines - lineNumber;
            }
            for (int i = 0; i < linesDrawn; i++) {
                view = rootView.getView(lineNumber);
                final Rectangle rec1 = master.getEditorPane().modelToView(view.getStartOffset());
                final Rectangle rec2 = master.getEditorPane().modelToView(view.getEndOffset() - 1);
                if ((rec1 == null) || (rec2 == null)) {
                    break;
                }
                yOffset = rec1.y + rec1.height - (lineHeight / 4);
                lineHeight = (int)(rec2.getY() + rec2.getHeight() - rec1.getY());
                g.drawString(formatLineNumber(++lineNumber), linesXOffset, yOffset);
            }
        } catch (BadLocationException ex) {
            //
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lineNumber  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String formatLineNumber(final int lineNumber) {
        final String strNumber = Integer.toString(lineNumber);
        final int nc = getNumberCount(lineNumber);
        if (nc < maxNumberCount) {
            final StringBuilder sb = new StringBuilder(10);
            sb.append(lineNumberPadding, 0, maxNumberCount - nc);
            sb.append(strNumber);
            return sb.toString();
        } else {
            return strNumber;
        }
    }
}
