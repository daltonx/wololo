package Office;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.XPrintable;

import java.io.File;
import java.io.IOException;

public class Document {
    private Instance office;
    private XComponent document;

    public Document(Instance office) {
        this.office = office;
    }

    public void loadBuffer (byte[] buffer) {
        document = office.loadDocumentBuffer(buffer);
    }

    public void printBuffer(byte[] buffer) {

    }

    public void load (String path) {
        try {
            _load(path);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    public void print (String path) {
        try {
            _print(path);
        } catch (Exception e) {
            System.err.println(String.format("INSTANCE %s - %s - FAILED TO PRINT", office.pipeName, office.state));
            throw new RuntimeException(e);
        }
    }
    private void _load (String path) throws IOException {
        File sourceFile = new File(path);
        StringBuffer sourceUrl = new StringBuffer("file:///");
        sourceUrl.append(sourceFile.getCanonicalPath().replace("\\", "/"));

        document = office.loadDocument2(sourceUrl.toString());
    }

    private void _print (String path) throws Exception {
        File outputFile = new File(path);
        StringBuffer outputUrl = new StringBuffer("file:///");
        outputUrl.append(outputFile.getCanonicalPath().replace('\\', '/'));

        XPrintable xPrintable = UnoRuntime.queryInterface(XPrintable.class, this.document);

        try {
            xPrintable.print(new PropertyValue[]{
                    new PropertyValue("FileName", -1, outputUrl.toString(), PropertyState.DIRECT_VALUE),
                    new PropertyValue("Wait", -1, true, PropertyState.DIRECT_VALUE),
            });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void printBuffer () {
        XPrintable xPrintable = UnoRuntime.queryInterface(XPrintable.class, this.document);

        try {
            xPrintable.print(new PropertyValue[]{
                    new PropertyValue("FileName", -1, "private:stream", PropertyState.DIRECT_VALUE),
                    new PropertyValue("Wait", -1, true, PropertyState.DIRECT_VALUE)
            });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void dispose () {
        office.release(document);
    }
}
