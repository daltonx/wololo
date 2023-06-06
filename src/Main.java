import Http.Server;
import Office.State;
import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.bridge.UnoUrlResolver;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.comp.helper.ComponentContext;
import com.sun.star.comp.helper.ComponentContextEntry;
import com.sun.star.comp.loader.JavaLoader;
import com.sun.star.comp.servicemanager.ServiceManager;
import com.sun.star.container.XSet;
import com.sun.star.frame.*;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.loader.XImplementationLoader;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.URL;
import com.sun.star.util.XCloseable;
import com.sun.star.view.XPrintable;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class Main {
    public static void main (String [] args) throws Exception, IOException {
        boolean authorize = Objects.equals(System.getenv("AUTHORIZE"), "true");
        String secretKey = Objects.requireNonNullElse(System.getenv("SECRET_KEY"), "SECRET_KEY");
        new Server(authorize, secretKey);
    }
}