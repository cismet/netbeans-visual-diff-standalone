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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.diff.builtin;

import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.cmdline.CmdlineDiffProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Utility class for patch application.
 *
 * @author   Martin Entlicher
 * @version  $Revision$, $Date$
 */
public class Patch extends Reader {

    //~ Static fields/initializers ---------------------------------------------

    private static final int CONTEXT_DIFF = 0;
    private static final int NORMAL_DIFF = 1;
    private static final int UNIFIED_DIFF = 2;

    private static final String CONTEXT_MARK1B = "*** ";
//    private static final String CONTEXT_MARK1E = " ****";
    private static final String CONTEXT_MARK2B = "--- ";
//    private static final String CONTEXT_MARK2E = " ----";
    private static final String CONTEXT_MARK_DELIMETER = ",";
    private static final String DIFFERENCE_DELIMETER = "***************";
//    private static final String LINE_PREP = "  ";
    private static final String LINE_PREP_ADD = "+ ";
    private static final String LINE_PREP_REMOVE = "- ";
    private static final String LINE_PREP_CHANGE = "! ";

    private static final String UNIFIED_MARK = "@@";
    private static final String UNIFIED_MARK1 = "--- ";
//    private static final String UNIFIED_MARK2 = "+++ ";
    private static final String LINE_PREP_UNIF_ADD = "+";
    private static final String LINE_PREP_UNIF_REMOVE = "-";
//    private static final String LINE_PREP_UNIF_CHANGE = null;

    //~ Instance fields --------------------------------------------------------

    private Difference[] diffs;
    private PushbackReader source;
    private int currDiff = 0;
    private int line = 1;
    private String newLine = null; // String, that is used to separate lines
    private StringBuffer buff = new StringBuffer();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of Patch.
     *
     * @param  diffs   DOCUMENT ME!
     * @param  source  DOCUMENT ME!
     */
    private Patch(final Difference[] diffs, final Reader source) {
        this.diffs = diffs;
        this.source = new PushbackReader(new BufferedReader(source), 1);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Apply the patch to the source.
     *
     * @param   diffs   The differences to patch
     * @param   source  The source stream
     *
     * @return  The patched stream
     */
    public static Reader apply(final Difference[] diffs, final Reader source) { // throws IOException, ParseException {
        return new Patch(diffs, source);
    }

    /**
     * Parse the differences.
     *
     * <p>public static Difference[] parse(Reader source) throws IOException { return parseContextDiff(source); }</p>
     *
     * @param   source  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */

    /**
     * Parse the differences and corresponding file names.
     *
     * @param   source  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static FileDifferences[] parse(final Reader source) throws IOException {
        final List<FileDifferences> fileDifferences = new ArrayList<FileDifferences>();
        // int pushBackLimit = DIFFERENCE_DELIMETER.length();
        // PushbackReader recognizedSource = new PushbackReader(source, pushBackLimit);
        final Patch.SinglePatchReader patchReader = new Patch.SinglePatchReader(source);
        final int[] diffType = new int[1];
        final String[] fileName = new String[2];
        while (patchReader.hasNextPatch(diffType, fileName)) {
            // System.out.println("Have a next patch of name '"+fileName[0]+"'");
            Difference[] diffs = null;
            switch (diffType[0]) {
                case CONTEXT_DIFF: {
                    diffs = parseContextDiff(patchReader);
                    break;
                }
                case UNIFIED_DIFF: {
                    diffs = parseUnifiedDiff(patchReader);
                    break;
                }
                case NORMAL_DIFF: {
                    diffs = parseNormalDiff(patchReader);
                    break;
                }
            }
            if (diffs != null) {
                fileDifferences.add(new FileDifferences(fileName[0], fileName[1], diffs));
            }
        }
        return fileDifferences.toArray(new FileDifferences[fileDifferences.size()]);
    }

    @Override
    public int read(final char[] cbuf, final int off, final int length) throws java.io.IOException {
        if (buff.length() < length) {
            doRetrieve(length - buff.length());
        }
        final int ret = Math.min(buff.length(), length);
        if (ret == 0) {
            return -1;
        }
        final String retStr = buff.substring(0, ret);
        final char[] retChars = retStr.toCharArray();
        System.arraycopy(retChars, 0, cbuf, off, ret);
        buff.delete(0, ret);
        return ret;
    }

    @Override
    public void close() throws java.io.IOException {
        if (currDiff < diffs.length) {
            throw new IOException("There are " + (diffs.length - currDiff) + " pending hunks!");
        }
        source.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   length  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private void doRetrieve(final int length) throws IOException {
        for (final int size = 0; size < length; line++) {
            if ((currDiff < diffs.length)
                        && (((Difference.ADD == diffs[currDiff].getType())
                                && (line == (diffs[currDiff].getFirstStart() + 1)))
                            || ((Difference.ADD != diffs[currDiff].getType())
                                && (line == diffs[currDiff].getFirstStart())))) {
                if (compareText(source, diffs[currDiff].getFirstText())) {
                    final String text = convertNewLines(diffs[currDiff].getSecondText(), newLine);
                    buff.append(text);
                    currDiff++;
                } else {
                    throw new IOException("Patch not applicable.");
                }
            }
            StringBuffer newLineBuffer = null;
            if (newLine == null) {
                newLineBuffer = new StringBuffer();
            }
            final String lineStr = readLine(source, newLineBuffer);
            if (newLineBuffer != null) {
                newLine = newLineBuffer.toString();
            }
            if (lineStr == null) {
                break;
            }
            buff.append(lineStr);
            buff.append(newLine);
        }
    }

    /**
     * Reads a line and returns the char sequence for newline.
     *
     * @param   r   DOCUMENT ME!
     * @param   nl  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private static String readLine(final PushbackReader r, final StringBuffer nl) throws IOException {
        final StringBuffer line = new StringBuffer();
        int ic = r.read();
        if (ic == -1) {
            return null;
        }
        char c = (char)ic;
        while ((c != '\n') && (c != '\r')) {
            line.append(c);
            ic = r.read();
            if (ic == -1) {
                break;
            }
            c = (char)ic;
        }
        if (nl != null) {
            nl.append(c);
        }
        if (c == '\r') {
            try {
                ic = r.read();
                if (ic != -1) {
                    c = (char)ic;
                    if (c != '\n') {
                        r.unread(c);
                    } else if (nl != null) {
                        nl.append(c);
                    }
                }
            } catch (IOException ioex) {
            }
        }
        return line.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   text     DOCUMENT ME!
     * @param   newLine  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String convertNewLines(final String text, final String newLine) {
        if (text == null) {
            return ""; // NOI18N
        }
        if (newLine == null) {
            return text;
        }
        final StringBuffer newText = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (c == '\n') {
                newText.append(newLine);
            } else if (c == '\r') {
                if (((i + 1) < text.length()) && (text.charAt(i + 1) == '\n')) {
                    i++;
                    newText.append(newLine);
                }
            } else {
                newText.append(c);
            }
        }
        return newText.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   source  DOCUMENT ME!
     * @param   text    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private boolean compareText(final PushbackReader source, String text) throws IOException {
        if ((text == null) || (text.length() == 0)) {
            return true;
        }
        text = adjustTextNL(text);
        final char[] chars = new char[text.length()];
        int pos = 0;
        //J-
        int n;
        //J+
        String readStr = "";
        do {
            n = source.read(chars, 0, chars.length - pos);
            if (n > 0) {
                pos += n;
                readStr = readStr + new String(chars, 0, n);
            }
            if (readStr.endsWith("\r")) {
                try {
                    final char c = (char)source.read();
                    if (c != '\n') {
                        source.unread(c);
                    } else {
                        readStr += c;
                    }
                } catch (IOException ioex) {
                }
            }
            readStr = adjustTextNL(readStr);
            pos = readStr.length();
        } while ((n > 0) && (pos < chars.length));
        readStr.getChars(0, readStr.length(), chars, 0);
        line += numChars('\n', chars);
        // System.out.println("Comparing text of the diff:\n'"+text+"'\nWith the read text:\n'"+readStr+"'\n");
        // System.out.println("  EQUALS = "+readStr.equals(text));
        return readStr.equals(text);
    }

    /**
     * When comparing the two texts, it's important to ignore different line endings. This method assures, that only
     * '\n' is used as the line ending.
     *
     * @param   text  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String adjustTextNL(String text) {
        text = text.replace("\r\n", "\n");
        text = text.replace("\n\r", "\n");
        text = text.replace("\r", "\n");
        return text;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c      DOCUMENT ME!
     * @param   chars  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static int numChars(final char c, final char[] chars) {
        int n = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == c) {
                n++;
            }
        }
        return n;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   in  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private static Difference[] parseContextDiff(final Reader in) throws IOException {
        final BufferedReader br = new BufferedReader(in);
        final ArrayList<Difference> diffs = new ArrayList<Difference>();
        String line = null;
        do {
            if ((line == null) || !DIFFERENCE_DELIMETER.equals(line)) {
                do {
                    line = br.readLine();
                } while ((line != null) && !DIFFERENCE_DELIMETER.equals(line));
            }
            final int[] firstInterval = new int[2];
            line = br.readLine();
            if ((line != null) && line.startsWith(CONTEXT_MARK1B)) {
                try {
                    readNums(line, CONTEXT_MARK1B.length(), firstInterval);
                } catch (NumberFormatException nfex) {
                    throw new IOException(nfex.getLocalizedMessage());
                }
            } else {
                continue;
            }
            final ArrayList<Object> firstChanges = new ArrayList<Object>();                                  // List of intervals and texts
            line = fillChanges(firstInterval, br, CONTEXT_MARK2B, firstChanges);
            final int[] secondInterval = new int[2];
            if ((line != null) && line.startsWith(CONTEXT_MARK2B)) {
                try {
                    readNums(line, CONTEXT_MARK2B.length(), secondInterval);
                } catch (NumberFormatException nfex) {
                    throw new IOException(nfex.getLocalizedMessage());
                }
            } else {
                continue;
            }
            final ArrayList<Object> secondChanges = new ArrayList<Object>();                                 // List of intervals and texts
            line = fillChanges(secondInterval, br, DIFFERENCE_DELIMETER, secondChanges);
            if (changesCountInvariant(firstChanges, secondChanges) == false) {
                throw new IOException(
                    "Diff file format error. Number of new and old file changes in one hunk must be same!"); // NOI18N
            }
            mergeChanges(firstInterval, secondInterval, firstChanges, secondChanges, diffs);
        } while (line != null);
        return diffs.toArray(new Difference[diffs.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   changes1  DOCUMENT ME!
     * @param   changes2  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static boolean changesCountInvariant(final List<Object> changes1, final List<Object> changes2) { // both are Union<int[],String>
        int i1 = 0;
        Iterator it = changes1.iterator();
        while (it.hasNext()) {
            final int[] ints = (int[])it.next();
            if (ints[2] == 2) {
                i1++;
            }
            final String skip = (String)it.next();
        }

        int i2 = 0;
        it = changes2.iterator();
        while (it.hasNext()) {
            final int[] ints = (int[])it.next();
            if (ints[2] == 2) {
                i2++;
            }
            final String skip = (String)it.next();
        }

        return i1 == i2;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   str     DOCUMENT ME!
     * @param   off     DOCUMENT ME!
     * @param   values  DOCUMENT ME!
     *
     * @throws  NumberFormatException  DOCUMENT ME!
     */
    private static void readNums(final String str, int off, final int[] values) throws NumberFormatException {
        int end = str.indexOf(CONTEXT_MARK_DELIMETER, off);
        if (end > 0) {
            values[0] = Integer.parseInt(str.substring(off, end).trim());
        } else {
            throw new NumberFormatException("Missing comma.");
        }
        off = end + 1;
        end = str.indexOf(' ', off);
        if (end > 0) {
            values[1] = Integer.parseInt(str.substring(off, end).trim());
        } else {
            throw new NumberFormatException("Missing final space.");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interval         DOCUMENT ME!
     * @param   br               DOCUMENT ME!
     * @param   untilStartsWith  DOCUMENT ME!
     * @param   changes          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private static String fillChanges(final int[] interval,
            final BufferedReader br,
            final String untilStartsWith,
            final List<Object /* int[3] or String*/> changes) throws IOException {
        String line = br.readLine();
        for (int pos = interval[0]; pos <= interval[1]; pos++) {
            if ((line == null) || line.startsWith(untilStartsWith)) {
                break;
            }
            if (line.startsWith(LINE_PREP_ADD)) {
                final int[] changeInterval = new int[3];
                changeInterval[0] = pos;
                changeInterval[2] = Difference.ADD;
                final StringBuffer changeText = new StringBuffer();
                changeText.append(line.substring(LINE_PREP_ADD.length()));
                changeText.append('\n');
                do {
                    line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.startsWith(LINE_PREP_ADD)) {
                        changeText.append(line.substring(LINE_PREP_ADD.length()));
                        changeText.append('\n');
                    } else {
                        break;
                    }
                    pos++;
                } while (true);
                changeInterval[1] = pos;
                changes.add(changeInterval);
                changes.add(changeText.toString());
            } else if (line.startsWith(LINE_PREP_REMOVE)) {
                final int[] changeInterval = new int[3];
                changeInterval[0] = pos;
                changeInterval[2] = Difference.DELETE;
                final StringBuffer changeText = new StringBuffer();
                changeText.append(line.substring(LINE_PREP_REMOVE.length()));
                changeText.append('\n');
                do {
                    line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.startsWith(LINE_PREP_REMOVE)) {
                        changeText.append(line.substring(LINE_PREP_REMOVE.length()));
                        changeText.append('\n');
                    } else {
                        break;
                    }
                    pos++;
                } while (true);
                changeInterval[1] = pos;
                changes.add(changeInterval);
                changes.add(changeText.toString());
            } else if (line.startsWith(LINE_PREP_CHANGE)) {
                final int[] changeInterval = new int[3];
                changeInterval[0] = pos;
                changeInterval[2] = Difference.CHANGE;
                final StringBuffer changeText = new StringBuffer();
                changeText.append(line.substring(LINE_PREP_CHANGE.length()));
                changeText.append('\n');
                do {
                    line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.startsWith(LINE_PREP_CHANGE)) {
                        changeText.append(line.substring(LINE_PREP_CHANGE.length()));
                        changeText.append('\n');
                    } else {
                        break;
                    }
                    pos++;
                } while (true);
                changeInterval[1] = pos;
                changes.add(changeInterval);
                changes.add(changeText.toString());
            } else {
                line = br.readLine();
            }
        }
        return line;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  firstInterval   DOCUMENT ME!
     * @param  secondInterval  DOCUMENT ME!
     * @param  firstChanges    DOCUMENT ME!
     * @param  secondChanges   DOCUMENT ME!
     * @param  diffs           DOCUMENT ME!
     */
    private static void mergeChanges(final int[] firstInterval,
            final int[] secondInterval,
            final List firstChanges,
            final List secondChanges,
            final List<Difference> diffs) {
        int p1;
        int p2;
        final int n1 = firstChanges.size();
        final int n2 = secondChanges.size();
        // System.out.println("mergeChanges(("+firstInterval[0]+", "+firstInterval[1]+"), ("+secondInterval[0]+",
        // "+secondInterval[1]+"))"); System.out.println("firstChanges.size() = "+n1);
        // System.out.println("secondChanges.size() = "+n2);
        int firstToSecondIntervalShift = secondInterval[0] - firstInterval[0];
        // System.out.println("shift = "+firstToSecondIntervalShift);
        for (p1 = p2 = 0; (p1 < n1) || (p2 < n2);) {
            boolean isAddRemove = true;
            while (isAddRemove && (p1 < n1)) {
                final int[] interval = (int[])firstChanges.get(p1);
                if (p2 < n2) {
                    final int[] interval2 = (int[])secondChanges.get(p2);
                    if ((interval[0] + firstToSecondIntervalShift) > interval2[0]) {
                        break;
                    }
                    // We need to set differences successively. Differences with
                    // higher line numbers must not precede differences with
                    // smaller line numbers
                }
                isAddRemove = (interval[2] == Difference.ADD) || (interval[2] == Difference.DELETE);
                if (isAddRemove) {
                    if (interval[2] == Difference.ADD) {
                        diffs.add(new Difference(
                                interval[2],
                                interval[0]
                                        - 1,
                                0,
                                interval[0]
                                        + firstToSecondIntervalShift,
                                interval[1]
                                        + firstToSecondIntervalShift,
                                (String)firstChanges.get(p1 + 1),
                                ""));
                        firstToSecondIntervalShift += interval[1] - interval[0] + 1;
                    } else {
                        diffs.add(new Difference(
                                interval[2],
                                interval[0],
                                interval[1],
                                interval[0]
                                        + firstToSecondIntervalShift
                                        - 1,
                                0,
                                (String)firstChanges.get(p1 + 1),
                                ""));
                        firstToSecondIntervalShift -= interval[1] - interval[0] + 1;
                    }
                    p1 += 2;
                    // System.out.println("added diff = "+diffs.get(diffs.size() - 1));
                    // System.out.println("new shift = "+firstToSecondIntervalShift);
                }
            }
            isAddRemove = true;
            while (isAddRemove && (p2 < n2)) {
                final int[] interval = (int[])secondChanges.get(p2);
                isAddRemove = (interval[2] == Difference.ADD) || (interval[2] == Difference.DELETE);
                if (isAddRemove) {
                    if (interval[2] == Difference.ADD) {
                        diffs.add(new Difference(
                                interval[2],
                                interval[0]
                                        - firstToSecondIntervalShift
                                        - 1,
                                0,
                                interval[0],
                                interval[1],
                                "",
                                (String)secondChanges.get(p2 + 1)));
                        firstToSecondIntervalShift += interval[1] - interval[0] + 1;
                    } else {
                        diffs.add(new Difference(
                                interval[2],
                                interval[0]
                                        - firstToSecondIntervalShift,
                                interval[1]
                                        - firstToSecondIntervalShift,
                                interval[0]
                                        - 1,
                                0,
                                "",
                                (String)secondChanges.get(p2 + 1)));
                        firstToSecondIntervalShift -= interval[1] - interval[0] + 1;
                    }
                    p2 += 2;
                    // System.out.println("added diff = "+diffs.get(diffs.size() - 1));
                    // System.out.println("new shift = "+firstToSecondIntervalShift);
                }
            }
            // Change is remaining
            if ((p1 < n1) && (p2 < n2)) {
                final int[] interval1 = (int[])firstChanges.get(p1);
                if (interval1[2] == Difference.CHANGE) { // double check the break above
                    final int[] interval2 = (int[])secondChanges.get(p2);
                    diffs.add(new Difference(
                            interval1[2],
                            interval1[0],
                            interval1[1],
                            interval2[0],
                            interval2[1],
                            (String)firstChanges.get(p1 + 1),
                            (String)secondChanges.get(p2 + 1)));
                    p1 += 2;
                    p2 += 2;
                    firstToSecondIntervalShift += interval2[1] - interval2[0] - (interval1[1] - interval1[0]);
                    // System.out.println("added diff = "+diffs.get(diffs.size() - 1));
                    // System.out.println("new shift = "+firstToSecondIntervalShift);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   in  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private static Difference[] parseUnifiedDiff(final Reader in) throws IOException {
        final BufferedReader br = new BufferedReader(in);
        final List<Difference> diffs = new ArrayList<Difference>();
        String line = null;
        do {
            while ((line == null)
                        || !(line.startsWith(UNIFIED_MARK)
                            && (line.length() > UNIFIED_MARK.length())
                            && line.endsWith(UNIFIED_MARK))) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
            }
            if (line == null) {
                continue;
            }
            final int[] intervals = new int[4];
            try {
                readUnifiedNums(line, UNIFIED_MARK.length(), intervals);
            } catch (NumberFormatException nfex) {
                final IOException ioex = new IOException("Can not parse: " + line);
                ioex.initCause(nfex);
                throw ioex;
            }
            line = fillUnidifChanges(intervals, br, diffs);
        } while (line != null);
        return diffs.toArray(new Difference[diffs.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   str     DOCUMENT ME!
     * @param   off     DOCUMENT ME!
     * @param   values  DOCUMENT ME!
     *
     * @throws  NumberFormatException  DOCUMENT ME!
     */
    private static void readUnifiedNums(final String str, int off, final int[] values) throws NumberFormatException {
        while ((str.charAt(off) == ' ') || (str.charAt(off) == '-')) {
            off++;
        }
        int end = str.indexOf(CONTEXT_MARK_DELIMETER, off);
        if (end > 0) {
            values[0] = Integer.parseInt(str.substring(off, end).trim());
        } else {
            throw new NumberFormatException("Missing comma.");
        }
        off = end + 1;
        end = str.indexOf(' ', off);
        if (end > 0) {
            values[1] = Integer.parseInt(str.substring(off, end).trim());
        } else {
            throw new NumberFormatException("Missing middle space.");
        }
        off = end + 1;
        while ((str.charAt(off) == ' ') || (str.charAt(off) == '+')) {
            off++;
        }
        end = str.indexOf(CONTEXT_MARK_DELIMETER, off);
        if (end > 0) {
            values[2] = Integer.parseInt(str.substring(off, end).trim());
        } else {
            throw new NumberFormatException("Missing second comma.");
        }
        off = end + 1;
        end = str.indexOf(' ', off);
        if (end > 0) {
            values[3] = Integer.parseInt(str.substring(off, end).trim());
        } else {
            throw new NumberFormatException("Missing final space.");
        }
        values[1] += values[0] - 1;
        values[3] += values[2] - 1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   interval  DOCUMENT ME!
     * @param   br        DOCUMENT ME!
     * @param   diffs     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private static String fillUnidifChanges(final int[] interval, final BufferedReader br, final List<Difference> diffs)
            throws IOException {
        String line = br.readLine();
        int pos1 = interval[0];
        int pos2 = interval[2];
        while ((line != null) && (pos1 <= interval[1]) && (pos2 <= interval[3])) {
            if (line.startsWith(LINE_PREP_UNIF_ADD)) {
                final int begin = pos2;
                final StringBuffer changeText = new StringBuffer();
                changeText.append(line.substring(LINE_PREP_UNIF_ADD.length()));
                changeText.append('\n');
                do {
                    line = br.readLine();
                    pos2++;
                    if (line.startsWith(LINE_PREP_UNIF_ADD)) {
                        changeText.append(line.substring(LINE_PREP_UNIF_ADD.length()));
                        changeText.append('\n');
                    } else {
                        break;
                    }
                } while (true);
                Difference diff = null;
                if (diffs.size() > 0) {
                    final Difference previousDiff = (Difference)diffs.get(diffs.size() - 1);
                    if ((Difference.DELETE == previousDiff.getType()) && (previousDiff.getFirstEnd() == (pos1 - 1))) {
                        diff = new Difference(
                                Difference.CHANGE,
                                previousDiff.getFirstStart(),
                                previousDiff.getFirstEnd(),
                                begin,
                                pos2
                                        - 1,
                                previousDiff.getFirstText(),
                                changeText.toString());
                        diffs.remove(diffs.size() - 1);
                    }
                }
                if (diff == null) {
                    diff = new Difference(Difference.ADD, pos1 - 1, 0, begin, pos2 - 1, null, changeText.toString());
                }
                diffs.add(diff);
            } else if (line.startsWith(LINE_PREP_UNIF_REMOVE)) {
                final int begin = pos1;
                final StringBuffer changeText = new StringBuffer();
                changeText.append(line.substring(LINE_PREP_UNIF_REMOVE.length()));
                changeText.append('\n');
                do {
                    line = br.readLine();
                    pos1++;
                    if (line.startsWith(LINE_PREP_UNIF_REMOVE)) {
                        changeText.append(line.substring(LINE_PREP_UNIF_REMOVE.length()));
                        changeText.append('\n');
                    } else {
                        break;
                    }
                } while (true);
                Difference diff = null;
                if (diffs.size() > 0) {
                    final Difference previousDiff = (Difference)diffs.get(diffs.size() - 1);
                    if ((Difference.ADD == previousDiff.getType()) && (previousDiff.getSecondEnd() == (pos2 - 1))) {
                        diff = new Difference(
                                Difference.CHANGE,
                                begin,
                                pos1
                                        - 1,
                                previousDiff.getFirstStart(),
                                previousDiff.getFirstEnd(),
                                changeText.toString(),
                                previousDiff.getFirstText());
                        diffs.remove(diffs.size() - 1);
                    }
                }
                if (diff == null) {
                    diff = new Difference(Difference.DELETE, begin, pos1 - 1, pos2 - 1, 0, changeText.toString(), null);
                }
                diffs.add(diff);
            } else {
                line = br.readLine();
                pos1++;
                pos2++;
            }
        }
        return line;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   in  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private static Difference[] parseNormalDiff(final Reader in) throws IOException {
        Pattern normRegexp;
        try {
            normRegexp = Pattern.compile(CmdlineDiffProvider.DIFF_REGEXP);
        } catch (PatternSyntaxException rsex) {
            normRegexp = null;
        }
        final StringBuffer firstText = new StringBuffer();
        final StringBuffer secondText = new StringBuffer();
        final BufferedReader br = new BufferedReader(in);
        final List<Difference> diffs = new ArrayList<Difference>();
        String line;
        while ((line = br.readLine()) != null) {
            CmdlineDiffProvider.outputLine(line, normRegexp, diffs, firstText, secondText);
        }
        CmdlineDiffProvider.setTextOnLastDifference(diffs, firstText, secondText);
        return diffs.toArray(new Difference[diffs.size()]);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * A reader, that will not read more, than a single patch content from the supplied reader with possibly more
     * patches.
     *
     * @version  $Revision$, $Date$
     */
    private static class SinglePatchReader extends Reader {

        //~ Static fields/initializers -----------------------------------------

        private static final int BUFF_SIZE = 512;

        private static final String FILE_INDEX = "Index: "; // NOI18N

        //~ Instance fields ----------------------------------------------------

        private PushbackReader in;
        private char[] buffer = new char[BUFF_SIZE];
        private int buffLength = 0;
        private int buffPos = 0;
        private boolean isAtEndOfPatch = false;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SinglePatchReader object.
         *
         * @param  in  DOCUMENT ME!
         */
        public SinglePatchReader(final Reader in) {
            this.in = new PushbackReader(in, BUFF_SIZE);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public int read(final char[] values, int offset, int length) throws java.io.IOException {
            // System.out.println("SinglePatchReader.read("+offset+", "+length+")");
            int totRead = 0;
            while (length > 0) {
                int buffCopyLength;
                if (length < buffLength) {
                    buffCopyLength = length;
                    length = 0;
                } else {
                    if (buffLength > 0) {
                        buffCopyLength = buffLength;
                        length -= buffLength;
                    } else {
                        if (isAtEndOfPatch) {
                            length = 0;
                            buffCopyLength = -1;
                        } else {
                            buffLength = readTillEndOfPatch(buffer);
                            buffPos = 0;
                            if (buffLength <= 0) {
                                buffCopyLength = -1;
                            } else {
                                buffCopyLength = Math.min(length, buffLength);
                                length -= buffCopyLength;
                            }
                        }
                    }
                }
                if (buffCopyLength > 0) {
                    System.arraycopy(buffer, buffPos, values, offset, buffCopyLength);
                    offset += buffCopyLength;
                    buffLength -= buffCopyLength;
                    buffPos += buffCopyLength;
                    totRead += buffCopyLength;
                } else {
                    length = 0;
                }
            }
            if (totRead == 0) {
                totRead = -1;
            }
            // System.out.println("  read = '"+((totRead >= 0) ? new String(values, 0, totRead) : "NOTHING")+"',
            // totRead = "+totRead);
            return totRead;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   buffer  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  IOException  DOCUMENT ME!
         */
        private int readTillEndOfPatch(final char[] buffer) throws IOException {
            int length = in.read(buffer);
            final String input = new String(buffer);
            int end = 0;
            if (input.startsWith(FILE_INDEX) || ((end = input.indexOf("\n" + FILE_INDEX)) >= 0)) {
                isAtEndOfPatch = true;
            } else {
                end = input.lastIndexOf('\n');
                if (end >= 0) {
                    end++;
                }
            }
            if ((end >= 0) && (end < length)) {
                in.unread(buffer, end, length - end);
                length = end;
            }
            if (end == 0) {
                length = -1;
            }
            return length;
        }

        @Override
        public void close() throws java.io.IOException {
            // Do nothing!
        }

        /**
         * DOCUMENT ME!
         *
         * @param   diffType  DOCUMENT ME!
         * @param   fileName  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  IOException  DOCUMENT ME!
         */
        private boolean hasNextPatch(final int[] diffType, final String[] fileName) throws IOException {
            isAtEndOfPatch = false; // We're prepared for the next patch
            final PushbackReader patchSource = in;
            final char[] buff = new char[DIFFERENCE_DELIMETER.length()];
            int length;
            Pattern normRegexp;
            boolean contextBeginDetected = false;
            try {
                normRegexp = Pattern.compile(CmdlineDiffProvider.DIFF_REGEXP);
            } catch (PatternSyntaxException rsex) {
                normRegexp = null;
            }
            while ((length = patchSource.read(buff)) > 0) {
                String input = new String(buff, 0, length);
                int nl;
                final int nln = input.indexOf('\n');
                final int nlr = input.indexOf('\r');
                if (nln < 0) {
                    nl = nlr;
                } else {
                    nl = nln;
                }
                if (nl >= 0) {
                    if ((nln > 0) && (nln == (nlr + 1))) {
                        input = input.substring(0, nl - 1);
                    } else {
                        input = input.substring(0, nl);
                    }
                    if ((nl + 1) < length) {
                        patchSource.unread(buff, nl + 1, length - (nl + 1));
                        length = nl + 1;
                    }
                }
                if (input.equals(DIFFERENCE_DELIMETER)) {
                    diffType[0] = CONTEXT_DIFF;
                    patchSource.unread(buff, 0, length);
                    return true;
                } else if (input.startsWith(UNIFIED_MARK + " ")) {
                    diffType[0] = UNIFIED_DIFF;
                    patchSource.unread(buff, 0, length);
                    return true;
                } else if (input.startsWith(FILE_INDEX)) {
                    final StringBuffer name = new StringBuffer(input.substring(FILE_INDEX.length()));
                    if (nl < 0) {
                        int r;
                        char c;
                        while (((c = (char)(r = patchSource.read())) != '\n') && (r != -1) && (r != '\r')) {
                            name.append(c);
                        }
                    }
                    fileName[1] = name.toString();
                } else if (input.startsWith(CONTEXT_MARK1B)
                            || (!contextBeginDetected && input.startsWith(UNIFIED_MARK1))) {
                    StringBuffer name;
                    if (input.startsWith(CONTEXT_MARK1B)) {
                        contextBeginDetected = true;
                        name = new StringBuffer(input.substring(CONTEXT_MARK1B.length()));
                    } else {
                        name = new StringBuffer(input.substring(UNIFIED_MARK1.length()));
                    }
                    final String sname = name.toString();
                    final int spaceIndex = sname.indexOf('\t');
                    if (spaceIndex > 0) {
                        name = name.delete(spaceIndex, name.length());
                    }
                    if (nl < 0) {
                        int r = 0;
                        char c = 0;
                        if (spaceIndex < 0) {
                            while (((c = (char)(r = patchSource.read())) != '\n') && (c != '\r') && (c != '\t')
                                        && (r != -1)) {
                                name.append(c);
                            }
                        }
                        if ((c != '\n') && (c != '\r') && (r != -1)) {
                            while (((c = (char)(r = patchSource.read())) != '\n') && (c != '\r') && (r != -1)) {
                                ;   // Read the rest of the line
                            }
                        }
                        if (c == '\r') {
                            r = patchSource.read();
                            if (r != -1) {
                                c = (char)r;
                                if (c != '\n') {
                                    patchSource.unread(c);
                                }
                            }
                        }
                    }
                    fileName[0] = name.toString();
                } else if ((normRegexp != null) && normRegexp.matcher(input).matches()) {
                    diffType[0] = NORMAL_DIFF;
                    patchSource.unread(buff, 0, length);
                    return true;
                } else {            // Read the rest of the garbaged line
                    if (nl < 0) {
                        int r;
                        char c;
                        while (((c = (char)(r = patchSource.read())) != '\n') && (c != '\r') && (r != -1)) {
                            ;
                        }
                        if (c == '\r') {
                            r = patchSource.read();
                            if (r != -1) {
                                c = (char)r;
                                if (c != '\n') {
                                    patchSource.unread(c);
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class FileDifferences extends Object {

        //~ Instance fields ----------------------------------------------------

        private String fileName;
        private String indexName;
        private Difference[] diffs;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FileDifferences object.
         *
         * @param  fileName   DOCUMENT ME!
         * @param  indexName  DOCUMENT ME!
         * @param  diffs      DOCUMENT ME!
         */
        public FileDifferences(final String fileName, final String indexName, final Difference[] diffs) {
            this.fileName = fileName;
            this.diffs = diffs;
            this.indexName = indexName;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  header filename (typically absolute path on source host) or null
         */
        public final String getFileName() {
            return fileName;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  relative Index: file name or null
         */
        public final String getIndexName() {
            return indexName;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public final Difference[] getDifferences() {
            return diffs;
        }
    }
}
