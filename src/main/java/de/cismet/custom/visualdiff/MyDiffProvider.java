/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.custom.visualdiff;

import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>DiffProvider</code> which uses the custom <code>HuntDiff</code> class to compute differences of two readers.
 *
 * <p>The <code>DiffView</code> obtained by calling <code>Diff.getDefault().createDiff(StreamSource,
 * StreamSource)</code> is an instance of <code>EditableDiffView</code>. In order to compute the differences, <code>
 * EditableDiffView</code> invokes <code>DiffModuleConfig.getDefault().getDefaultDiffProvider()</code> to obtain a
 * default <code>DiffProvider</code>. But since Netbeans' <code>DiffModuleConfig</code> class only returns instances of
 * the Netbeans classes <code>BuiltInDiffProvider</code> and <code>CmdLineDiffProvider</code> the class <code>
 * EditableDiffView</code> had to be changed. This is why the sources of the package <code>
 * org.netbeans.modules.diff.builtin</code> are included in this project. Now <code>EditableDiffView</code> calls the
 * constructor of <code>MyDiffProvider</code> to compute the differences.</p>
 *
 * @author   jweintraut
 * @version  $Revision$, $Date$
 */
public class MyDiffProvider extends DiffProvider {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Difference[] computeDiff(final Reader reader1, final Reader reader2) throws IOException {
        return HuntDiff.diff(getLines(reader1), getLines(reader2), true);
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
    private String[] getLines(final Reader reader) throws IOException {
        final List<String> result = new ArrayList<String>();
        final BufferedReader bufferedReader = new BufferedReader(reader);

        try {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                result.add(line);
            }
        } finally {
            bufferedReader.close();
        }

        return result.toArray(new String[0]);
    }
}
