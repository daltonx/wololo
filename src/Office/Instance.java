package Office;

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
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XSynchronousDispatch;
import com.sun.star.lang.*;
import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
import com.sun.star.loader.XImplementationLoader;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.sun.star.util.XCloseable;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

public class Instance {
    private PropertyValue[] documentProperties;
    public State state = State.STOPPED;
    public long lastActivity;
    public int runs = 0;
    public Process process;
    public XComponentContext xContext;
    public XMultiComponentFactory componentFactory;
    public Object Desktop;
    public String pipeName;
    public ComponentContext xLocalContext;
    public XComponentLoader xCompLoader;
    private XDispatchProvider dispatchProvider;
    public XBridgeFactory xbf;
    private Path userInstallation;

    public Instance() {
        //properties ReadOnly, OpenNewView, Hidden, Silent should be added, as in dispatchwatcher.cxx
        documentProperties = new PropertyValue[]{
                new PropertyValue("ReadOnly", -1, true, PropertyState.DIRECT_VALUE),
                new PropertyValue("OpenNewView", -1, true, PropertyState.DIRECT_VALUE),
                new PropertyValue("Silent", -1, true, PropertyState.DIRECT_VALUE),
                //new PropertyValue("Hidden", -1, true, PropertyState.DIRECT_VALUE),
        };
    }

    public void start () {
        try {
            _start();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void _start () throws IOException {
        Random random = new Random();
        pipeName = "uno" + Long.toString(random.nextLong() & Long.MAX_VALUE);

        String sOffice = System.getenv("SOFFICE");

        userInstallation = Files.createTempDirectory("soffice_");

        String cmd[] = new String[]{
                sOffice,
                "--headless",
                "--nologo",
                "--nodefault",
                "--norestore",
                "--nolockcheck",
                "--accept=pipe,name=" + pipeName + ";urp;",
                "-env:UserInstallation=file://" + userInstallation.toString(),
        };

        System.out.println(userInstallation.toString());
        process = Runtime.getRuntime().exec(cmd);
        lastActivity = System.currentTimeMillis();
        state = State.STARTED;

        pipe(process.getInputStream(), System.out, "");
        pipe(process.getErrorStream(), System.err, "");
    }
    private static void pipe(final InputStream in, final PrintStream out, final String prefix) {
        (new Thread("Pipe: " + prefix) {
            public void run() {
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                    while(true) {
                        String s = r.readLine();
                        if (s == null) {
                            break;
                        }

                        out.println(prefix + s);
                    }
                } catch (UnsupportedEncodingException var3) {
                    var3.printStackTrace(System.err);
                } catch (IOException var4) {
                    var4.printStackTrace(System.err);
                }

            }
        }).start();
    }

    private void deleteProfile () throws IOException {
        Files.walk(userInstallation)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * @TODO clear /tmp dirs and files properly
     */
    public void kill () {
        try {
            System.err.println(String.format("INSTANCE %s - KILLED", pipeName));
            process.destroyForcibly();
            deleteProfile();
        } catch (IOException e) {
            System.out.print(e);
            //throw new RuntimeException(e);
        }
    }

    private void insertFactories(XSet xSet, XImplementationLoader xImpLoader) throws Exception {
        xSet.insert(xImpLoader.activate("com.sun.star.comp.urlresolver.UrlResolver", null, null, null));
        xSet.insert(xImpLoader.activate("com.sun.star.comp.bridgefactory.BridgeFactory", null, null, null));
        xSet.insert(xImpLoader.activate("com.sun.star.comp.connections.Connector", null, null, null));
        xSet.insert(xImpLoader.activate("com.sun.star.comp.connections.Acceptor", null, null, null));
    }

    private XComponentContext createComponentContext () throws Exception {
        ServiceManager serviceManager = new ServiceManager();
        XImplementationLoader xImplementationLoader = UnoRuntime.queryInterface(XImplementationLoader.class, new JavaLoader());
        XInitialization xInitialization = UnoRuntime.queryInterface(XInitialization.class, xImplementationLoader);
        xInitialization.initialize(new Object[]{serviceManager});

        HashMap contextEntries = new HashMap(1);
        contextEntries.put("/singletons/com.sun.star.lang.theServiceManager", new ComponentContextEntry(null, serviceManager));

        xLocalContext = new ComponentContext(contextEntries, null);
        serviceManager.setDefaultContext(xLocalContext);
        XSet xSet = UnoRuntime.queryInterface(XSet.class, serviceManager);

        insertFactories(xSet, xImplementationLoader);

        return xLocalContext;
    }

    public String getUrl () {
        return "uno:pipe,name=" + this.pipeName + ";urp;StarOffice.ComponentContext";
    }

    public void connect () {
        try {
            _connect();
        } catch (Throwable e) {
            //System.err.println("Connection failed, retry");
        }
    }
    private void _connect () throws Exception {
        lastActivity = System.currentTimeMillis();
        XComponentContext xLocalContext = createComponentContext();
        XUnoUrlResolver xUnoUrlResolver = UnoUrlResolver.create(xLocalContext);

        String url = getUrl();

        Object context = xUnoUrlResolver.resolve(url);
        xContext = UnoRuntime.queryInterface(XComponentContext.class, context);

        if (xContext == null) {
            throw new Exception("No component context!");
        }

        componentFactory = xContext.getServiceManager();
        Desktop = componentFactory.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
        xCompLoader = UnoRuntime.queryInterface(XComponentLoader.class, Desktop);
        dispatchProvider = UnoRuntime.queryInterface(XDispatchProvider.class, Desktop);
        state = State.READY;

        Object bf = componentFactory.createInstanceWithContext("com.sun.star.bridge.BridgeFactory", xContext);
        xbf = UnoRuntime.queryInterface(XBridgeFactory.class , bf);
    }

    public void release (XComponent document) {
        if (state != State.SICK) {
            try {
                XCloseable xCloseable = UnoRuntime.queryInterface(XCloseable.class, document);
                xCloseable.close(true);
                state = State.READY;
                System.out.println(String.format("INSTANCE %s - RELEASED", pipeName));
            } catch (Throwable e) {
                state = State.DEAD;
            }
        } else {
            state = State.DEAD;
        }
    }

    public void lock () {
        runs++;
        lastActivity = System.currentTimeMillis();
        state = runs > 5 ? State.SICK : State.BUSY;
        System.out.println(String.format("INSTANCE %s - LOCKED", pipeName));
    }

    public XComponent loadDocument (String path) {
        try {
            return _loadDocument(path);
        } catch (com.sun.star.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
    private XComponent _loadDocument (String path) throws com.sun.star.io.IOException {
        return xCompLoader.loadComponentFromURL(path, "_blank", 0, documentProperties);
    }

    public XComponent loadDocument2 (String path) {
        URL url = new URL();
        url.Complete = path;

        XDispatch dispatch = dispatchProvider.queryDispatch(url, "_blank", 0);
        XSynchronousDispatch synchronousDispatch = UnoRuntime.queryInterface(XSynchronousDispatch.class, dispatch);

        Object doc = synchronousDispatch.dispatchWithReturnValue(url, documentProperties);
        XComponent xDoc = UnoRuntime.queryInterface(XComponent.class, doc);

        return xDoc;
    }


    public XComponent loadDocumentBuffer (byte[] buffer) {
        try {
            return _loadDocumentBuffer(buffer);
        } catch (Throwable e) {
            System.err.println(e);
            return null;
        }
    }

    public XComponent _loadDocumentBuffer (byte[] buffer) throws com.sun.star.io.IOException {
        ByteArrayToXInputStreamAdapter stream = new ByteArrayToXInputStreamAdapter(buffer);
        PropertyValue inputStream = new PropertyValue();
        inputStream.Name = "InputStream";
        inputStream.Value = stream;

        documentProperties[3] = inputStream;

        return xCompLoader.loadComponentFromURL("private:stream", "_blank", 0, documentProperties);
    }
}
