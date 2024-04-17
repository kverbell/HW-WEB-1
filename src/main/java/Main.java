import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {

        int port = 9999;
        final var server = new Server(port);

        Handler handler = new Handler() {

            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    File file = new File(request.getPath());
                    Path filePath = file.toPath();
                    byte[] contentBytes = Files.readAllBytes(filePath);

                    String headers = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + request.getMimeType() + "\r\n" +
                            "Content-Length: " + contentBytes.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n";
                    responseStream.write(headers.getBytes(StandardCharsets.UTF_8));
                    responseStream.write(contentBytes);
                    responseStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        server.addHandler("GET", "/messages", handler);
        server.addHandler("POST", "/messages", handler);

        server.start();
    }
}

