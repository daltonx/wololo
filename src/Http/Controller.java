package Http;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class Controller {
    private Map<String, BiConsumer<Request, Response>> routes = new HashMap<>();
    Authorization tokenVerifier;
    boolean authorize;
    ArrayList<String> whitelist;
    protected Controller (boolean authorize, String secretKey, ArrayList<String> whitelist) {
        this.authorize = authorize;
        this.whitelist = whitelist;

        if (authorize) {
            String authRoute = Objects.requireNonNullElse(System.getenv("AUTH_ROUTE"), "/authorize");
            int tokenLifetime = Integer.parseInt(Objects.requireNonNullElse(System.getenv("TOKEN_LIFETIME"), "60"));
            whitelist.add(authRoute);
            tokenVerifier = new Authorization(secretKey);
            get(authRoute, (Request req, Response res) -> {
                res.text(tokenVerifier.generateToken(tokenLifetime));
            });
        }
    }

    protected boolean authorized (Request req) {
        String authorization = req.headers.get("authorization");
        if (authorization == null)
            return false;
        int tokenIndex = authorization.indexOf("Bearer ");
        if (tokenIndex == -1)
            return false;
        return tokenVerifier.verifyToken(authorization.substring(7));
    }
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
            if (authorize) {
                if (whitelist.contains(req.uri) || authorized(req)) {
                    handler.accept(req, res);
                } else {
                    res.status(401);
                }
            } else {
                handler.accept(req, res);
            }
        } else {
            res.status(404);
        }
    }
}
