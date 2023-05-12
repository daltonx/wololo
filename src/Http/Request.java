package Http;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Request {
    String method;
    String uri;
    String version;
    Map<String, String> headers = new HashMap<>();
    public byte[] body;
    enum State {
        REQUEST, HEADER, BODY, END;
    }
    private State state = State.REQUEST;
    private ByteTokenizer tokenizer = new ByteTokenizer();
    private final byte[] CRLF = "\r\n".getBytes();
    private final String SPACE = " ";
    private final String COLON = ": ";
    public boolean ready = false;
    public boolean pushBuffer (ByteBuffer buffer) {
        tokenizer.add(buffer);
        return parse();
    }

    private boolean _parse () {
        byte[] token;

        switch (state) {
            case REQUEST:
                token = tokenizer.next(CRLF);
                if (token == null) return false;
                parseRequest(token);
                System.out.println(method + " " + uri);
                return true;
            case HEADER:
                token = tokenizer.next(CRLF);
                if (token == null) return false;
                return parseHeader(token);
            case BODY:
                token = tokenizer.next(Integer.parseInt(headers.get("content-length")));
                if (token == null) return false;
                return parseBody(token);
            case END:
                return true;
        }
        return true;
    }

    public boolean parse () {
        while (state != State.END) {
            if (!_parse()) {
                return false;
            }
        }
        return true;
    }

    private void parseRequest (byte[] token) {
        String[] parts = new String(token).split(SPACE);
        method = parts[0];
        uri = parts[1];
        version = parts[2];

        state = State.HEADER;
    }

    private boolean parseHeader (byte[] token) {
        if (token.length == 0) {
            state = headers.containsKey("content-length") ? state.BODY : state.END;
            return true;
        }

        String[] parts = new String(token).split(COLON);
        headers.put(parts[0].toLowerCase(), parts[1]);
        return true;
    }

    private boolean parseBody (byte[] token) {
        body = token;
        state = State.END;
        return true;
    }
}
