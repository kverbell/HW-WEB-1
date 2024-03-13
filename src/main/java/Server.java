import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js");
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        int port = 9999;
        Server server = new Server(port);
        server.start();
    }

    public void start() {

        ExecutorService threadPool = Executors.newFixedThreadPool(64);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();

                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        handleConnection(socket);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            String requestLine = in.readLine();
            String[] parts = requestLine.split(" ");

            if (parts.length != 3) {
                return;
            }

            String path = parts[1];

            if (!validPaths.contains(path)) {
                out.write(("HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n").getBytes());
                out.flush();
                return;
            }

            Path filePath = Path.of(".", "public", path);
            String mimeType = Files.probeContentType(filePath);

            if (path.equals("/classic.html")) {
                String template = Files.readString(filePath);
                String content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                );
                byte[] contentBytes = content.getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + contentBytes.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(contentBytes);
                out.flush();
                return;
            }

            long length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}


