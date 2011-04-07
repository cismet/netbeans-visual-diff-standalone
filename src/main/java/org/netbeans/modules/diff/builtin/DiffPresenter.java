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
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.spi.diff.*;

import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.DefaultPropertyModel;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.InstanceDataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

import java.awt.*;


//import java.awt.event.ItemEvent;
//import java.awt.event.ItemListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;

import javax.swing.*;

/**
 * This panel is to be used as a wrapper for diff visualizers.
 *
 * @author   Martin Entlicher
 * @version  $Revision$, $Date$
 */
public class DiffPresenter extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROP_PROVIDER = "provider";     // NOI18N
    public static final String PROP_VISUALIZER = "visualizer"; // NOI18N

    public static final String PROP_TOOLBAR = "DiffPresenter.toolbarPanel"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private DiffPresenter.Info diffInfo;
    private DiffProvider defaultProvider;
    private DiffVisualizer defaultVisualizer;
    private JComponent progressPanel;

    /** Interruptible (to be able to drop streams of deadlocked external program) request processor. */
    private final RequestProcessor diffRP = new RequestProcessor("Diff", 1, true);

    private RequestProcessor.Task computationTask = diffRP.post( // NOI28N
            new Runnable() {

                @Override
                public void run() {
                }
            });

    private boolean added;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JPanel jPanel1;
    javax.swing.JLabel providerLabel;
    javax.swing.JPanel servicesPanel;
    javax.swing.JPanel toolbarPanel;
    javax.swing.JLabel visualizerLabel;
    javax.swing.JPanel visualizerPanel;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates <i>just computing diff</i> presenter. The mode is left on {@link #initWithDiffInfo} call.
     */
    public DiffPresenter() {
        final String label = NbBundle.getMessage(DiffPresenter.class, "diff.prog");
        final ProgressHandle progress = ProgressHandleFactory.createHandle(label);
        progressPanel = ProgressHandleFactory.createProgressComponent(progress);
        add(progressPanel);
        progress.start();
    }

    /**
     * Creates new DiffPresenter with given content.
     *
     * @param  diffInfo  DOCUMENT ME!
     */
    public DiffPresenter(final DiffPresenter.Info diffInfo) {
        initWithDiffInfo(diffInfo);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Seta actual diff content. Can be called just once.
     *
     * @param  diffInfo  DOCUMENT ME!
     */
    public final void initWithDiffInfo(final DiffPresenter.Info diffInfo) {
        assert this.diffInfo == null;
        this.diffInfo = diffInfo;
        if (progressPanel != null) {
            remove(progressPanel);
        }
        initComponents();
        initMyComponents();
        providerLabel.getAccessibleContext()
                .setAccessibleDescription(org.openide.util.NbBundle.getMessage(
                        DiffPresenter.class,
                        "ACS_ProviderA11yDesc"));   // NOI18N
        visualizerLabel.getAccessibleContext()
                .setAccessibleDescription(org.openide.util.NbBundle.getMessage(
                        DiffPresenter.class,
                        "ACS_VisualizerA11yDesc")); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        toolbarPanel = new javax.swing.JPanel();
        servicesPanel = new javax.swing.JPanel();
        providerLabel = new javax.swing.JLabel();
        visualizerLabel = new javax.swing.JLabel();
        visualizerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        toolbarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        toolbarPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(toolbarPanel, gridBagConstraints);

        servicesPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            providerLabel,
            org.openide.util.NbBundle.getMessage(DiffPresenter.class, "LBL_Provider"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        servicesPanel.add(providerLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            visualizerLabel,
            org.openide.util.NbBundle.getMessage(DiffPresenter.class, "LBL_Visualizer"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        servicesPanel.add(visualizerLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(servicesPanel, gridBagConstraints);

        visualizerPanel.setLayout(new java.awt.BorderLayout());

        visualizerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(visualizerPanel, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     */
    private void initMyComponents() {
        PropertyDescriptor pd;
        PropertyModel model;
        PropertyPanel panel;
        java.awt.GridBagConstraints gridBagConstraints;
        final FileObject services = FileUtil.getConfigFile("Services");
        final DataFolder df = DataFolder.findFolder(services);
        final Object editor = PropertyEditorManager.findEditor(Object.class);
        if (diffInfo.isChooseProviders() && (editor != null)) {
            try {
                pd = new PropertyDescriptor(PROP_PROVIDER, getClass());
            } catch (java.beans.IntrospectionException intrex) {
                return;
            }
            pd.setPropertyEditorClass(editor.getClass());
            // special attributes to the property editor
            pd.setValue("superClass", DiffProvider.class);
            pd.setValue("suppressCustomEditor", Boolean.TRUE);
            final FileObject providersFO = services.getFileObject("DiffProviders");
            try {
                final DataObject providersDO = DataObject.find(providersFO);
                final Node providersNode = providersDO.getNodeDelegate();
                pd.setValue("node", providersNode);
            } catch (DataObjectNotFoundException donfex) {
            }
            pd.setValue(ExPropertyEditor.PROPERTY_HELP_ID, "org.netbeans.modules.diff.DiffPresenter.providers");
            model = new DefaultPropertyModel(this, pd);
            panel = new PropertyPanel(model, PropertyPanel.PREF_INPUT_STATE);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            if (!diffInfo.isChooseVisualizers()) {
                gridBagConstraints.weightx = 1.0;
            }
            gridBagConstraints.gridx = 1;
            servicesPanel.add(panel, gridBagConstraints);
            providerLabel.setLabelFor(panel);
            panel.getAccessibleContext()
                    .setAccessibleName(org.openide.util.NbBundle.getMessage(
                            DiffPresenter.class,
                            "ACS_ProviderPropertyPanelA11yName")); // NOI18N
            panel.getAccessibleContext()
                    .setAccessibleDescription(org.openide.util.NbBundle.getMessage(
                            DiffPresenter.class,
                            "ACS_ProviderPropertyPanelA11yDesc")); // NOI18N
        }
        if (diffInfo.isChooseVisualizers() && (editor != null)) {
            try {
                pd = new PropertyDescriptor(PROP_VISUALIZER, getClass());
            } catch (java.beans.IntrospectionException intrex) {
                return;
            }
            pd.setPropertyEditorClass(editor.getClass());
            // special attributes to the property editor
            pd.setValue("superClass", DiffVisualizer.class);
            pd.setValue("suppressCustomEditor", Boolean.TRUE);
            final FileObject visualizersFO = services.getFileObject("DiffVisualizers");
            try {
                final DataObject visualizersDO = DataObject.find(visualizersFO);
                final Node visualizersNode = visualizersDO.getNodeDelegate();
                pd.setValue("node", visualizersNode);
            } catch (DataObjectNotFoundException donfex) {
            }
            pd.setValue(ExPropertyEditor.PROPERTY_HELP_ID, "org.netbeans.modules.diff.DiffPresenter.visualizers");
            model = new DefaultPropertyModel(this, pd);
            panel = new PropertyPanel(model, PropertyPanel.PREF_INPUT_STATE);
            panel.getAccessibleContext()
                    .setAccessibleName(org.openide.util.NbBundle.getMessage(
                            DiffPresenter.class,
                            "ACS_VisualizerPropertyPanelA11yName")); // NOI18N
            panel.getAccessibleContext()
                    .setAccessibleDescription(org.openide.util.NbBundle.getMessage(
                            DiffPresenter.class,
                            "ACS_VisualizerPropertyPanelA11yDesc")); // NOI18N

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridx = 3;
            servicesPanel.add(panel, gridBagConstraints);
            visualizerLabel.setLabelFor(panel);
        }
        providerLabel.setVisible(diffInfo.isChooseProviders() && (editor != null));
        visualizerLabel.setVisible(diffInfo.isChooseVisualizers() && (editor != null));
        servicesPanel.setVisible((diffInfo.isChooseProviders() || diffInfo.isChooseVisualizers()) && (editor != null));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DiffProvider getProvider() {
        return defaultProvider;
    }

    /**
     * Set the diff provider and update the view.
     *
     * @param  p  DOCUMENT ME!
     */
    public void setProvider(final DiffProvider p) {
        this.defaultProvider = (DiffProvider)p;

        if (added) {
            asyncDiff((DiffProvider)p, defaultVisualizer);
            setDefaultDiffService(p, "Services/DiffProviders"); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public DiffVisualizer getVisualizer() {
        return defaultVisualizer;
    }

    /**
     * Set the diff visualizer and update the view.
     *
     * @param  v  DOCUMENT ME!
     */
    public void setVisualizer(final DiffVisualizer v) {
        this.defaultVisualizer = (DiffVisualizer)v;

        if (added) {
            asyncDiff(defaultProvider, (DiffVisualizer)v);
            setDefaultDiffService(v, "Services/DiffVisualizers"); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ds      DOCUMENT ME!
     * @param  folder  DOCUMENT ME!
     */
    private static void setDefaultDiffService(final Object ds, final String folder) {
        // System.out.println("setDefaultDiffService("+ds+")");
        final FileObject services = FileUtil.getConfigFile(folder);
        final DataFolder df = DataFolder.findFolder(services);
        final DataObject[] children = df.getChildren();
        // System.out.println("  Got children.");
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof InstanceDataObject) {
                final InstanceDataObject ido = (InstanceDataObject)children[i];
                if (ido.instanceOf(ds.getClass())) {
                    // System.out.println("  Found an instance of my class.");
                    try {
                        if (ds.equals(ido.instanceCreate())) {
                            // System.out.println("  Have it, settings the order.");
                            df.setOrder(new DataObject[] { ido });
                            break;
                        }
                    } catch (java.io.IOException ioex) {
                    } catch (ClassNotFoundException cnfex) {
                    }
                }
            }
        }
    }

    /*
     * public void addProvidersChangeListener(PropertyChangeListener l) {
     * propSupport.addPropertyChangeListener(PROP_PROVIDER, l); }
     *
     * public void removeProvidersChangeListener(PropertyChangeListener l) {
     * propSupport.removePropertyChangeListener(PROP_PROVIDER, l); }
     *
     * public void addVisualizersChangeListener(PropertyChangeListener l) {
     * propSupport.addPropertyChangeListener(PROP_VISUALIZER, l); }
     *
     * public void removeVisualizersChangeListener(PropertyChangeListener l) {
     * propSupport.removePropertyChangeListener(PROP_VISUALIZER, l); }
     */

    /* Start lazy diff computation */
    @Override
    public void addNotify() {
        super.addNotify();
        added = true;
        asyncDiff(defaultProvider, defaultVisualizer);
    }

    /* On close kill background task. */
    @Override
    public void removeNotify() {
        super.removeNotify();
        computationTask.cancel();
    }

    /**
     * Asynchronously computes and shows diff.
     *
     * @param  p  DOCUMENT ME!
     * @param  v  DOCUMENT ME!
     */
    private synchronized void asyncDiff(final DiffProvider p, final DiffVisualizer v) {
        if (v == null) {
            return;
        }

        Difference[] diffs;
        if (p != null) {
            diffs = diffInfo.getInitialDifferences();
            if (diffs == null) {
                final JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                final String message = NbBundle.getMessage(DiffPresenter.class, "BK0001");
                final JLabel label = new JLabel(message);
                label.setHorizontalAlignment(JLabel.CENTER);
                panel.add(label, BorderLayout.CENTER);
                setVisualizer(panel);
            }
        } else {
            diffs = diffInfo.getDifferences();
        }

        final Difference[] fdiffs = diffs;
        final Runnable computation = new Runnable() {

                @Override
                public void run() {
                    try {
                        Difference[] adiffs = fdiffs;
                        final String message = NbBundle.getMessage(DiffPresenter.class, "BK0001");
                        final ProgressHandle ph = ProgressHandleFactory.createHandle(message);
                        if (adiffs == null) {
                            try {
                                ph.start();
                                adiffs = p.computeDiff(
                                        diffInfo.createFirstReader(),
                                        diffInfo.createSecondReader());
                            } finally {
                                ph.finish();
                            }
                        }

                        if (adiffs == null) {
                            return;
                        }

                        final Difference[] fadiffs = adiffs;
                        javax.swing.SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        viewVisualizer(v, fadiffs);
                                    } catch (IOException ioex) {
                                        ErrorManager.getDefault().notify(ErrorManager.USER, ioex);
                                    }
                                }
                            });
                    } catch (InterruptedIOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.USER, ex);
                    }
                }
            };

        computationTask.cancel();
        computationTask = diffRP.post(computation);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   v      DOCUMENT ME!
     * @param   diffs  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private void viewVisualizer(final DiffVisualizer v, final Difference[] diffs) throws IOException {
        assert SwingUtilities.isEventDispatchThread();
        final Component c = v.createView(
                diffs,
                diffInfo.getName1(),
                diffInfo.getTitle1(),
                diffInfo.createFirstReader(),
                diffInfo.getName2(),
                diffInfo.getTitle2(),
                diffInfo.createSecondReader(),
                diffInfo.getMimeType());
        setVisualizer((JComponent)c);
        final TopComponent tp = diffInfo.getPresentingComponent();
        if (tp != null) {
            tp.setName(c.getName());
            if (c instanceof TopComponent) {
                final TopComponent vtp = (TopComponent)c;
                tp.setToolTipText(vtp.getToolTipText());
                tp.setIcon(vtp.getIcon());
            }
        }
        c.requestFocus();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  visualizer  DOCUMENT ME!
     */
    private void setVisualizer(final JComponent visualizer) {
        visualizerPanel.removeAll();
        if (visualizer != null) {
            toolbarPanel.removeAll();
            final JComponent toolbar = (JComponent)visualizer.getClientProperty(PROP_TOOLBAR);
            if (toolbar != null) {
                toolbarPanel.add(toolbar);
            }
            visualizerPanel.add(visualizer, java.awt.BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * This class contains informations about the differences.
     *
     * @version  $Revision$, $Date$
     */
    public abstract static class Info extends Object {

        //~ Instance fields ----------------------------------------------------

        private String name1;
        private String name2;
        private String title1;
        private String title2;
        private String mimeType;
        private boolean chooseProviders;
        private boolean chooseVisualizers;
        private TopComponent tp;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new Info object.
         *
         * @param  name1              DOCUMENT ME!
         * @param  name2              DOCUMENT ME!
         * @param  title1             DOCUMENT ME!
         * @param  title2             DOCUMENT ME!
         * @param  mimeType           DOCUMENT ME!
         * @param  chooseProviders    DOCUMENT ME!
         * @param  chooseVisualizers  DOCUMENT ME!
         */
        public Info(final String name1,
                final String name2,
                final String title1,
                final String title2,
                final String mimeType,
                final boolean chooseProviders,
                final boolean chooseVisualizers) {
            this.name1 = name1;
            this.name2 = name2;
            this.title1 = title1;
            this.title2 = title2;
            this.mimeType = mimeType;
            this.chooseProviders = chooseProviders;
            this.chooseVisualizers = chooseVisualizers;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getName1() {
            return name1;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getName2() {
            return name2;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getTitle1() {
            return title1;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getTitle2() {
            return title2;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getMimeType() {
            return mimeType;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isChooseProviders() {
            return chooseProviders;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isChooseVisualizers() {
            return chooseVisualizers;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Difference[] getDifferences() {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Difference[] getInitialDifferences() {
            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  FileNotFoundException  DOCUMENT ME!
         */
        public abstract Reader createFirstReader() throws FileNotFoundException;

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  FileNotFoundException  DOCUMENT ME!
         */
        public abstract Reader createSecondReader() throws FileNotFoundException;

        /**
         * DOCUMENT ME!
         *
         * @param  tp  DOCUMENT ME!
         */
        public void setPresentingComponent(final TopComponent tp) {
            this.tp = tp;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public TopComponent getPresentingComponent() {
            return tp;
        }
    }
}
