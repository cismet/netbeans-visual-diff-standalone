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
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.diff.Utils;
import org.netbeans.modules.editor.java.JavaKit;

import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

import java.awt.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.text.*;

/**
 * Editor pane with added decorations (diff lines).
 *
 * @author   Maros Sandor
 * @version  $Revision$, $Date$
 */
class DecoratedEditorPane extends JEditorPane implements PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final RequestProcessor FONT_RP = new RequestProcessor("DiffFontLoadingRP", 1); // NOI18N

    //~ Instance fields --------------------------------------------------------

    private Difference[] currentDiff;
    private DiffContentPanel master;

    private final RequestProcessor.Task repaintTask;

    private int fontHeight = -1;
    private int charWidth;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DecoratedEditorPane object.
     *
     * @param  master  DOCUMENT ME!
     */
    public DecoratedEditorPane(final DiffContentPanel master) {
//                 BaseTextUI btu = new BaseTextUI();
//        btu.installUI(this);
//        super.setUI(btu);
        repaintTask = Utils.createParallelTask(new RepaintPaneTask());
        setBorder(null);
        this.master = master;
        master.getMaster().addPropertyChangeListener(this);
        final EditorKit kit = new JavaKit(); // CloneableEditorSupport.getEditorKit("text/x-java");
        setEditorKit(kit);
        setContentType("text/x-java");
//        setEditorKit(CloneableEditorSupport.getEditorKit("text/plain"));
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return    DOCUMENT ME!
     *
     * @Override  public void setUI(ComponentUI cui){ System.out.println("setComponentUI"); new
     *            Exception().printStackTrace(); BaseTextUI btu=new BaseTextUI(); btu.installUI(this); super.setUI(btu);
     *            invalidate(); } @Override public void setUI(TextUI cui){ System.out.println("settextUI"); BaseTextUI
     *            btu = new BaseTextUI(); btu.installUI(this); new Exception().printStackTrace(); super.setUI(btu);
     *            invalidate(); }
     */
    public boolean isFirst() {
        return master.isFirst();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DiffContentPanel getMaster() {
        return master;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  diff  DOCUMENT ME!
     */
    void setDifferences(final Difference[] diff) {
        currentDiff = diff;
        repaint();
    }

    @Override
    public void setFont(final Font font) {
        super.setFont(font);
        setFontHeightWidth(getFont());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  font  DOCUMENT ME!
     */
    private void setFontHeightWidth(final Font font) {
        FONT_RP.post(new Runnable() {

                @Override
                public void run() {
                    final FontMetrics metrics = getFontMetrics(font);
                    charWidth = metrics.charWidth('m');
                    fontHeight = metrics.getHeight();
                }
            });
    }

    @Override
    public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
        if (fontHeight == -1) {
            return super.getScrollableUnitIncrement(visibleRect, orientation, direction);
        }
        switch (orientation) {
            case SwingConstants.VERTICAL: {
                return fontHeight;
            }
            case SwingConstants.HORIZONTAL: {
                return charWidth;
            }
            default: {
                throw new IllegalArgumentException("Invalid orientation: " + orientation); // discrimination
            }
        }
    }

    @Override
    protected void paintComponent(final Graphics gr) {
        super.paintComponent(gr);
        if (currentDiff == null) {
            return;
        }

        final EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(this);

        final Graphics2D g = (Graphics2D)gr.create();
        final Rectangle clip = g.getClipBounds();
        final Stroke cs = g.getStroke();
        // compensate for cursor drawing, it is needed for catching a difference on the cursor line
        clip.y -= 1;
        clip.height += 1;

        final FoldHierarchy foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        final JTextComponent component = editorUI.getComponent();
        if (component == null) {
            return;
        }
        final View rootView = Utilities.getDocumentView(component);
        if (rootView == null) {
            return;
        }
        final BaseTextUI textUI = (BaseTextUI)component.getUI();

        final AbstractDocument doc = (AbstractDocument)component.getDocument();
        doc.readLock();
        try {
            foldHierarchy.lock();
            try {
                final int startPos = textUI.getPosFromY(clip.y);
                final int startViewIndex = rootView.getViewIndex(startPos, Position.Bias.Forward);
                final int rootViewCount = rootView.getViewCount();

                if ((startViewIndex >= 0) && (startViewIndex < rootViewCount)) {
                    // find the nearest visible line with an annotation
                    final Rectangle rec = textUI.modelToView(
                            component,
                            rootView.getView(startViewIndex).getStartOffset());
                    int y = (rec == null) ? 0 : rec.y;

                    final int clipEndY = clip.y + clip.height;
                    final Element rootElem = textUI.getRootView(component).getElement();

                    View view = rootView.getView(startViewIndex);
                    int line = rootElem.getElementIndex(view.getStartOffset());
                    line++; // make it 1-based

                    final int curDif = master.getMaster().getCurrentDifference();

                    g.setColor(master.getMaster().getColorLines());
                    for (int i = startViewIndex; i < rootViewCount; i++) {
                        view = rootView.getView(i);
                        line = rootElem.getElementIndex(view.getStartOffset());
                        line++; // make it 1-based
                        Difference ad = master.isFirst() ? EditableDiffView.getFirstDifference(currentDiff, line)
                                                         : EditableDiffView.getSecondDifference(currentDiff, line);
                        final Rectangle rec1 = component.modelToView(view.getStartOffset());
                        final Rectangle rec2 = component.modelToView(view.getEndOffset() - 1);
                        if ((rec1 == null) || (rec2 == null)) {
                            break;
                        }
                        y = (int)rec1.getY();
                        final int height = (int)(rec2.getY() + rec2.getHeight() - rec1.getY());
                        if (ad != null) {
                            // TODO: can cause AIOOBE, synchronize "currentDiff" and "curDif" variables
                            g.setStroke(((curDif >= 0) && (curDif < currentDiff.length) && (currentDiff[curDif] == ad))
                                    ? master.getMaster().getBoldStroke() : cs);
                            final int yy = y + height;
                            if (ad.getType() == (master.isFirst() ? Difference.ADD : Difference.DELETE)) {
                                g.drawLine(0, yy, getWidth(), yy);
                                ad = null;
                            } else {
                                if ((master.isFirst() ? ad.getFirstStart() : ad.getSecondStart()) == line) {
                                    g.drawLine(0, y, getWidth(), y);
                                }
                                if ((master.isFirst() ? ad.getFirstEnd() : ad.getSecondEnd()) == line) {
                                    g.drawLine(0, yy, getWidth(), yy);
                                }
                            }
                        }
                        y += height;
                        if (y >= clipEndY) {
                            break;
                        }
                    }
                }
            } finally {
                foldHierarchy.unlock();
            }
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ble);
        } finally {
            doc.readUnlock();
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        repaintTask.schedule(150);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class RepaintPaneTask implements Runnable {

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        repaint();
                    }
                });
        }
    }
}
