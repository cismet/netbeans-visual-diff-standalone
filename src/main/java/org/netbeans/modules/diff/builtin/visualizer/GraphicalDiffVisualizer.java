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
import org.netbeans.spi.diff.DiffVisualizer;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;

import java.io.Reader;
import java.io.Serializable;

/**
 * The default graphical visualizer of diffs.
 *
 * @author   Martin Entlicher
 * @version  $Revision$, $Date$
 */
public class GraphicalDiffVisualizer extends DiffVisualizer implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    static final long serialVersionUID = -1135210647457196211L;

    //~ Instance fields --------------------------------------------------------

    private Color colorAdded = DiffComponent.COLOR_ADDED;
    private Color colorMissing = DiffComponent.COLOR_MISSING;
    private Color colorChanged = DiffComponent.COLOR_CHANGED;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of BuiltInDiffVisualizer.
     */
    public GraphicalDiffVisualizer() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Get the display name of this diff visualizer.
     *
     * @return  DOCUMENT ME!
     */
    public String getDisplayName() {
        return NbBundle.getMessage(GraphicalDiffVisualizer.class, "GraphicalDiffVisualizer.displayName");
    }

    /**
     * Get a short description of this diff visualizer.
     *
     * @return  DOCUMENT ME!
     */
    public String getShortDescription() {
        return NbBundle.getMessage(GraphicalDiffVisualizer.class, "GraphicalDiffVisualizer.shortDescription");
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
     */

    /**
     * Show the visual representation of the diff between two files.
     *
     * @param   diffs     The list of differences (instances of {@link Difference}). may be <code>null</code> in case
     *                    that it does not need diff provider.
     * @param   name1     fo1 the first FileObject
     * @param   title1    fo2 the second FileObject compared with the first one.
     * @param   r1        DOCUMENT ME!
     * @param   name2     DOCUMENT ME!
     * @param   title2    DOCUMENT ME!
     * @param   r2        DOCUMENT ME!
     * @param   MIMEType  DOCUMENT ME!
     *
     * @return  The TopComponent representing the diff visual representation or null, when the representation is outside
     *          the IDE.
     *
     *          <p>public Component showDiff(List diffs, FileObject fo1, FileObject fo2) { DiffComponent diff; try {
     *          diff = new DiffComponent(diffs, null, fo1.getMIMEType(), fo1.getName(), fo2.getName(),
     *          fo1.getPackageNameExt('/', '.'), fo2.getPackageNameExt('/', '.'), new
     *          InputStreamReader(fo1.getInputStream()), new InputStreamReader(fo2.getInputStream())); } catch
     *          (FileNotFoundException fnfex) { org.openide.TopManager.getDefault().notifyException(fnfex); return null;
     *          } return diff; }</p>
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
     */
    @Override
    public Component createView(final Difference[] diffs,
            final String name1,
            final String title1,
            final Reader r1,
            final String name2,
            final String title2,
            final Reader r2,
            final String MIMEType) {
        if (diffs.length == 0) {
            DialogDisplayer.getDefault()
                    .notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(GraphicalDiffVisualizer.class, "MSG_NoDifference", name1, name2)));
        }
        final DiffComponent diff;
        String componentName = name1;
        if ((name2 != null) && (name2.length() > 0)) {
            componentName = NbBundle.getMessage(
                    GraphicalDiffVisualizer.class,
                    "MSG_TwoFilesDiffTitle",
                    componentName,
                    name2);
        }
        diff = new DiffComponent(
                diffs,
                componentName,
                MIMEType,
                name1,
                name2,
                title1,
                title2,
                r1,
                r2,
                new Color[] { colorMissing, colorAdded, colorChanged });
        return diff;
    }

    /**
     * Getter for property colorAdded.
     *
     * @return  Value of property colorAdded.
     */
    public java.awt.Color getColorAdded() {
        return colorAdded;
    }

    /**
     * Setter for property colorAdded.
     *
     * @param  colorAdded  New value of property colorAdded.
     */
    public void setColorAdded(final java.awt.Color colorAdded) {
        this.colorAdded = colorAdded;
    }

    /**
     * Getter for property colorMissing.
     *
     * @return  Value of property colorMissing.
     */
    public java.awt.Color getColorMissing() {
        return colorMissing;
    }

    /**
     * Setter for property colorMissing.
     *
     * @param  colorMissing  New value of property colorMissing.
     */
    public void setColorMissing(final java.awt.Color colorMissing) {
        this.colorMissing = colorMissing;
    }

    /**
     * Getter for property colorChanged.
     *
     * @return  Value of property colorChanged.
     */
    public java.awt.Color getColorChanged() {
        return colorChanged;
    }

    /**
     * Setter for property colorChanged.
     *
     * @param  colorChanged  New value of property colorChanged.
     */
    public void setColorChanged(final java.awt.Color colorChanged) {
        this.colorChanged = colorChanged;
    }
}
