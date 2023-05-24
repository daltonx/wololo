package Http;

import Office.Converter;
import Office.Daemon;
import Office.Instance;

import java.util.ArrayList;
import java.util.List;

public class Router extends Controller {
    Router (boolean authorize, String secretKey) {
        super(authorize, secretKey, new ArrayList<String>(List.of("/", "/status")));

        String minInstances = System.getenv("MIN_INSTANCES");
        Daemon daemon = new Daemon(minInstances != null ? Integer.parseInt(minInstances) : 5);

        get("/", (Request req, Response res) -> {
            res.text("WOLOLO v0.5");
        });

        get("/status", (Request req, Response res) -> {
            String data = "";
            for (Instance instance: daemon.instances) {
                data += String.format("<div>%s - %s - %d RUNS<div>", instance.pipeName, instance.state, instance.runs);
            }
            res.text(data);
        });

        post("/print", (Request req, Response res) -> {
            Instance instance = daemon.getInstance();
            if (instance != null) {
                Converter converter = new Converter(req, res, instance);
                Thread thread = new Thread(converter::print);
                thread.start();
            } else {
                req.ready = false;
            }
        });

        post("/convert", (Request req, Response res) -> {
            Instance instance = daemon.getInstance();
            if (instance != null) {
                Converter converter = new Converter(req, res, instance);
                Thread thread = new Thread(converter::convert);
                thread.start();
            } else {
                req.ready = false;
            }
        });

        post("/legacy/print", (Request req, Response res) -> {
            Converter converter = new Converter(req, res);
            Thread thread = new Thread(converter::legacyPrint);
            thread.start();
        });

        post("/legacy/convert", (Request req, Response res) -> {
            Converter converter = new Converter(req, res);
            Thread thread = new Thread(converter::legacyConvert);
            thread.start();
        });
    }
}
