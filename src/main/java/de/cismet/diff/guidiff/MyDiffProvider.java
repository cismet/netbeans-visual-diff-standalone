package de.cismet.diff.guidiff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.util.lookup.ServiceProvider;

//@ServiceProvider(service = DiffProvider.class)
public class MyDiffProvider extends DiffProvider {
    @Override
    public Difference[] computeDiff(Reader reader, Reader reader1) throws IOException {
        return HuntDiff.diff(getLines(reader), getLines(reader1), true);
    }

    private String[] getLines(Reader reader) throws IOException {
        List<String> result = new ArrayList<String>();
        BufferedReader bufferedReader = new BufferedReader(reader);

        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            result.add(line);
        }

        return result.toArray(new String[0]);
    }
}
