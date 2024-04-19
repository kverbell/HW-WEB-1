import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class Request {
    private final String requestType;
    private final String path;
    private final Map<String, String> headers;
    private final String requestBody;
    private final String mimeType;
    private final Map<String, List<String>> queryParams;

    public Request(String requestType, String path, Map<String, String> headers,
                   String requestBody, String mimeType, String queryString) {
        this.requestType = requestType;
        this.path = path;
        this.headers = headers;
        this.requestBody = requestBody;
        this.mimeType = mimeType;
        if ("GET".equals(requestType)) {
            this.queryParams = parseQueryString(queryString);
        } else {
            this.queryParams = new HashMap<>();
        }
    }

    public String getRequestType() {
        return requestType;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        List<String> values = queryParams.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    private Map<String, List<String>> parseQueryString(String queryString) {
        List<NameValuePair> params = URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8);
        return params.stream()
                .collect(Collectors.groupingBy(
                        NameValuePair::getName,
                        HashMap::new,
                        Collectors.mapping(NameValuePair::getValue, Collectors.toList())
                ));
    }

}
