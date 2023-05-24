import Http.Server;

import java.util.Objects;

public class Main {
    public static void main (String [] args) throws Exception {
        boolean authorize = System.getenv("AUTHORIZE").equals("true");
        String secretKey = Objects.requireNonNullElse(System.getenv("SECRET_KEY"), "SECRET_KEY");
        new Server(authorize, secretKey);
    }
}