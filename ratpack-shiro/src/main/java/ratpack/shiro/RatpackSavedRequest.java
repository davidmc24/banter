package ratpack.shiro;

import ratpack.http.Request;

import java.io.Serializable;

public class RatpackSavedRequest implements Serializable {
    private String method;
    private String query;
    private String uri;

    /**
     * Constructs a new instance from the given HTTP request.
     *
     * @param request the current request to save.
     */
    public RatpackSavedRequest(Request request) {
        this.method = request.getMethod().getName();
        this.query = request.getQuery();
        this.uri = request.getUri();
    }

    public String getMethod() {
        return method;
    }

    public String getQuery() {
        return query;
    }

    public String getUri() {
        return uri;
    }

    public String getRequestUrl() {
        StringBuilder requestUrl = new StringBuilder(getUri());
        if (getQuery() != null) {
            requestUrl.append("?").append(getQuery());
        }
        return requestUrl.toString();
    }
}
