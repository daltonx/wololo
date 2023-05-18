package Http;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class Controller {
    private Map<String, BiConsumer<Request, Response>> routes = new HashMap<>();

    protected void get (String route, BiConsumer<Request, Response> handler) {
        routes.put("GET@" + route, handler);
    }
    protected void post (String route, BiConsumer<Request, Response> handler) {
        routes.put("POST@" + route, handler);
    }

    protected void handle (Request req, Response res) {
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
