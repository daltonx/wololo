package Office;

import com.sun.star.comp.helper.ComponentContext;
import com.sun.star.comp.helper.ComponentContextEntry;
import com.sun.star.comp.loader.JavaLoader;
import com.sun.star.comp.servicemanager.ServiceManager;
import com.sun.star.container.XSet;
import com.sun.star.lang.XInitialization;
import com.sun.star.loader.XImplementationLoader;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import java.util.HashMap;

public class Bootstrap {
    static XComponentContext createComponentContext () throws Exception {
        ServiceManager serviceManager = new ServiceManager();
        XImplementationLoader xImplementationLoader = UnoRuntime.queryInterface(XImplementationLoader.class, new JavaLoader());
        XInitialization xInitialization = UnoRuntime.queryInterface(XInitialization.class, xImplementationLoader);
        xInitialization.initialize(new Object[]{serviceManager});

        HashMap contextEntries = new HashMap(1);
        contextEntries.put("/singletons/com.sun.star.lang.theServiceManager", new ComponentContextEntry(null, serviceManager));

        ComponentContext xLocalContext = new ComponentContext(contextEntries, null);
        serviceManager.setDefaultContext(xLocalContext);
        XSet xSet = UnoRuntime.queryInterface(XSet.class, serviceManager);

        xSet.insert(xImplementationLoader.activate("com.sun.star.comp.urlresolver.UrlResolver", null, null, null));
        xSet.insert(xImplementationLoader.activate("com.sun.star.comp.bridgefactory.BridgeFactory", null, null, null));
        xSet.insert(xImplementationLoader.activate("com.sun.star.comp.connections.Connector", null, null, null));
        xSet.insert(xImplementationLoader.activate("com.sun.star.comp.connections.Acceptor", null, null, null));

        return xLocalContext;
    }
}
