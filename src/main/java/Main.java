import java.io.BufferedOutputStream;

public class Main {
    public static void main(String[] args) {

        int port = 9999;
        final var server = new Server(port);

        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
            }
        });

        server.start();
    }
}

