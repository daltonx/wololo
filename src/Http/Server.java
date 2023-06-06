/* Simple HTTP server, heavily inspired by https://github.com/ebarlas/microhttp */
package Http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
// broken pipe will crash the server
public class Server {
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    private Router router;
    public Server (boolean authorize, String secretKey) throws IOException {
        String port = Objects.requireNonNullElse(System.getenv("PORT"), "7777");
        router = new Router(authorize, secretKey);
        selector = Selector.open();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", Integer.parseInt(port)));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        Thread thread = new Thread(this::run);
        thread.start();
    }

    private void run () {
        try {
            _run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void _run ()  throws IOException {
        while (true) {
            selector.select(); // blocks until a channel is ready
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // using if-else to ensure only a state is treated each cycle
                if (key.isAcceptable()) {
                    SocketChannel clientSocket = serverSocketChannel.accept();
                    clientSocket.configureBlocking(false);
                    SelectionKey _key = clientSocket.register(selector, SelectionKey.OP_READ);
                    new ClientHandler(clientSocket, _key, router);
                } else if (key.isReadable()) {
                    ClientHandler clientHandler = (ClientHandler) key.attachment();
                    clientHandler.onReadable();
                } else if (key.isWritable()) {
                    ClientHandler clientHandler = (ClientHandler) key.attachment();
                    clientHandler.onWritable();
                }
                iterator.remove();
            }
        }
    }
 }
