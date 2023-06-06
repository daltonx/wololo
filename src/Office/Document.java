package Office;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.XPrintable;

public class Document {
    private Instance office;
    private XComponent document;

    public Document(Instance office) {
        this.office = office;
    }

    public void load (String path) {
        try {
            document = office.loadDocument("file://" + path);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    public void print (String path) {
        try {
            XPrintable xPrintable = UnoRuntime.queryInterface(XPrintable.class, this.document);

            xPrintable.print(new PropertyValue[]{
                    new PropertyValue("FileName", -1, "file://" + path, PropertyState.DIRECT_VALUE),
                    new PropertyValue("Wait", -1, true, PropertyState.DIRECT_VALUE),
            });
        } catch (Exception e) {
            System.err.println(String.format("INSTANCE %s - %s - FAILED TO PRINT", office.pipeName, office.state));
            throw new RuntimeException(e);
        }
    }

    public void convert (String path) {
        try {
            XStorable xStorable = UnoRuntime.queryInterface(XStorable.class, this.document);
            xStorable.storeToURL("file://" + path, new PropertyValue[]{
                    new PropertyValue("FilterName", -1, "writer_pdf_Export", PropertyState.DIRECT_VALUE),
                    new PropertyValue("FilterData", -1, new PropertyValue[]{}, PropertyState.DIRECT_VALUE),
            });
        } catch (Exception e) {
            System.err.println(String.format("INSTANCE %s - %s - FAILED TO CONVERT", office.pipeName, office.state));
            throw new RuntimeException(e);
        }
    }

    public void dispose () {
        office.release(document);
    }
}
