package Office;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XStorable;
import com.sun.star.frame.XSynchronousDispatch;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.URL;
import com.sun.star.util.XCloseable;
import com.sun.star.view.XPrintable;

public class Document {
    private Instance office;
    private XComponent document;
    static final PropertyValue[] documentProperties = new PropertyValue[]{
        new PropertyValue("ReadOnly", -1, true, PropertyState.DIRECT_VALUE),
        new PropertyValue("OpenNewView", -1, true, PropertyState.DIRECT_VALUE),
        new PropertyValue("Silent", -1, true, PropertyState.DIRECT_VALUE),
        //new PropertyValue("Hidden", -1, true, PropertyState.DIRECT_VALUE),
    };

    public Document(Instance office) {
        this.office = office;
    }

    public void load (String path) {
        try {
            URL url = new URL();
            url.Complete = "file://" + path;

            XDispatch xDispatch = office.queryDispatch(url, "_blank", 0);
            XSynchronousDispatch xSynchronousDispatch = UnoRuntime.queryInterface(XSynchronousDispatch.class, xDispatch);

            Object doc = xSynchronousDispatch.dispatchWithReturnValue(url, documentProperties);
            document = UnoRuntime.queryInterface(XComponent.class, doc);
        } catch (Throwable e) {
            System.err.println("Failed to load a document");
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
            System.err.println(String.format("INSTANCE %s - FAILED TO PRINT", office.getTitle()));
            //throw new RuntimeException(e);
        }
    }

    public void convert (String path) {
        try {
            XStorable xStorable = UnoRuntime.queryInterface(XStorable.class, this.document);
            xStorable.storeToURL("file://" + path, new PropertyValue[]{
                    new PropertyValue("FilterName", -1, "writer_pdf_Export", PropertyState.DIRECT_VALUE),
                    new PropertyValue("FilterData", -1, new PropertyValue[]{
                            new PropertyValue("ExportFormFields", -1, false, PropertyState.DIRECT_VALUE),
                    }, PropertyState.DIRECT_VALUE),
            });
        } catch (Exception e) {
            System.err.println(String.format("INSTANCE %s - FAILED TO CONVERT", office.getTitle()));
            //throw new RuntimeException(e);
        }
    }

    public void close () {
        try {
            XCloseable xCloseable = UnoRuntime.queryInterface(XCloseable.class, document);
            xCloseable.close(true);
            office.detach();
        } catch (CloseVetoException e) {
            System.out.println(String.format("INSTANCE %s - FAILED TO CLOSE DOCUMENT", office.getTitle()));
            //throw new RuntimeException(e);
        }
    }
}
