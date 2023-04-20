package Http;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import Office.*;

public class Controller {
    private Map<String, BiConsumer<Request, Response>> routes = new HashMap<>();
    Controller () {
        String minInstances = System.getenv("MIN_INSTANCES");
        Daemon daemon = new Daemon(minInstances != null ? Integer.parseInt(minInstances) : 5);

        get("/", (Request req, Response res) -> {
            res.text("WOLOLO");
        });

        get("/status", (Request req, Response res) -> {
            String data = "";
            for (Instance instance: daemon.instances) {
                data += String.format("<div>%s - %s - %d RUNS<div>", instance.pipeName, instance.state, instance.runs);
            }
            res.text(data);
        });

        post("/office2pdf", (Request req, Response res) -> {
            Instance instance = daemon.getInstance();
            if (instance != null) {
                Converter converter = new Converter(req, res, instance);
                Thread t = new Thread(converter::run);
                t.start();
            } else {
                req.ready = false;
            }
        });
    }

    public void get (String route, BiConsumer<Request, Response> handler) {
        routes.put("GET@" + route, handler);
    }
    public void post (String route, BiConsumer<Request, Response> handler) {
        routes.put("POST@" + route, handler);
    }

    public void handle (Request req, Response res) {
        String route = req.method + "@" + req.uri;
        BiConsumer<Request, Response> handler = routes.get(route);

        if (handler != null) {
            req.ready = true;
            handler.accept(req, res);
        } else {
            res.status(404);
        }
    }
}
