package Http;

import Office.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Router extends Controller {
    Router (boolean authorize, String secretKey) {
        super(authorize, secretKey, new ArrayList<String>(List.of("/", "/status")));

        String minInstances = Objects.requireNonNullElse(System.getenv("MIN_INSTANCES"), "2");
        String maxInstances = Objects.requireNonNullElse(System.getenv("MAX_INSTANCES"), "5");

        Daemon daemon = new Daemon(Integer.parseInt(minInstances), Integer.parseInt(maxInstances));

        get("/", (Request req, Response res) -> {
            res.text("WOLOLO v0.6");
        });

        get("/status", (Request req, Response res) -> {
            String data = "";
            for (Instance instance: daemon.instances) {
                data += String.format("<div>%s - %d RUNS - %s QUEUED<div>", instance.getTitle(), instance.runs, instance.queued);
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
