import Http.Server;
import com.sun.star.uno.Exception;

import java.io.IOException;
import java.util.Objects;

public class Main {
    public static void main (String [] args) throws Exception, IOException {
        boolean authorize = Objects.equals(System.getenv("AUTHORIZE"), "true");
        String secretKey = Objects.requireNonNullElse(System.getenv("SECRET_KEY"), "SECRET_KEY");
        new Server(authorize, secretKey);
    }
}
