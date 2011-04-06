/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.custom.visualdiff;

import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;

import java.awt.BorderLayout;
import java.awt.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import javax.swing.SwingWorker;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class DiffPanel extends javax.swing.JPanel {

    //~ Instance fields --------------------------------------------------------

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private DiffView view;
    private String jsonLeft;
    private String jsonRight;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DiffPanel object.
     */
    public DiffPanel() {
        initComponents();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void update() {
        if ((jsonLeft == null) || (jsonRight == null)) {
            log.warn("during update: one json object was null");
        } else {
            new SwingWorker<Component, Void>() {

                    @Override
                    protected Component doInBackground() throws Exception {
                        final StreamSource source1 = new StreamSource() {

                                @Override
                                public String getName() {
                                    return "name";
                                }

                                @Override
                                public String getTitle() {
                                    return "title";
                                }

                                @Override
                                public String getMIMEType() {
                                    return "text/javascript";
                                }

                                @Override
                                public Reader createReader() throws IOException {
                                    return new StringReader(jsonLeft);
                                }

                                @Override
                                public Writer createWriter(final Difference[] conflicts) throws IOException {
                                    return null;
                                }
                            };

                        final StreamSource source2 = new StreamSource() {

                                @Override
                                public String getName() {
                                    return "name2";
                                }

                                @Override
                                public String getTitle() {
                                    return "title2";
                                }

                                @Override
                                public String getMIMEType() {
                                    return "text/javascript";
                                }

                                @Override
                                public Reader createReader() throws IOException {
                                    return new StringReader(jsonRight);
                                }

                                @Override
                                public Writer createWriter(final Difference[] conflicts) throws IOException {
                                    return null;
                                }
                            };

                        return Diff.getDefault().createDiff(source1, source2).getComponent();
                    }

                    @Override
                    protected void done() {
                        try {
                            final Component result = get();
                            removeAll();
                            add(result, BorderLayout.CENTER);
                        } catch (Exception e) {
                            log.error("error during update of diff component", e);
                        }
                    }
                }.execute();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getJsonLeft() {
        return jsonLeft;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  jsonLeft  DOCUMENT ME!
     */
    public void setJsonLeft(final String jsonLeft) {
        this.jsonLeft = jsonLeft;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getJsonRight() {
        return jsonRight;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  jsonRight  DOCUMENT ME!
     */
    public void setJsonRight(final String jsonRight) {
        this.jsonRight = jsonRight;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        setLayout(new java.awt.BorderLayout());
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     */
    public void clear() {
        removeAll();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
