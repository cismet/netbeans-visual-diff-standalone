/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.custom.visualdiff;

import org.netbeans.api.diff.DiffView;

import org.openide.util.Exceptions;

import java.awt.BorderLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * This is a test which demonstrates the use of the VisualDiff component.
 *
 * <p>In order to run this test application, make sure that the directory <code>\META-INF\services</code> in the target
 * directory or built jar file contains</p>
 *
 * <ul>
 *   <li><code>org.netbeans.api.diff.Diff</code> containing <code>
 *     org.netbeans.modules.diff.builtin.DefaultDiff</code></li>
 *   <li><code>org.netbeans.spi.diff.DiffControllerProvider</code> containing <code>
 *     org.netbeans.modules.diff.builtin.DefaultDiffControllerProvider</code></li>
 *   <li><code>org.netbeans.spi.diff.DiffVisualizer</code> containing <code>
 *     org.netbeans.modules.diff.builtin.visualizer.TextDiffVisualizer
 *     org.netbeans.modules.diff.builtin.visualizer.editable.EditableDiffVisualizer</code></li>
 * </ul>
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class Testapplication extends javax.swing.JFrame {

    //~ Static fields/initializers ---------------------------------------------

    private static final String MIMETYPE_HTML = "text/html";
    private static final String MIMETYPE_JAVA = "text/x-java";
    private static final String MIMETYPE_JSON = "text/javascript";
    private static final String MIMETYPE_TEXT = "text/plain";

    private static final String FILENAME1_HTML = "src/test/resources/filestodiff/html1.html";
    private static final String FILENAME2_HTML = "src/test/resources/filestodiff/html2.html";
    private static final String FILENAME1_JAVA = "src/test/resources/filestodiff/java1.java";
    private static final String FILENAME2_JAVA = "src/test/resources/filestodiff/java2.java";
    private static final String FILENAME1_JSON = "src/test/resources/filestodiff/json1.json";
    private static final String FILENAME2_JSON = "src/test/resources/filestodiff/json2.json";
    private static final String FILENAME1_TEXT = "src/test/resources/filestodiff/text1.txt";
    private static final String FILENAME2_TEXT = "src/test/resources/filestodiff/text2.txt";

    //~ Instance fields --------------------------------------------------------

    private DiffPanel pnlDiff;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDiffHTMLFiles;
    private javax.swing.JButton btnDiffJSONFiles;
    private javax.swing.JButton btnDiffJavaFiles;
    private javax.swing.JButton btnDiffTextFiles;
    private javax.swing.JButton btnNextDifference;
    private javax.swing.JButton btnPrevDifference;
    private javax.swing.JPanel pnlControls;
    private javax.swing.JSeparator separator;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Testapplication object.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Testapplication() throws Exception {
        initComponents();

        final File file1 = new File(FILENAME1_JSON);
        final File file2 = new File(FILENAME2_JSON);

        System.out.println(file1.toString());
        System.out.println(file2.toString());

        pnlDiff = new DiffPanel();
        pnlDiff.setLeftAndRight(
                this.getLines(new InputStreamReader(new FileInputStream(file1), Charset.defaultCharset())),
            MIMETYPE_TEXT,
            file1.getName(),
            this.getLines(new InputStreamReader(new FileInputStream(file2), Charset.defaultCharset())),
            MIMETYPE_TEXT,
            file2.getName());
        getContentPane().add(pnlDiff, BorderLayout.CENTER);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * A helper method to read the content of a reader and convert it to a String.
     *
     * @param   reader  The reader to read from.
     *
     * @return  A string with all the content provided by reader.
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private String getLines(final Reader reader) throws IOException {
        final StringBuilder result = new StringBuilder();
        final BufferedReader bufferedReader = new BufferedReader(reader);

        try {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
        } finally {
            bufferedReader.close();
        }

        return result.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlControls = new javax.swing.JPanel();
        btnPrevDifference = new javax.swing.JButton();
        btnNextDifference = new javax.swing.JButton();
        separator = new javax.swing.JSeparator();
        btnDiffHTMLFiles = new javax.swing.JButton();
        btnDiffJavaFiles = new javax.swing.JButton();
        btnDiffJSONFiles = new javax.swing.JButton();
        btnDiffTextFiles = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(Testapplication.class, "Testapplication.title")); // NOI18N

        btnPrevDifference.setText(org.openide.util.NbBundle.getMessage(
                Testapplication.class,
                "Testapplication.btnPrevDifference.text")); // NOI18N
        btnPrevDifference.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnPrevDifferenceActionPerformed(evt);
                }
            });
        pnlControls.add(btnPrevDifference);

        btnNextDifference.setText(org.openide.util.NbBundle.getMessage(
                Testapplication.class,
                "Testapplication.btnNextDifference.text")); // NOI18N
        btnNextDifference.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnNextDifferenceActionPerformed(evt);
                }
            });
        pnlControls.add(btnNextDifference);

        separator.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        separator.setPreferredSize(new java.awt.Dimension(2, 23));
        pnlControls.add(separator);

        btnDiffHTMLFiles.setText(org.openide.util.NbBundle.getMessage(
                Testapplication.class,
                "Testapplication.btnDiffHTMLFiles.text")); // NOI18N
        btnDiffHTMLFiles.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnDiffHTMLFilesActionPerformed(evt);
                }
            });
        pnlControls.add(btnDiffHTMLFiles);

        btnDiffJavaFiles.setText(org.openide.util.NbBundle.getMessage(
                Testapplication.class,
                "Testapplication.btnDiffJavaFiles.text")); // NOI18N
        btnDiffJavaFiles.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnDiffJavaFilesActionPerformed(evt);
                }
            });
        pnlControls.add(btnDiffJavaFiles);

        btnDiffJSONFiles.setText(org.openide.util.NbBundle.getMessage(
                Testapplication.class,
                "Testapplication.btnDiffJSONFiles.text")); // NOI18N
        btnDiffJSONFiles.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnDiffJSONFilesActionPerformed(evt);
                }
            });
        pnlControls.add(btnDiffJSONFiles);

        btnDiffTextFiles.setText(org.openide.util.NbBundle.getMessage(
                Testapplication.class,
                "Testapplication.btnDiffTextFiles.text")); // NOI18N
        btnDiffTextFiles.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnDiffTextFilesActionPerformed(evt);
                }
            });
        pnlControls.add(btnDiffTextFiles);

        getContentPane().add(pnlControls, java.awt.BorderLayout.SOUTH);

        final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 729) / 2, (screenSize.height - 706) / 2, 729, 706);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * The action handler for the 'next difference' button. Increases the 'current difference' property of the view.
     *
     * @param  evt  The event to handle.
     */
    private void btnNextDifferenceActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextDifferenceActionPerformed
        if (pnlDiff.getDiffView() != null) {
            final DiffView view = pnlDiff.getDiffView();
            if (view.canSetCurrentDifference()) {
                view.setCurrentDifference((view.getCurrentDifference() + 1) % view.getDifferenceCount());
            }
        }
    }//GEN-LAST:event_btnNextDifferenceActionPerformed

    /**
     * The action handler for the 'previous difference' button. Decreases the 'current difference' property of the view.
     *
     * @param  evt  The event to handle.
     */
    private void btnPrevDifferenceActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevDifferenceActionPerformed
        if (pnlDiff.getDiffView() != null) {
            final DiffView view = pnlDiff.getDiffView();
            if (view.canSetCurrentDifference()) {
                view.setCurrentDifference(((view.getCurrentDifference() == 0) ? (view.getDifferenceCount() - 1)
                                                                              : (view.getCurrentDifference() - 1))
                            % view.getDifferenceCount());
            }
        }
    }//GEN-LAST:event_btnPrevDifferenceActionPerformed

    /**
     * The action handler for the 'HTML' button. Diffs two HTML files.
     *
     * @param  evt  The event to handle.
     */
    private void btnDiffHTMLFilesActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiffHTMLFilesActionPerformed
        final File file1 = new File(FILENAME1_HTML);
        final File file2 = new File(FILENAME2_HTML);
        try {
            pnlDiff.setLeftAndRight(
                    this.getLines(new InputStreamReader(new FileInputStream(file1), Charset.defaultCharset())),
                MIMETYPE_HTML,
                file1.getName(),
                this.getLines(new InputStreamReader(new FileInputStream(file2), Charset.defaultCharset())),
                MIMETYPE_HTML,
                file2.getName());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_btnDiffHTMLFilesActionPerformed

    /**
     * The action handler for the 'Java' button. Diffs two Java files.
     *
     * @param  evt  The event to handle.
     */
    private void btnDiffJavaFilesActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiffJavaFilesActionPerformed
        final File file1 = new File(FILENAME1_JAVA);
        final File file2 = new File(FILENAME2_JAVA);
        try {
            pnlDiff.setLeftAndRight(
                   this.getLines(new InputStreamReader(new FileInputStream(file1), Charset.defaultCharset())),
                MIMETYPE_JAVA,
                file1.getName(),
                this.getLines(new InputStreamReader(new FileInputStream(file2), Charset.defaultCharset())),
                MIMETYPE_JAVA,
                file2.getName());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_btnDiffJavaFilesActionPerformed

    /**
     * The action handler for the 'JSON' button. Diffs two JSON files.
     *
     * @param  evt  The event to handle.
     */
    private void btnDiffJSONFilesActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiffJSONFilesActionPerformed
        final File file1 = new File(FILENAME1_JSON);
        final File file2 = new File(FILENAME2_JSON);
        try {
            pnlDiff.setLeftAndRight(
                    this.getLines(new InputStreamReader(new FileInputStream(file1), Charset.defaultCharset())),
                MIMETYPE_JSON,
                file1.getName(),
                this.getLines(new InputStreamReader(new FileInputStream(file2), Charset.defaultCharset())),
                MIMETYPE_JSON,
                file2.getName());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_btnDiffJSONFilesActionPerformed

    /**
     * The action handler for the 'Text' button. Diffs two text files.
     *
     * @param  evt  The event to handle.
     */
    private void btnDiffTextFilesActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiffTextFilesActionPerformed
        final File file1 = new File(FILENAME1_TEXT);
        final File file2 = new File(FILENAME2_TEXT);
        try {
            pnlDiff.setLeftAndRight(
                    this.getLines(new InputStreamReader(new FileInputStream(file1), Charset.defaultCharset())),
                MIMETYPE_TEXT,
                file1.getName(),
               this.getLines(new InputStreamReader(new FileInputStream(file2), Charset.defaultCharset())),
                MIMETYPE_TEXT,
                file2.getName());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_btnDiffTextFilesActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        new Testapplication().setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
    }
}
