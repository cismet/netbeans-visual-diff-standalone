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
package org.netbeans.modules.diff.builtin.visualizer;

import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.builtin.Hunk;

import java.io.*;

/**
 * Unified diff engine.
 *
 * @author   Maros Sandor
 * @version  $Revision$, $Date$
 */
final class UnifiedDiff {

    //~ Instance fields --------------------------------------------------------

    private final TextDiffVisualizer.TextDiffInfo diffInfo;
    private BufferedReader baseReader;
    private BufferedReader modifiedReader;
    private final String newline;
    private boolean baseEndsWithNewline;
    private boolean modifiedEndsWithNewline;

    private int currentBaseLine;
    private int currentModifiedLine;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UnifiedDiff object.
     *
     * @param  diffInfo  DOCUMENT ME!
     */
    public UnifiedDiff(final TextDiffVisualizer.TextDiffInfo diffInfo) {
        this.diffInfo = diffInfo;
        currentBaseLine = 1;
        currentModifiedLine = 1;
        this.newline = System.getProperty("line.separator");
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public String computeDiff() throws IOException {
        baseReader = checkEndingNewline(diffInfo.createFirstReader(), true);
        modifiedReader = checkEndingNewline(diffInfo.createSecondReader(), false);

        final StringBuilder buffer = new StringBuilder();
        buffer.append("--- ");
        buffer.append(diffInfo.getName1());
        buffer.append(newline);
        buffer.append("+++ ");
        buffer.append(diffInfo.getName2());
        buffer.append(newline);

        final Difference[] diffs = diffInfo.getDifferences();

        //J-
        for (int currentDifference = 0; currentDifference < diffs.length;) {
            // the new hunk will span differences from currentDifference to lastDifference (exclusively)
            final int lastDifference = getLastIndex(currentDifference);
            final Hunk hunk = computeHunk(currentDifference, lastDifference);
            dumpHunk(buffer, hunk);
            currentDifference = lastDifference;
        }
        //J+

        return buffer.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   reader  DOCUMENT ME!
     * @param   isBase  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private BufferedReader checkEndingNewline(final Reader reader, final boolean isBase) throws IOException {
        final StringWriter sw = new StringWriter();
        copyStreamsCloseAll(sw, reader);
        final String s = sw.toString();
        final char endingChar = (s.length() == 0) ? 0 : s.charAt(s.length() - 1);
        if (isBase) {
            baseEndsWithNewline = (endingChar == '\n') || (endingChar == '\r');
        } else {
            modifiedEndsWithNewline = (endingChar == '\n') || (endingChar == '\r');
        }
        return new BufferedReader(new StringReader(s));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   firstDifference  DOCUMENT ME!
     * @param   lastDifference   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private Hunk computeHunk(final int firstDifference, final int lastDifference) throws IOException {
        final Hunk hunk = new Hunk();

        final Difference firstDiff = diffInfo.getDifferences()[firstDifference];
        int contextLines = diffInfo.getContextNumLines();

        int skipLines;
        if (firstDiff.getType() == Difference.ADD) {
            if (contextLines > firstDiff.getFirstStart()) {
                contextLines = firstDiff.getFirstStart();
            }
            skipLines = firstDiff.getFirstStart() - contextLines - currentBaseLine + 1;
        } else {
            if (contextLines >= firstDiff.getFirstStart()) {
                contextLines = firstDiff.getFirstStart() - 1;
            }
            skipLines = firstDiff.getFirstStart() - contextLines - currentBaseLine;
        }

        // move file pointers to the beginning of hunk
        while (skipLines-- > 0) {
            readLine(baseReader);
            readLine(modifiedReader);
        }

        hunk.baseStart = currentBaseLine;
        hunk.modifiedStart = currentModifiedLine;

        // output differences with possible contextual lines in-between
        for (int i = firstDifference; i < lastDifference; i++) {
            final Difference diff = diffInfo.getDifferences()[i];
            writeContextLines(
                hunk,
                diff.getFirstStart()
                        - currentBaseLine
                        + ((diff.getType() == Difference.ADD) ? 1 : 0));

            if (diff.getFirstEnd() > 0) {
                final int n = diff.getFirstEnd() - diff.getFirstStart() + 1;
                outputLines(hunk, baseReader, "-", n);
                hunk.baseCount += n;
                if (!baseEndsWithNewline && (i == (diffInfo.getDifferences().length - 1))
                            && (diff.getFirstEnd() == (currentBaseLine - 1))) {
                    hunk.lines.add(Hunk.ENDING_NEWLINE);
                }
            }
            if (diff.getSecondEnd() > 0) {
                final int n = diff.getSecondEnd() - diff.getSecondStart() + 1;
                outputLines(hunk, modifiedReader, "+", n);
                hunk.modifiedCount += n;
                if (!modifiedEndsWithNewline && (i == (diffInfo.getDifferences().length - 1))
                            && (diff.getSecondEnd() == (currentModifiedLine - 1))) {
                    hunk.lines.add(Hunk.ENDING_NEWLINE);
                }
            }
        }

        // output bottom context lines
        writeContextLines(hunk, diffInfo.getContextNumLines());

        if (hunk.modifiedCount == 0) {
            hunk.modifiedStart = 0; // empty file
        }
        if (hunk.baseCount == 0) {
            hunk.baseStart = 0;     // empty file
        }
        return hunk;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   hunk   DOCUMENT ME!
     * @param   count  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private void writeContextLines(final Hunk hunk, int count) throws IOException {
        while (count-- > 0) {
            final String line = readLine(baseReader);
            if (line == null) {
                return;
            }
            hunk.lines.add(" " + line);
            readLine(modifiedReader); // move the modified file pointer as well
            hunk.baseCount++;
            hunk.modifiedCount++;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   reader  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private String readLine(final BufferedReader reader) throws IOException {
        final String s = reader.readLine();
        if (s != null) {
            if (reader == baseReader) {
                currentBaseLine++;
            }
            if (reader == modifiedReader) {
                currentModifiedLine++;
            }
        }
        return s;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   hunk    DOCUMENT ME!
     * @param   reader  DOCUMENT ME!
     * @param   mode    DOCUMENT ME!
     * @param   n       DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private void outputLines(final Hunk hunk, final BufferedReader reader, final String mode, int n)
            throws IOException {
        while (n-- > 0) {
            final String line = readLine(reader);
            hunk.lines.add(mode + line);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   firstIndex  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getLastIndex(int firstIndex) {
        final int contextLines = diffInfo.getContextNumLines() * 2;
        final Difference[] diffs = diffInfo.getDifferences();
        for (++firstIndex; firstIndex < diffs.length; firstIndex++) {
            final Difference prevDiff = diffs[firstIndex - 1];
            final Difference currentDiff = diffs[firstIndex];
            final int prevEnd = 1
                        + ((prevDiff.getType() == Difference.ADD) ? prevDiff.getFirstStart() : prevDiff.getFirstEnd());
            final int curStart = (currentDiff.getType() == Difference.ADD) ? (currentDiff.getFirstStart() + 1)
                                                                           : currentDiff.getFirstStart();
            if ((curStart - prevEnd) > contextLines) {
                break;
            }
        }
        return firstIndex;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  buffer  DOCUMENT ME!
     * @param  hunk    DOCUMENT ME!
     */
    private void dumpHunk(final StringBuilder buffer, final Hunk hunk) {
        buffer.append("@@ -");
        buffer.append(Integer.toString(hunk.baseStart));
        if (hunk.baseCount != 1) {
            buffer.append(",");
            buffer.append(Integer.toString(hunk.baseCount));
        }
        buffer.append(" +");
        buffer.append(Integer.toString(hunk.modifiedStart));
        if (hunk.modifiedCount != 1) {
            buffer.append(",");
            buffer.append(Integer.toString(hunk.modifiedCount));
        }
        buffer.append(" @@");
        buffer.append(newline);
        for (final String line : hunk.lines) {
            buffer.append(line);
            buffer.append(newline);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   writer  DOCUMENT ME!
     * @param   reader  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private static void copyStreamsCloseAll(final Writer writer, final Reader reader) throws IOException {
        final char[] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }
}
