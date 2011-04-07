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
import org.netbeans.editor.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;

import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;

import java.io.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;

/**
 * Handles interaction among Diff components: editor panes, scroll bars, action bars and the split pane.
 *
 * @author   Maros Sandor
 * @version  $Revision$, $Date$
 */
class DiffViewManager implements ChangeListener {

    //~ Instance fields --------------------------------------------------------

    private final EditableDiffView master;

    private final DiffContentPanel leftContentPanel;
    private final DiffContentPanel rightContentPanel;

    /** True when this class caused the current scroll event, false otherwise. */
    private boolean myScrollEvent;

    private int cachedDiffSerial;
    private DecoratedDifference[] decorationsCached = new DecoratedDifference[0];
    private HighLight[] secondHilitesCached = new HighLight[0];
    private HighLight[] firstHilitesCached = new HighLight[0];
    private final ScrollMapCached scrollMap = new ScrollMapCached();
    private final RequestProcessor.Task highlightComputeTask;

    private final Boolean[] smartScrollDisabled = new Boolean[] { Boolean.FALSE };

    private int rightHeightCached;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DiffViewManager object.
     *
     * @param  master  DOCUMENT ME!
     */
    public DiffViewManager(final EditableDiffView master) {
        this.master = master;
        this.leftContentPanel = master.getEditorPane1();
        this.rightContentPanel = master.getEditorPane2();
        highlightComputeTask = new RequestProcessor("DiffViewHighlightsComputer", 1, true, false).create(
                new HighlightsComputeTask());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void init() {
        initScrolling();
    }

    /**
     * DOCUMENT ME!
     */
    private void initScrolling() {
        leftContentPanel.getScrollPane().getVerticalScrollBar().getModel().addChangeListener(this);
        rightContentPanel.getScrollPane().getVerticalScrollBar().getModel().addChangeListener(this);
        // The vertical scroll bar must be there for mouse wheel to work correctly.
        // However it's not necessary to be seen (but must be visible so that the wheel will work).
        leftContentPanel.getScrollPane().getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
    }

    /**
     * DOCUMENT ME!
     *
     * @param  runnable  DOCUMENT ME!
     */
    public void runWithSmartScrollingDisabled(final Runnable runnable) {
        synchronized (smartScrollDisabled) {
            smartScrollDisabled[0] = true;
        }
        try {
            runnable.run();
        } catch (Exception e) {
            Logger.getLogger(DiffViewManager.class.getName()).log(Level.SEVERE, "", e);
        } finally {
            SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        synchronized (smartScrollDisabled) {
                            smartScrollDisabled[0] = false;
                        }
                    }
                });
        }
    }

    @Override
    public void stateChanged(final ChangeEvent e) {
        final JScrollBar leftScrollBar = leftContentPanel.getScrollPane().getVerticalScrollBar();
        final JScrollBar rightScrollBar = rightContentPanel.getScrollPane().getVerticalScrollBar();
        if (e.getSource() == leftContentPanel.getScrollPane().getVerticalScrollBar().getModel()) {
            final int value = leftScrollBar.getValue();
            leftContentPanel.getActionsScrollPane().getVerticalScrollBar().setValue(value);
            if (myScrollEvent) {
                return;
            }
            myScrollEvent = true;
        } else {
            final int value = rightScrollBar.getValue();
            rightContentPanel.getActionsScrollPane().getVerticalScrollBar().setValue(value);
            if (myScrollEvent) {
                return;
            }
            myScrollEvent = true;
            final boolean doSmartScroll;
            synchronized (smartScrollDisabled) {
                doSmartScroll = !smartScrollDisabled[0];
            }
            if (doSmartScroll) {
                smartScroll();
                master.updateCurrentDifference();
            }
        }
        master.getMyDivider().repaint();
        myScrollEvent = false;
    }

    /**
     * DOCUMENT ME!
     */
    public void scroll() {
        myScrollEvent = true;
        smartScroll();
        master.getMyDivider().repaint();
        myScrollEvent = false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    EditableDiffView getMaster() {
        return master;
    }
    /**
     * DOCUMENT ME!
     */
    private void updateDifferences() {
        assert EventQueue.isDispatchThread();
        final int mds = master.getDiffSerial();
        if ((mds <= cachedDiffSerial) && (rightContentPanel.getEditorPane().getSize().height == rightHeightCached)) {
            return;
        }
        rightHeightCached = rightContentPanel.getEditorPane().getSize().height;
        cachedDiffSerial = mds;
        computeDecorations();
        master.getEditorPane1().getLinesActions().repaint();
        master.getEditorPane2().getLinesActions().repaint();
        firstHilitesCached = secondHilitesCached = new HighLight[0];
        // interrupt running highlight scan and start new one outside of AWT
        highlightComputeTask.cancel();
        highlightComputeTask.schedule(0);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DecoratedDifference[] getDecorations() {
        if (EventQueue.isDispatchThread()) {
            updateDifferences();
        }
        return decorationsCached;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HighLight[] getSecondHighlights() {
        if (EventQueue.isDispatchThread()) {
            updateDifferences();
        }
        return secondHilitesCached;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HighLight[] getFirstHighlights() {
        if (EventQueue.isDispatchThread()) {
            updateDifferences();
        }
        return firstHilitesCached;
    }

    /**
     * DOCUMENT ME!
     */
    private void computeFirstHighlights() {
        final List<HighLight> hilites = new ArrayList<HighLight>();
        final Document doc = leftContentPanel.getEditorPane().getDocument();
        final DecoratedDifference[] decorations = decorationsCached;
        for (final DecoratedDifference dd : decorations) {
            if (Thread.interrupted()) {
                return;
            }
            final Difference diff = dd.getDiff();
            if (dd.getBottomLeft() == -1) {
                continue;
            }
            int start = getRowStartFromLineOffset(doc, diff.getFirstStart() - 1);
            if (isOneLineChange(diff)) {
                final CorrectRowTokenizer firstSt = new CorrectRowTokenizer(diff.getFirstText());
                final CorrectRowTokenizer secondSt = new CorrectRowTokenizer(diff.getSecondText());
                for (int i = diff.getSecondStart(); i <= diff.getSecondEnd(); i++) {
                    final String firstRow = firstSt.nextToken();
                    final String secondRow = secondSt.nextToken();
                    final List<HighLight> rowhilites = computeFirstRowHilites(start, firstRow, secondRow);
                    hilites.addAll(rowhilites);
                    start += firstRow.length() + 1;
                }
            } else {
                int end = getRowStartFromLineOffset(doc, diff.getFirstEnd());
                if (end == -1) {
                    end = doc.getLength();
                }
                final SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setBackground(attrs, master.getColor(diff));
                attrs.addAttribute(HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.TRUE);
                hilites.add(new HighLight(start, end, attrs));
            }
        }
        firstHilitesCached = hilites.toArray(new HighLight[hilites.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   doc        DOCUMENT ME!
     * @param   lineIndex  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    static int getRowStartFromLineOffset(final Document doc, final int lineIndex) {
        if (doc instanceof BaseDocument) {
            return Utilities.getRowStartFromLineOffset((BaseDocument)doc, lineIndex);
        } else {
            // TODO: find row start from line offet
            final Element element = doc.getDefaultRootElement();
            final Element line = element.getElement(lineIndex);
            return line.getStartOffset();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void computeSecondHighlights() {
        final List<HighLight> hilites = new ArrayList<HighLight>();
        final Document doc = rightContentPanel.getEditorPane().getDocument();
        final DecoratedDifference[] decorations = decorationsCached;
        for (final DecoratedDifference dd : decorations) {
            if (Thread.interrupted()) {
                return;
            }
            final Difference diff = dd.getDiff();
            if (dd.getBottomRight() == -1) {
                continue;
            }
            int start = getRowStartFromLineOffset(doc, diff.getSecondStart() - 1);
            if (isOneLineChange(diff)) {
                final CorrectRowTokenizer firstSt = new CorrectRowTokenizer(diff.getFirstText());
                final CorrectRowTokenizer secondSt = new CorrectRowTokenizer(diff.getSecondText());
                for (int i = diff.getSecondStart(); i <= diff.getSecondEnd(); i++) {
                    try {
                        final String firstRow = firstSt.nextToken();
                        final String secondRow = secondSt.nextToken();
                        final List<HighLight> rowhilites = computeSecondRowHilites(start, firstRow, secondRow);
                        hilites.addAll(rowhilites);
                        start += secondRow.length() + 1;
                    } catch (Exception e) {
                        //
                    }
                }
            } else {
                int end = getRowStartFromLineOffset(doc, diff.getSecondEnd());
                if (end == -1) {
                    end = doc.getLength();
                }
                final SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setBackground(attrs, master.getColor(diff));
                attrs.addAttribute(HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.TRUE);
                hilites.add(new HighLight(start, end, attrs));
            }
        }
        secondHilitesCached = hilites.toArray(new HighLight[hilites.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rowStart  DOCUMENT ME!
     * @param   left      DOCUMENT ME!
     * @param   right     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<HighLight> computeFirstRowHilites(final int rowStart, final String left, final String right) {
        final List<HighLight> hilites = new ArrayList<HighLight>(4);

        final String leftRows = wordsToRows(left);
        final String rightRows = wordsToRows(right);

        final DiffProvider diffprovider = Lookup.getDefault().lookup(DiffProvider.class);
        if (diffprovider == null) {
            return hilites;
        }

        final Difference[] diffs;
        try {
            diffs = diffprovider.computeDiff(new StringReader(leftRows), new StringReader(rightRows));
        } catch (IOException e) {
            return hilites;
        }

        // what we can hilite in first source
        for (final Difference diff : diffs) {
            if (diff.getType() == Difference.ADD) {
                continue;
            }
            final int start = rowOffset(leftRows, diff.getFirstStart());
            final int end = rowOffset(leftRows, diff.getFirstEnd() + 1);

            final SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setBackground(attrs, master.getColor(diff));
            hilites.add(new HighLight(rowStart + start, rowStart + end, attrs));
        }
        return hilites;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rowStart  DOCUMENT ME!
     * @param   left      DOCUMENT ME!
     * @param   right     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<HighLight> computeSecondRowHilites(final int rowStart, final String left, final String right) {
        final List<HighLight> hilites = new ArrayList<HighLight>(4);

        final String leftRows = wordsToRows(left);
        final String rightRows = wordsToRows(right);

        final DiffProvider diffprovider = Lookup.getDefault().lookup(DiffProvider.class);
        if (diffprovider == null) {
            return hilites;
        }

        final Difference[] diffs;
        try {
            diffs = diffprovider.computeDiff(new StringReader(leftRows), new StringReader(rightRows));
        } catch (IOException e) {
            return hilites;
        }

        // what we can hilite in second source
        for (final Difference diff : diffs) {
            if (diff.getType() == Difference.DELETE) {
                continue;
            }
            final int start = rowOffset(rightRows, diff.getSecondStart());
            final int end = rowOffset(rightRows, diff.getSecondEnd() + 1);

            final SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setBackground(attrs, master.getColor(diff));
            hilites.add(new HighLight(rowStart + start, rowStart + end, attrs));
        }
        return hilites;
    }

    /**
     * 1-based row index.
     *
     * @param   row       DOCUMENT ME!
     * @param   rowIndex  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int rowOffset(final String row, int rowIndex) {
        if (rowIndex == 1) {
            return 0;
        }
        int newLines = 0;
        for (int i = 0; i < row.length(); i++) {
            final char c = row.charAt(i);
            if (c == '\n') {
                newLines++;
                if (--rowIndex == 1) {
                    return i + 1 - newLines;
                }
            }
        }
        return row.length();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   s  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String wordsToRows(final String s) {
        final StringBuilder sb = new StringBuilder(s.length() * 2);
        final StringTokenizer st = new StringTokenizer(s, " \t\n[]{};:'\",.<>/?-=_+\\|~!@#$%^&*()", true); // NOI18N
        while (st.hasMoreTokens()) {
            final String token = st.nextToken();
            if (token.length() == 0) {
                continue;
            }
            sb.append(token);
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   diff  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isOneLineChange(final Difference diff) {
        return (diff.getType() == Difference.CHANGE)
                    && ((diff.getFirstEnd() - diff.getFirstStart()) == (diff.getSecondEnd() - diff.getSecondStart()));
    }

    /**
     * DOCUMENT ME!
     */
    private void computeDecorations() {
        final Document document = master.getEditorPane2().getEditorPane().getDocument();
        final View rootLeftView = Utilities.getDocumentView(leftContentPanel.getEditorPane());
        final View rootRightView = Utilities.getDocumentView(rightContentPanel.getEditorPane());
        if ((rootLeftView == null) || (rootRightView == null)) {
            return;
        }

        final Difference[] diffs = master.getDifferences();
        DecoratedDifference[] decorations = new DecoratedDifference[diffs.length];
        for (int i = 0; i < diffs.length; i++) {
            final Difference difference = diffs[i];
            final DecoratedDifference dd = new DecoratedDifference(difference, canRollback(document, difference));
            final Rectangle leftStartRect = getRectForView(leftContentPanel.getEditorPane(),
                    rootLeftView,
                    difference.getFirstStart()
                            - 1,
                    false);
            final Rectangle leftEndRect = getRectForView(leftContentPanel.getEditorPane(),
                    rootLeftView,
                    difference.getFirstEnd()
                            - 1,
                    true);
            final Rectangle rightStartRect = getRectForView(rightContentPanel.getEditorPane(),
                    rootRightView,
                    difference.getSecondStart()
                            - 1,
                    false);
            final Rectangle rightEndRect = getRectForView(rightContentPanel.getEditorPane(),
                    rootRightView,
                    difference.getSecondEnd()
                            - 1,
                    true);
            if ((leftStartRect == null) || (leftEndRect == null) || (rightStartRect == null)
                        || (rightEndRect == null)) {
                decorations = new DecoratedDifference[0];
                break;
            }
            if (difference.getType() == Difference.ADD) {
                dd.topRight = rightStartRect.y;
                dd.bottomRight = rightEndRect.y + rightEndRect.height;
                dd.topLeft = leftStartRect.y + leftStartRect.height;
                dd.floodFill = true;
            } else if (difference.getType() == Difference.DELETE) {
                dd.topLeft = leftStartRect.y;
                dd.bottomLeft = leftEndRect.y + leftEndRect.height;
                dd.topRight = rightStartRect.y + rightStartRect.height;
                dd.floodFill = true;
            } else {
                dd.topRight = rightStartRect.y;
                dd.bottomRight = rightEndRect.y + rightEndRect.height;
                dd.topLeft = leftStartRect.y;
                dd.bottomLeft = leftEndRect.y + leftEndRect.height;
                dd.floodFill = true;
            }
            decorations[i] = dd;
        }
        decorationsCached = decorations;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   comp        DOCUMENT ME!
     * @param   rootView    DOCUMENT ME!
     * @param   lineNumber  DOCUMENT ME!
     * @param   endOffset   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Rectangle getRectForView(final JTextComponent comp,
            final View rootView,
            final int lineNumber,
            final boolean endOffset) {
        if ((lineNumber == -1) || (lineNumber >= rootView.getViewCount())) {
            return new Rectangle();
        }
        Rectangle rect = null;
        final View view = rootView.getView(lineNumber);
        try {
            rect = (view == null) ? null
                                  : comp.modelToView(endOffset ? (view.getEndOffset() - 1) : view.getStartOffset());
        } catch (BadLocationException ex) {
            //
        }
        return rect;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   doc   DOCUMENT ME!
     * @param   diff  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean canRollback(final Document doc, final Difference diff) {
        if (!(doc instanceof GuardedDocument)) {
            return true;
        }
        final GuardedDocument document = (GuardedDocument)doc;
        int start;
        int end;
        if (diff.getType() == Difference.DELETE) {
            start = end = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart());
        } else {
            start = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
            end = Utilities.getRowStartFromLineOffset(document, diff.getSecondEnd());
        }
        final MarkBlockChain mbc = ((GuardedDocument)document).getGuardedBlockChain();
        return (mbc.compareBlock(start, end) & MarkBlock.OVERLAP) == 0;
    }

    /**
     * 1. find the difference whose top (first line) is closest to the center of the screen. If there is no difference
     * on screen, proceed to #5 2. find line offset of the found difference in the other document 3. scroll the other
     * document so that the difference starts on the same visual line
     *
     * <p>5. scroll the other document proportionally</p>
     */
    private void smartScroll() {
        final DiffContentPanel rightPane = master.getEditorPane2();
        final DiffContentPanel leftPane = master.getEditorPane1();

        final int[] map = scrollMap.getScrollMap(rightPane.getEditorPane().getSize().height, master.getDiffSerial());

        final int rightOffet = rightPane.getScrollPane().getVerticalScrollBar().getValue();
        if (rightOffet >= map.length) {
            return;
        }
        leftPane.getScrollPane().getVerticalScrollBar().setValue(map[rightOffet]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   differenceMatchStart  DOCUMENT ME!
     * @param   rightOffset           DOCUMENT ME!
     * @param   positions             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int computeLeftOffsetToMatchDifference(final DifferencePosition differenceMatchStart,
            final int rightOffset,
            final Rectangle[] positions) {
        final Rectangle leftStartRect = positions[0];
        final Rectangle leftEndRect = positions[1];
        final Rectangle rightStartRect = positions[2];
        final Rectangle rightEndRect = positions[3];
        final Difference diff = differenceMatchStart.getDiff();
        final boolean matchStart = differenceMatchStart.isStart();

        int value;
        int valueSecond;
        if (matchStart) {
            value = leftStartRect.y + leftStartRect.height;                 // kde zacina prva, 180
            valueSecond = rightStartRect.y + rightStartRect.height;         // kde by zacinala druha, napr. 230
        } else {
            if (diff.getType() == Difference.ADD) {
                value = leftStartRect.y;                                    // kde zacina prva, 180
                valueSecond = rightStartRect.y + rightStartRect.height;     // kde by zacinala druha, napr. 230
            } else {
                value = leftEndRect.y + leftEndRect.height;                 // kde zacina prva, 180
                if (diff.getType() == Difference.DELETE) {
                    value += leftEndRect.height;
                    valueSecond = rightStartRect.y + rightStartRect.height; // kde by zacinala druha, napr. 230
                } else {
                    valueSecond = rightEndRect.y + rightEndRect.height;     // kde by zacinala druha, napr. 230
                }
            }
        }

        // druha je na 400
        final int secondOffset = rightOffset - valueSecond;

        value += secondOffset;
        if (diff.getType() == Difference.ADD) {
            value += rightStartRect.height;
        }
        if (diff.getType() == Difference.DELETE) {
            value -= leftStartRect.height;
        }

        return value;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rightOffset          DOCUMENT ME!
     * @param   rightViewportHeight  DOCUMENT ME!
     * @param   diffs                DOCUMENT ME!
     * @param   index                DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int findDifferenceToMatch(final int rightOffset,
            final int rightViewportHeight,
            final DecoratedDifference[] diffs,
            int index) {
        int candidateIndex = -1;
        // start the loop with the last used index, it will speed-up things
        for (; index < diffs.length; ++index) {
            final DecoratedDifference dd = diffs[index];
            if (dd.getTopRight() > (rightOffset + rightViewportHeight)) {
                break;
            }
            if (dd.getBottomRight() != -1) {
                if (dd.getBottomRight() <= rightOffset) {
                    continue;
                }
            } else {
                if (dd.getTopRight() <= rightOffset) {
                    continue;
                }
            }
            if (candidateIndex > -1) {
                final DecoratedDifference candidate = diffs[candidateIndex];
                if (candidate.getDiff().getType() == Difference.DELETE) {
                    candidateIndex = index;
                } else if (candidate.getTopRight() < rightOffset) {
                    candidateIndex = index;
                } else if (dd.getTopRight() <= (rightOffset + (rightViewportHeight / 2))) {
                    candidateIndex = index;
                }
            } else {
                candidateIndex = index;
            }
        }
        return candidateIndex;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getScrollFactor() {
        final BoundedRangeModel m1 = leftContentPanel.getScrollPane().getVerticalScrollBar().getModel();
        final BoundedRangeModel m2 = rightContentPanel.getScrollPane().getVerticalScrollBar().getModel();
        return ((double)m1.getMaximum() - m1.getExtent()) / (m2.getMaximum() - m2.getExtent());
    }

    /**
     * The split pane needs to be repainted along with editor.
     *
     * @param  decoratedEditorPane  the pane that is currently repainting
     */
    void editorPainting(final DecoratedEditorPane decoratedEditorPane) {
        if (!decoratedEditorPane.isFirst()) {
            final JComponent mydivider = master.getMyDivider();
            mydivider.paint(mydivider.getGraphics());
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class DifferencePosition {

        //~ Instance fields ----------------------------------------------------

        private Difference diff;
        private boolean isStart;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DifferencePosition object.
         *
         * @param  diff   DOCUMENT ME!
         * @param  start  DOCUMENT ME!
         */
        public DifferencePosition(final Difference diff, final boolean start) {
            this.diff = diff;
            isStart = start;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Difference getDiff() {
            return diff;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isStart() {
            return isStart;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class DecoratedDifference {

        //~ Instance fields ----------------------------------------------------

        private final Difference diff;
        private final boolean canRollback;
        private int topLeft;          // top line in the left pane
        private int bottomLeft = -1;  // bottom line in the left pane, -1 for ADDs
        private int topRight;
        private int bottomRight = -1; // bottom line in the right pane, -1 for DELETEs
        private boolean floodFill;    // should the whole difference be highlited

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DecoratedDifference object.
         *
         * @param  difference   DOCUMENT ME!
         * @param  canRollback  DOCUMENT ME!
         */
        public DecoratedDifference(final Difference difference, final boolean canRollback) {
            diff = difference;
            this.canRollback = canRollback;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean canRollback() {
            return canRollback;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Difference getDiff() {
            return diff;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getTopLeft() {
            return topLeft;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getBottomLeft() {
            return bottomLeft;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getTopRight() {
            return topRight;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getBottomRight() {
            return bottomRight;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isFloodFill() {
            return floodFill;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class HighLight {

        //~ Instance fields ----------------------------------------------------

        private final int startOffset;
        private final int endOffset;
        private final AttributeSet attrs;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new HighLight object.
         *
         * @param  startOffset  DOCUMENT ME!
         * @param  endOffset    DOCUMENT ME!
         * @param  attrs        DOCUMENT ME!
         */
        public HighLight(final int startOffset, final int endOffset, final AttributeSet attrs) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.attrs = attrs;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getStartOffset() {
            return startOffset;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getEndOffset() {
            return endOffset;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public AttributeSet getAttrs() {
            return attrs;
        }
    }

    /**
     * Java StringTokenizer does not work if the very first character is a delimiter.
     *
     * @version  $Revision$, $Date$
     */
    private static class CorrectRowTokenizer {

        //~ Instance fields ----------------------------------------------------

        private final String s;
        private int idx;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CorrectRowTokenizer object.
         *
         * @param  s  DOCUMENT ME!
         */
        public CorrectRowTokenizer(final String s) {
            this.s = s;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String nextToken() {
            String token = null;
            for (int end = idx; end < s.length(); end++) {
                if (s.charAt(end) == '\n') {
                    token = s.substring(idx, end);
                    idx = end + 1;
                    break;
                }
            }
            return token;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ScrollMapCached {

        //~ Instance fields ----------------------------------------------------

        private int rightPanelHeightCached;
        private int[] scrollMapCached;
        private int diffSerialCached;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   rightPanelHeight  DOCUMENT ME!
         * @param   diffSerial        DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public synchronized int[] getScrollMap(final int rightPanelHeight, final int diffSerial) {
            if ((rightPanelHeight != rightPanelHeightCached) || (diffSerialCached != diffSerial)
                        || (scrollMapCached == null)) {
                diffSerialCached = diffSerial;
                rightPanelHeightCached = rightPanelHeight;
                scrollMapCached = compute();
            }
            return scrollMapCached;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private int[] compute() {
            final DiffContentPanel rightPane = master.getEditorPane2();

            final int rightViewportHeight = rightPane.getScrollPane().getViewport().getViewRect().height;

            int[] scrollMap = new int[rightPanelHeightCached];

            final EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(leftContentPanel.getEditorPane());
            if (editorUI == null) {
                return scrollMap;
            }

            int lastOffset = 0;
            final View rootLeftView = Utilities.getDocumentView(leftContentPanel.getEditorPane());
            final View rootRightView = Utilities.getDocumentView(rightContentPanel.getEditorPane());
            if ((rootLeftView == null) || (rootRightView == null)) {
                return scrollMap;
            }
            final HashMap<Difference, Rectangle[]> positionsPerDiff = new HashMap<Difference, Rectangle[]>(
                    getDecorations().length);

            final DecoratedDifference[] diffs = getDecorations();
            int lastDiffIndex = 0;
            for (int rightOffset = 0; rightOffset < rightPanelHeightCached; rightOffset += 5) { // count position for
                                                                                                // every fifth pix,
                                                                                                // others are
                                                                                                // linearly
                                                                                                // interpolated
                DifferencePosition dpos = null;
                int leftOffset;
                // find diff for every fifth pix
                final int candidateIndex = (diffs.length == 0)
                    ? -1 : findDifferenceToMatch(rightOffset, rightViewportHeight, diffs, lastDiffIndex);
                if (candidateIndex > -1) {
                    lastDiffIndex = candidateIndex;
                    final DecoratedDifference candidate = diffs[candidateIndex];
                    boolean matchStart = candidate.getTopRight() > (rightOffset + (rightViewportHeight / 2));
                    if ((candidate.getDiff().getType() == Difference.DELETE)
                                && (candidate.getTopRight() < (rightOffset + (rightViewportHeight * 4 / 5)))) {
                        matchStart = false;
                    }
                    if ((candidate.getDiff().getType() == Difference.DELETE)
                                && (candidate == diffs[diffs.length - 1])) {
                        matchStart = false;
                    }
                    dpos = new DifferencePosition(candidate.getDiff(), matchStart);
                }

                if (dpos == null) {
                    leftOffset = lastOffset + rightOffset;
                } else {
                    final Difference diff = dpos.getDiff();
                    Rectangle[] positions = positionsPerDiff.get(diff);
                    if (positions == null) {
                        positions = new Rectangle[4];
                        positions[0] = getRectForView(leftContentPanel.getEditorPane(),
                                rootLeftView,
                                diff.getFirstStart()
                                        - 1,
                                false);
                        positions[1] = getRectForView(leftContentPanel.getEditorPane(),
                                rootLeftView,
                                diff.getFirstEnd()
                                        - 1,
                                true);
                        positions[2] = getRectForView(rightContentPanel.getEditorPane(),
                                rootRightView,
                                diff.getSecondStart()
                                        - 1,
                                false);
                        positions[3] = getRectForView(rightContentPanel.getEditorPane(),
                                rootRightView,
                                diff.getSecondEnd()
                                        - 1,
                                true);
                        positionsPerDiff.put(diff, positions);
                    }
                    leftOffset = computeLeftOffsetToMatchDifference(dpos, rightOffset, positions);
                    lastOffset = leftOffset - rightOffset;
                }
                // now try to interpolate for next 4 positions
                final int maxIndex = Math.min(rightPanelHeightCached - rightOffset, 5);
                for (int i = 0; i < maxIndex; ++i) {
                    scrollMap[rightOffset + i] = leftOffset + i;
                }
            }
            scrollMap = smooth(scrollMap);
            return scrollMap;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   map  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private int[] smooth(final int[] map) {
            final int[] newMap = new int[map.length];
            int leftShift = 0;
            float correction = 0.0f;
            for (int i = 0; i < map.length; i++) {
                final int leftOffset = map[i];
                final int requestedShift = leftOffset - i;
                if (requestedShift > leftShift) {
                    if (correction > (requestedShift - leftShift)) {
                        correction = requestedShift - leftShift;
                    }
                    leftShift += correction;
                    correction += 0.02f;
                } else if (requestedShift < leftShift) {
                    leftShift -= 1;
                } else {
                    correction = 1.0f;
                }
                newMap[i] = i + leftShift;
            }
            return newMap;
        }
    }

    /**
     * Counts differences for rows.
     *
     * @version  $Revision$, $Date$
     */
    private class HighlightsComputeTask implements Runnable {

        //~ Instance fields ----------------------------------------------------

        private int diffSerial;

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            diffSerial = cachedDiffSerial;
            computeSecondHighlights();
            if (diffSerial != cachedDiffSerial) {
                return;
            }
            computeFirstHighlights();
            if (diffSerial == cachedDiffSerial) {
                EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            master.getEditorPane1().fireHilitingChanged();
                            master.getEditorPane2().fireHilitingChanged();
                        }
                    });
            }
        }
    }
}
