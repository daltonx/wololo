package Office;

import Http.Request;
import Http.Response;
import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.bridge.UnoUrlResolver;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Random;

public class Instance {
    private final String sOffice = System.getenv("SOFFICE");
    public int queued = 0;
    public int runs = 0;
    private long lastActivity;
    private String pipeName;
    private Path userInstallation;
    private boolean alive = false;
    private boolean connected = false;
    final int TIMEOUT = 60000;
    private XDesktop Desktop;
    private XDispatchProvider dispatchProvider;
    private final PropertyValue[] documentProperties = new PropertyValue[]{
        new PropertyValue("ReadOnly", -1, true, PropertyState.DIRECT_VALUE),
        new PropertyValue("OpenNewView", -1, true, PropertyState.DIRECT_VALUE),
        new PropertyValue("Silent", -1, true, PropertyState.DIRECT_VALUE),
        //new PropertyValue("Hidden", -1, true, PropertyState.DIRECT_VALUE),
    };

    public Instance() {}

    public Instance(String pipeName) {
        this.pipeName = pipeName;
    }

    public State getState () {
        long now = System.currentTimeMillis();
        boolean timedOut = (lastActivity + TIMEOUT) < now;

        if (!alive)
            return State.DRAFT;

        if (!connected)
            return State.IDLE;

        if (timedOut)
            return State.TRASH;

        if (runs >= 10) {
            if (queued == 0)
                return State.TRASH;
            return State.SICK;
        }

        if (queued >= 5)
            return State.BUSY;

        return State.READY;
    }

    public String getTitle () {
        return String.format("%s - %s", pipeName, getState());
    }

    public void start () {
        try {
            _start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void connect () {
        try {
            long now = System.currentTimeMillis();
            if (now - lastActivity > 2000)
                _connect();
        } catch (Exception e) {
            //System.out.println(e);
            //throw new RuntimeException(e);
        }
    }

    private String getUrl () {
        return "uno:pipe,name=" + pipeName + ";urp;StarOffice.ComponentContext";
    }

    private void _start () throws IOException {
        Random random = new Random();
        pipeName = "uno" + (random.nextLong() & Long.MAX_VALUE);
        userInstallation = Util.newTempPath("soffice_", "");

        String cmd[] = new String[]{
                sOffice,
                "--headless",
                "--nologo",
                "--nodefault",
                "--norestore",
                "--nolockcheck",
                "--accept=pipe,name=" + pipeName + ";urp;",
                "-env:UserInstallation=file://" + userInstallation,
        };

        Process process = Runtime.getRuntime().exec(cmd);
        lastActivity = System.currentTimeMillis();

        alive = true;
    }

    private void _connect () throws Exception {
        lastActivity = System.currentTimeMillis();
        XComponentContext xLocalContext = Bootstrap.createComponentContext();
        XUnoUrlResolver xUnoUrlResolver = UnoUrlResolver.create(xLocalContext);

        String url = getUrl();

        Object context = xUnoUrlResolver.resolve(url);
        XComponentContext xContext = UnoRuntime.queryInterface(XComponentContext.class, context);

        if (xContext == null)
            throw new Exception("No component context");

        XMultiComponentFactory componentFactory = xContext.getServiceManager();
        Object _Desktop = componentFactory.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
        Desktop = UnoRuntime.queryInterface(XDesktop.class, _Desktop);

        dispatchProvider = UnoRuntime.queryInterface(XDispatchProvider.class, Desktop);
        connected = true;
    }

    private void deleteProfile () throws IOException {
        deleteProfile(userInstallation);
    }

    private static void deleteProfile (Path userInstallation) throws IOException, UncheckedIOException {
        Files.walk(userInstallation)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public boolean terminate () {
        if (getState() != State.TRASH)
            return false;

        try {
            Desktop.terminate();
            System.out.println(String.format("INSTANCE %s - KILLED", getTitle()));
            deleteProfile();
        } catch (IOException e) {
            System.out.println("Failed to kill a instance");
            //throw new RuntimeException(e);
        }
        return true;
    }

    public XDispatch queryDispatch (URL url, String s, int i) {
        return dispatchProvider.queryDispatch(url, s, i);
    }

    public void attach () {
        System.out.println(String.format("INSTANCE %s - ATTACHED", getTitle()));
        runs++;
        queued++;
    }

    public void detach () {
        System.out.println(String.format("INSTANCE %s - DETACHED", getTitle()));
        queued--;
    }

    public int getHealth () {
        return queued + runs;
    }

    public static void singleTask (Request req, Response res, boolean convert) throws IOException, InterruptedException {
        String sOffice = System.getenv("SOFFICE");

        Path userInstallation = Util.newTempPath("soffice_", "");

        File inputFile = Util.writeTempFile("input_", ".tmp", req.body);
        inputFile.deleteOnExit();

        String cmd[] = new String[]{
                //"timeout 40s",
                sOffice,
                "--headless",
                "--writer",
                "--invisible",
                "--nodefault",
                "--nofirststartwizard",
                "--nolockcheck",
                "--nologo",
                "--norestore",
                convert ? "--convert-to" : "--print-to-file",
                convert ? "pdf" : "",
                inputFile.getPath(),
                "--outdir", inputFile.getParent(),
                "-env:UserInstallation=file://" + userInstallation.toString(),
                //"LANG=pt_BR.UTF8",
        };

        Process process = Runtime.getRuntime().exec(cmd);
        //pipe(process.getInputStream(), System.out, "");
        //pipe(process.getErrorStream(), System.err, "");
        process.waitFor();

        if (process.exitValue() == 0) {
            Path outputFile = Path.of(inputFile.getPath().replace(".tmp", ".pdf"));
            res.file(outputFile.toString(), "application/pdf");
            Files.delete(outputFile);
        } else {
            res.status(500);
        }

        inputFile.delete();
        deleteProfile(userInstallation);
    }
}
