import java.util.concurrent.ConcurrentHashMap;

public class Request {
    private final String requestType;
    private final String path;
    private final ConcurrentHashMap<String, String> headers;
    private final String requestBody;
    private final String mimeType;

    public Request(String requestType, String path, ConcurrentHashMap<String, String> headers,
                   String requestBody, String mimeType) {
        this.requestType = requestType;
        this.path = path;
        this.headers = headers;
        this.requestBody = requestBody;
        this.mimeType = mimeType;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getPath() {
        return path;
    }

    public ConcurrentHashMap<String, String> getHeaders() {
        return headers;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getMimeType() {
        return mimeType;
    }

}
