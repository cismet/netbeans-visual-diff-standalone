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
package org.netbeans.modules.diff.builtin.visualizer;

import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.builtin.DiffPresenter;
import org.netbeans.spi.diff.DiffVisualizer;

import org.openide.util.NbBundle;
import org.openide.windows.CloneableOpenSupport;

import java.awt.Component;

import java.io.*;

/**
 * The textual visualizer of diffs.
 *
 * @author   Martin Entlicher
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.spi.diff.DiffVisualizer.class)
public class TextDiffVisualizer extends DiffVisualizer implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    static final long serialVersionUID = -2481513747957146261L;

    //~ Instance fields --------------------------------------------------------

    private boolean contextMode = true;
    private int contextNumLines = 3;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of TextDiffVisualizer.
     */
    public TextDiffVisualizer() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Get the display name of this diff visualizer.
     *
     * @return  DOCUMENT ME!
     */
    public String getDisplayName() {
        return NbBundle.getMessage(TextDiffVisualizer.class, "TextDiffVisualizer.displayName");
    }

    /**
     * Get a short description of this diff visualizer.
     *
     * @return  DOCUMENT ME!
     */
    public String getShortDescription() {
        return NbBundle.getMessage(TextDiffVisualizer.class, "TextDiffVisualizer.shortDescription");
    }

    /**
     * Getter for property contextMode.
     *
     * @return  Value of property contextMode.
     */
    public boolean isContextMode() {
        return contextMode;
    }

    /**
     * Setter for property contextMode.
     *
     * @param  contextMode  New value of property contextMode.
     */
    public void setContextMode(final boolean contextMode) {
        this.contextMode = contextMode;
    }

    /**
     * Getter for property contextNumLines.
     *
     * @return  Value of property contextNumLines.
     */
    public int getContextNumLines() {
        return contextNumLines;
    }

    /**
     * Setter for property contextNumLines.
     *
     * @param  contextNumLines  New value of property contextNumLines.
     */
    public void setContextNumLines(final int contextNumLines) {
        this.contextNumLines = contextNumLines;
    }

    /**
     * Some diff visualizers may have built-in the diff calculation. In such a case the visualizer does not need any
     * diff provider.
     *
     * @param   diffs     DOCUMENT ME!
     * @param   name1     DOCUMENT ME!
     * @param   title1    DOCUMENT ME!
     * @param   r1        DOCUMENT ME!
     * @param   name2     DOCUMENT ME!
     * @param   title2    DOCUMENT ME!
     * @param   r2        DOCUMENT ME!
     * @param   MIMEType  DOCUMENT ME!
     *
     * @return  true when it relies on differences supplied, false if not.
     *
     *          <p>public boolean needsProvider() { return true; }</p>
     *
     * @throws  IOException  DOCUMENT ME!
     */

    /**
     * Show the visual representation of the diff between two sources.
     *
     * @param   diffs     The list of differences (instances of {@link Difference}). may be <code>null</code> in case
     *                    that it does not need diff provider.
     * @param   name1     the name of the first source
     * @param   title1    the title of the first source
     * @param   r1        the first source
     * @param   name2     the name of the second source
     * @param   title2    the title of the second source
     * @param   r2        the second resource compared with the first one.
     * @param   MIMEType  the mime type of these sources
     *
     * @return  The TopComponent representing the diff visual representation or null, when the representation is outside
     *          the IDE.
     *
     * @throws  IOException  when the reading from input streams fails.
     */
    @Override
    public Component createView(final Difference[] diffs,
            final String name1,
            final String title1,
            final Reader r1,
            final String name2,
            final String title2,
            final Reader r2,
            final String MIMEType) throws IOException {
        /*
         * TextDiffEditorSupport.DiffsListWithOpenSupport diff = new
         * TextDiffEditorSupport.DiffsListWithOpenSupport(diffs, name1 + " <> " + name2, title1+" <> "+title2);
         * diff.setContextMode(contextMode, contextNumLines); diff.setReaders(r1, r2); return ((TextDiffEditorSupport)
         * diff.getOpenSupport()).createCloneableTopComponentForMe(); //return null;
         */
        final TextDiffInfo diff = new TextDiffInfo(name1, name2, title1, title2, r1, r2, diffs);
        diff.setContextMode(contextMode, contextNumLines);
        return ((TextDiffEditorSupport)diff.getOpenSupport()).createCloneableTopComponentForMe();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   diffs  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    static InputStream differenceToLineDiffText(final Difference[] diffs) {
        final StringBuffer content = new StringBuffer();
        int n1;
        int n2;
        int n3;
        int n4;
        for (int i = 0; i < diffs.length; i++) {
            final Difference diff = diffs[i];
            switch (diff.getType()) {
                case Difference.ADD: {
                    n3 = diff.getSecondStart();
                    n4 = diff.getSecondEnd();
                    if (n3 == n4) {
                        content.append(diff.getFirstStart() + "a" + n3 + "\n");
                    } else {
                        content.append(diff.getFirstStart() + "a" + n3 + "," + n4 + "\n");
                    }
                    appendText(content, "> ", diff.getSecondText());
                    break;
                }
                case Difference.DELETE: {
                    n1 = diff.getFirstStart();
                    n2 = diff.getFirstEnd();
                    if (n1 == n2) {
                        content.append(n1 + "d" + diff.getSecondStart() + "\n");
                    } else {
                        content.append(n1 + "," + n2 + "d" + diff.getSecondStart() + "\n");
                    }
                    appendText(content, "< ", diff.getFirstText());
                    break;
                }
                case Difference.CHANGE: {
                    n1 = diff.getFirstStart();
                    n2 = diff.getFirstEnd();
                    n3 = diff.getSecondStart();
                    n4 = diff.getSecondEnd();
                    if ((n1 == n2) && (n3 == n4)) {
                        content.append(n1 + "c" + n3 + "\n");
                    } else if (n1 == n2) {
                        content.append(n1 + "c" + n3 + "," + n4 + "\n");
                    } else if (n3 == n4) {
                        content.append(n1 + "," + n2 + "c" + n3 + "\n");
                    } else {
                        content.append(n1 + "," + n2 + "c" + n3 + "," + n4 + "\n");
                    }
                    appendText(content, "< ", diff.getFirstText());
                    content.append("---\n");
                    appendText(content, "> ", diff.getSecondText());
                    break;
                }
            }
        }
        return new ByteArrayInputStream(content.toString().getBytes());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  buff    DOCUMENT ME!
     * @param  prefix  DOCUMENT ME!
     * @param  text    DOCUMENT ME!
     */
    private static void appendText(final StringBuffer buff, final String prefix, final String text) {
        if (text == null) {
            return;
        }
        int startLine = 0;
        do {
            int endLine = text.indexOf('\n', startLine);
            if (endLine < 0) {
                endLine = text.length();
            }
            buff.append(prefix + text.substring(startLine, endLine) + "\n");
            startLine = endLine + 1;
        } while (startLine < text.length());
    }

    /**
     * Produces textual diff output in unified format.
     *
     * @param   diffInfo  encapsulates information needed to produce the diff
     *
     * @return  String textual diff output in unified format (unidiff)
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static String differenceToUnifiedDiffText(final TextDiffInfo diffInfo) throws IOException {
        final UnifiedDiff ud = new UnifiedDiff(diffInfo);
        return ud.computeDiff();
    }

    /**
     * Produces textual diff output in normal format.
     *
     * @param   diffInfo  DOCUMENT ME!
     *
     * @return  String textual diff output in normal diff format
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static String differenceToNormalDiffText(final TextDiffInfo diffInfo) throws IOException {
        final InputStream is = differenceToLineDiffText(diffInfo.diffs);
        final StringWriter sw = new StringWriter();
        copyStreamsCloseAll(sw, new InputStreamReader(is));
        return sw.toString();
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class TextDiffInfo extends DiffPresenter.Info {

        //~ Instance fields ----------------------------------------------------

        private Reader r1;
        private Reader r2;
        private Difference[] diffs;
        private CloneableOpenSupport openSupport;
        private boolean contextMode;
        private int contextNumLines;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new TextDiffInfo object.
         *
         * @param  name1   DOCUMENT ME!
         * @param  name2   DOCUMENT ME!
         * @param  title1  DOCUMENT ME!
         * @param  title2  DOCUMENT ME!
         * @param  r1      DOCUMENT ME!
         * @param  r2      DOCUMENT ME!
         * @param  diffs   DOCUMENT ME!
         */
        public TextDiffInfo(final String name1,
                final String name2,
                final String title1,
                final String title2,
                final Reader r1,
                final Reader r2,
                final Difference[] diffs) {
            super(name1, name2, title1, title2, null, false, false);
            this.r1 = r1;
            this.r2 = r2;
            this.diffs = diffs;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getName() {
            String componentName = getName1();
            final String name2 = getName2();
            if ((name2 != null) && (name2.length() > 0)) {
                componentName += " <> " + name2;
            }
            return componentName;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getTitle() {
            return getTitle1() + " <> " + getTitle2();
        }

        @Override
        public Reader createFirstReader() {
            return r1;
        }

        @Override
        public Reader createSecondReader() {
            return r2;
        }

        @Override
        public Difference[] getDifferences() {
            return diffs;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public CloneableOpenSupport getOpenSupport() {
            if (openSupport == null) {
                openSupport = new TextDiffEditorSupport(this);
            }
            return openSupport;
        }

        /**
         * Setter for property contextMode.
         *
         * @param  contextMode      New value of property contextMode.
         * @param  contextNumLines  DOCUMENT ME!
         */
        public void setContextMode(final boolean contextMode, final int contextNumLines) {
            this.contextMode = contextMode;
            this.contextNumLines = contextNumLines;
        }

        /**
         * Getter for property contextMode.
         *
         * @return  Value of property contextMode.
         */
        public boolean isContextMode() {
            return contextMode;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getContextNumLines() {
            return contextNumLines;
        }
    }
}
