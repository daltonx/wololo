package Http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientHandler {
    private SocketChannel socket;
    private SelectionKey key;
    private Request request = new Request();
    private Response response = new Response();
    private Controller controller;
    private final int chunkSize = 64 * 1024;
    private final int maxRequestSize = 1024 * 1024;
    // this buffer could be shared between keys on a channel, as it holds ephemeral data
    // it would reduce memory usage and the allocation overhead
    private ByteBuffer readBuffer = ByteBuffer.allocate(chunkSize);
    private boolean finishedReading = false;
    ClientHandler (SocketChannel clientSocket, SelectionKey selectionKey, Controller controller) {
        socket = clientSocket;
        key = selectionKey;
        key.attach(this);
        this.controller = controller;
    }

    void onReadable () {
        try {
            readRequest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void readRequest () throws IOException {
        readBuffer.clear();
        int read = socket.read(readBuffer);

        if (read < 0) {
            close();
            return;
        }

        readBuffer.flip();
        if (request.pushBuffer(readBuffer)) {
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }
    void onWritable () {
        try {
            if (!request.ready) {
                controller.handle(request, response);
            }
            if (response.ready) {
                writeResponse();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void writeResponse () throws IOException {
        ByteBuffer buffer = response.write();
        while (buffer.remaining() > 0) {
            socket.write(buffer);
        }
        close();
    }

    void close () {
        try {
            key.cancel();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
