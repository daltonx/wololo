package Http;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class Response {
    private Map<String, String> headers;
    public int status = 200;
    List<byte[]> byteArrays = new ArrayList<>();
    int bufferSize = 0;
    public boolean ready = false;

    public Response () {
        headers = new HashMap<>(Map.of(
                "Content-Type", "text/html; charset=UTF-8",
                "Server", "autentiprinter"
        ));
    }

    private void pushBytes (byte[] data) {
        byteArrays.add(data);
        bufferSize += data.length;
    }

    private void writeHeaders () {
        pushBytes(String.format("HTTP/1.0 %d OK\n", status).getBytes());

        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        for (Map.Entry header: this.headers.entrySet()) {
            pushBytes((header.getKey() + ": " + header.getValue() + "\n").getBytes());
        }

        pushBytes(("Date: " + sdf.format(currentTime) + "\n\n").getBytes());
    }
    public ByteBuffer write () {
        ByteBuffer output = ByteBuffer.allocate(bufferSize);

        for (byte[] array: byteArrays) {
            output.put(array);
        }

        output.flip();
        return output;
    }

    public void text (String data) {
        writeHeaders();
        pushBytes(data.getBytes());
        ready = true;
    }
    public void status (int status) {
        this.status = status;
        writeHeaders();
        ready = true;
    }
    public void file (String path, String mime) {
        try {
            _file(path, mime);
            ready = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void _file (String path, String mime) throws IOException {
        headers.put("Content-Type", mime);
        writeHeaders();

        File file = new File(path);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        pushBytes(fileContent);
    }
}
