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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js");
    private final int port;
    private final ConcurrentHashMap <String, Handler> handlers;

    public Server(int port) {
        this.port = port;
        this.handlers = new ConcurrentHashMap<>();
    }

    public void addHandler(String requestType, String path, Handler handler) {
        handlers.put(requestType + " " + path, handler);
    }

    public void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(64);

        System.out.println("Server start.");

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
        } finally {
        threadPool.shutdown();
        }
    }

    private void handleConnection(Socket socket) {
        int headerIndex = 3;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            String requestLine = in.readLine();
            String[] parts = requestLine.split(" ");

            if (parts.length < 3) {
                return;
            }

            int requestTypeIndex = 0;
            String requestType = parts[requestTypeIndex];

            int pathIndex = 1;
            String path = parts[pathIndex];

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

            //заголовки
            ConcurrentHashMap<String, String> headers = new ConcurrentHashMap<>();
            if (parts.length > 3) {
                do {
                    String headerLine = parts[headerIndex];
                    if (headerLine.isEmpty()) {
                        break;
                    }
                    String[] headerParts = headerLine.split(": ");
                    if (headerParts.length == 2) {
                        headers.put(headerParts[0], headerParts[1]);
                    } else {
                        break;
                    }
                    headerIndex++;
                } while (headerIndex < parts.length);
            }

            //тело запроса
            StringBuilder requestBodyBuilder = new StringBuilder();

            int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
            if (contentLength > 0) {
                char[] buffer = new char[1024];
                int bytesRead;
                int totalBytesRead = 0;
                while (totalBytesRead < contentLength && (bytesRead = in.read(buffer)) != -1) {
                    requestBodyBuilder.append(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
            }

            String requestBody = requestBodyBuilder.toString();

            Request request = new Request(requestType, path, headers, requestBody, mimeType);

            BufferedOutputStream responseStream = new BufferedOutputStream(socket.getOutputStream());

            responseStream.write(responseBuilder().getBytes());
            responseStream.flush();

            Handler handler = handlers.get(requestType + " " + path);
            handler.handle(request, responseStream);


        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String responseBuilder () {

        String responseBody = "<h1>Hello!</h1>";
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + responseBody.length() + "\r\n" +
                "\r\n" +
                responseBody;
    }

}