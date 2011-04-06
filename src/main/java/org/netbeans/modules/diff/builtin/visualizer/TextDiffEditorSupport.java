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

import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.*;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
//import org.openide.util.Task;
//import org.openide.util.TaskListener;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.*;

import java.io.*;

import javax.swing.text.*;

/**
 * Support for associating an editor and a Swing {@link Document} to a revision object. This is a modification of
 * org.openide.text.DataEditorSupport
 *
 * @author   Jaroslav Tulach, Martin Entlicher
 * @version  $Revision$, $Date$
 */
public class TextDiffEditorSupport extends CloneableEditorSupport implements EditorCookie.Observable,
    OpenCookie,
    PrintCookie,
    CloseCookie {

    //~ Instance fields --------------------------------------------------------

    /** The difference. */
    // private final DiffsListWithOpenSupport diff;
    private final TextDiffVisualizer.TextDiffInfo diff;

    /*
     * public static final class DiffsListWithOpenSupport extends Object {  private Difference[] diffs; private final
     * String name; private final String tooltip; private CloneableOpenSupport openSupport; private boolean contextMode
     * = false; private int contextNumLines; private Reader r1, r2;  public DiffsListWithOpenSupport(Difference[] diffs,
     * String name, String tooltip) {     this.diffs = diffs;     this.name = name;     this.tooltip = tooltip; }  /*
     * public void setOpenSupport(CloneableOpenSupport openSupport) {     this.openSupport = openSupport; }
     *
     * public Difference[] getDiffs() {     return diffs; }  public CloneableOpenSupport getOpenSupport() {     if
     * (openSupport == null) {         openSupport = new TextDiffEditorSupport(this);     }     return openSupport; }
     * public String getName() {     return name; }  public String getTooltip() {     return tooltip; }  /** Setter for
     * property contextMode. @param contextMode New value of property contextMode.
     *
     * public void setContextMode(boolean contextMode, int contextNumLines) {     this.contextMode = contextMode;
     * this.contextNumLines = contextNumLines; }  /** Getter for property contextMode. @return Value of property
     * contextMode.
     *
     * public boolean isContextMode() {     return contextMode; }  public int getContextNumLines() {     return
     * contextNumLines; }  public void setReaders(Reader r1, Reader r2) {     this.r1 = r1;     this.r2 = r2; }  public
     * Reader getFirstReader() {     return r1; }  public Reader getSecondReader() {     return r2; } }
     */

    //~ Constructors -----------------------------------------------------------

    /**
     * Editor support for a given data object. The file is taken from the data object and is updated if the object moves
     * or renames itself.
     *
     * @param  diff  obj object to work with
     */
    TextDiffEditorSupport(final TextDiffVisualizer.TextDiffInfo diff) { // DiffsListWithOpenSupport diff) {
        super(new TextDiffEditorSupport.Env(diff));
        this.diff = diff;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Getter of the file object that this support is associated with.
     *
     * @return  file object passed in constructor
     */
    public final FileObject getFileObject() {
        return null;
    }

    /**
     * Message to display when an object is being opened.
     *
     * @return  the message or null if nothing should be displayed
     */
    @Override
    protected String messageOpening() {
        return NbBundle.getMessage(TextDiffEditorSupport.class, "CTL_ObjectOpen", // NOI18N
                diff.getName());
    }

    /**
     * Message to display when an object has been opened.
     *
     * @return  the message or null if nothing should be displayed
     */
    @Override
    protected String messageOpened() {
        return NbBundle.getMessage(TextDiffEditorSupport.class, "CTL_ObjectOpened", // NOI18N
                diff.getName());
    }

    /**
     * Constructs message that should be displayed when the data object is modified and is being closed.
     *
     * @return  text to show to the user
     */
    @Override
    protected String messageSave() {
        return ""; /*NbBundle.getMessage (
                    * DataEditorSupport.class, "MSG_SaveFile", // NOI18N obj.getName());*/
    }

    /**
     * Constructs message that should be used to name the editor component.
     *
     * @return  name of the editor
     */
    @Override
    protected String messageName() {
        return diff.getName();
    }

    /**
     * Text to use as tooltip for component.
     *
     * @return  text to show to the user
     */
    @Override
    protected String messageToolTip() {
        // update tooltip
        return diff.getTitle();
    }

    /**
     * Annotates the editor with icon from the data object and also sets appropriate selected node. This implementation
     * also listen to display name and icon chamges of the node and keeps editor top component up-to-date. If you
     * override this method and not call super, please note that you will have to keep things synchronized yourself.
     *
     * @param  editor  the editor that has been created and should be annotated
     */
    @Override
    protected void initializeCloneableEditor(final CloneableEditor editor) {
        editor.setIcon(ImageUtilities.loadImage("org/netbeans/modules/diff/diffSettingsIcon.gif", true));
        // ourNode.getIcon (java.beans.BeanInfo.ICON_COLOR_16x16));
        // nodeL = new DataNodeListener(editor);
        // ourNode.addNodeListener(WeakListener.node(nodeL, ourNode));
    }

    @Override
    protected CloneableEditor createCloneableEditor() {
        return new DiffCloneableEditor(this);
    }

    /**
     * Let's the super method create the document and also annotates it with Title and StreamDescription properities.
     *
     * @param   kit  kit to user to create the document
     *
     * @return  the document annotated by the properties
     */
    @Override
    protected StyledDocument createStyledDocument(final EditorKit kit) {
        final StyledDocument doc = super.createStyledDocument(kit);

        // set document name property
        doc.putProperty(javax.swing.text.Document.TitleProperty,
            diff.getName());
        /* set dataobject to stream desc property
         * doc.putProperty(javax.swing.text.Document.StreamDescriptionProperty, obj );
         */
        return doc;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CloneableTopComponent createCloneableTopComponentForMe() {
        return createCloneableTopComponent();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Getter for data object associated with this data object.
     *
     * <p>final DataObject getDataObjectHack () { return obj; }</p>
     *
     * @version  $Revision$, $Date$
     */

    /**
     * Environment that connects the data object and the CloneableEditorSupport.
     *
     * @version  $Revision$, $Date$
     */
    public static class Env extends Object implements CloneableOpenSupport.Env,
        CloneableEditorSupport.Env,
        java.io.Serializable
    /*PropertyChangeListener, VetoableChangeListener*/ {

        //~ Static fields/initializers -----------------------------------------

        /** generated Serialized Version UID. */
        static final long serialVersionUID = -2945098431098324441L;

        //~ Instance fields ----------------------------------------------------

        /** The difference. */
        private transient TextDiffVisualizer.TextDiffInfo diff;

        //~ Constructors -------------------------------------------------------

        /**
         * Constructor.
         *
         * @param  diff  obj this support should be associated with
         */
        public Env(final TextDiffVisualizer.TextDiffInfo diff) { // DiffsListWithOpenSupport diff) {
            this.diff = diff;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * Locks the file.
         *
         * @return     the lock on the file getFile ()
         *
         * @exception  IOException  if the file cannot be locked
         */
        // protected abstract FileLock takeLock () throws IOException;

        /**
         * Obtains the input stream.
         *
         * @return     DOCUMENT ME!
         *
         * @exception  IOException  if an I/O error occures
         */
        @Override
        public InputStream inputStream() throws IOException {
            if (diff.isContextMode()) {
                final String diffText = TextDiffVisualizer.differenceToUnifiedDiffText(diff);
                return new ByteArrayInputStream(diffText.getBytes("utf8")); // NOI18N
            } else {
                return TextDiffVisualizer.differenceToLineDiffText(diff.getDifferences());
            }
        }

        /**
         * Obtains the output stream.
         *
         * @return     DOCUMENT ME!
         *
         * @exception  IOException  if an I/O error occures
         */
        @Override
        public OutputStream outputStream() throws IOException {
            throw new IOException("No output to a file diff supported.");
            // return getFileImpl ().getOutputStream (fileLock);
        }

        /**
         * Mime type of the document.
         *
         * @return  the mime type to use for the document
         */
        @Override
        public String getMimeType() {
            return "text/plain"; // NOI18N
        }

        /**
         * First of all tries to lock the primary file and if it succeeds it marks the data object modified.
         *
         * @throws     java.io.IOException  DOCUMENT ME!
         * @exception  IOException          if the environment cannot be marked modified (for example when the file is
         *                                  readonly), when such exception is the support should discard all previous
         *                                  changes
         */
        @Override
        public void markModified() throws java.io.IOException {
            throw new IOException("The file revision can not be modified.");
            /*
             * if (fileLock == null || !fileLock.isValid()) { fileLock = takeLock (); }
             *
             * this.getDataObject ().setModified (true);
             */
        }

        /**
         * Reverse method that can be called to make the environment unmodified.
         */
        @Override
        public void unmarkModified() {
            // throw new IOException("The file revision can not be unmodified.");
        }

        /**
         * Called from the EnvListener.
         *
         * @param  propertyChangeListener  expected is the change expected
         */

        @Override
        public void removePropertyChangeListener(final java.beans.PropertyChangeListener propertyChangeListener) {
        }

        @Override
        public boolean isModified() {
            return false;
        }

        @Override
        public java.util.Date getTime() {
            return new java.util.Date(System.currentTimeMillis());
        }

        @Override
        public void removeVetoableChangeListener(final java.beans.VetoableChangeListener vetoableChangeListener) {
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addVetoableChangeListener(final java.beans.VetoableChangeListener vetoableChangeListener) {
        }

        @Override
        public void addPropertyChangeListener(final java.beans.PropertyChangeListener propertyChangeListener) {
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            // return (CloneableOpenSupport) list.getNodeDelegate(revisionItem,
            // null).getCookie(CloneableOpenSupport.class);
            return diff.getOpenSupport();
        }
    } // end of Env

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class DiffCloneableEditor extends CloneableEditor {

        //~ Instance fields ----------------------------------------------------

        private boolean componentShowingCalled = false;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DiffCloneableEditor object.
         *
         * @param  support  DOCUMENT ME!
         */
        DiffCloneableEditor(final CloneableEditorSupport support) {
            super(support);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * When I'm added to some other component I suppose, that I'll be displayed. In this case this method call
         * componentShowing(). It must be assured, that the initialization is done.
         */
        @Override
        public void addNotify() {
            componentShowing();
            super.addNotify();
        }
        /**
         * The componentShowing() method is used in CloneableEditor to make some initializations. It calls super method
         * only once, since it can be called multiple times when called from addNotify() as well.
         */
        @Override
        protected void componentShowing() {
            if (!componentShowingCalled) {
                super.componentShowing();
                componentShowingCalled = true;
            }
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(TextDiffEditorSupport.class);
        }
    }

    /** Listener on file object that notifies the Env object
    * that a file has been modified.
    *
    private static final class EnvListener extends FileChangeAdapter {
        /** Reference (Env) *
        private Reference env;

        /** @param env environement to use
        *
        public EnvListener (Env env) {
            this.env = new java.lang.ref.WeakReference (env);
        }

        /** Fired when a file is changed.
        * @param fe the event describing context where action has taken place
        *
        public void fileChanged(FileEvent fe) {
            Env env = (Env)this.env.get ();
            if (env == null || env.getFileImpl () != fe.getFile ()) {
                // the Env change its file and we are not used
                // listener anymore => remove itself from the list of listeners
                fe.getFile ().removeFileChangeListener (this);
                return;
            }

            env.fileChanged (fe.isExpected (), fe.getTime ());
        }

    }

    /** Listener on node representing asociated data object, listens to the
     * property changes of the node and updates state properly
     *
    private final class DataNodeListener extends NodeAdapter {
        /** Asociated editor *
        CloneableEditor editor;

        DataNodeListener (CloneableEditor editor) {
            this.editor = editor;
        }

        public void propertyChange (java.beans.PropertyChangeEvent ev) {

            if (Node.PROP_DISPLAY_NAME.equals(ev.getPropertyName())) {
                updateTitles();
            }
            if (Node.PROP_ICON.equals(ev.getPropertyName())) {
                editor.setIcon(
                    getDataObject().getNodeDelegate().getIcon (java.beans.BeanInfo.ICON_COLOR_16x16)
                );
            }
        }

    } // end of DataNodeListener
     */

}
