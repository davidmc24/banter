package ratpack.shiro;

import org.apache.shiro.util.AntPathMatcher;
import org.apache.shiro.util.PatternMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.handling.internal.HandlerException;
import ratpack.http.Request;
import ratpack.http.Response;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.shiro.util.StringUtils.split;

public abstract class PathMatchingHandler extends AdviceHandler {
    // TODO: remove references to serlvet
    /**
     * Log available to this class only
     */
    private static final Logger log = LoggerFactory.getLogger(PathMatchingHandler.class);

    /**
     * PatternMatcher used in determining which paths to react to for a given request.
     */
    protected PatternMatcher pathMatcher = new AntPathMatcher();

    /**
     * A collection of path-to-config entries where the key is a path which this filter should process and
     * the value is the (possibly null) configuration element specific to this Filter for that specific path.
     * <p/>
     * <p>To put it another way, the keys are the paths (urls) that this Filter will process.
     * <p>The values are filter-specific data that this Filter should use when processing the corresponding
     * key (path).  The values can be null if no Filter-specific config was specified for that url.
     */
    protected Map<String, Object> appliedPaths = new LinkedHashMap<String, Object>();

    /**
     * Splits any comma-delmited values that might be found in the <code>config</code> argument and sets the resulting
     * <code>String[]</code> array on the <code>appliedPaths</code> internal Map.
     * <p/>
     * That is:
     * <pre><code>
     * String[] values = null;
     * if (config != null) {
     *     values = split(config);
     * }
     * <p/>
     * this.{@link #appliedPaths appliedPaths}.put(path, values);
     * </code></pre>
     *
     * @param path   the application context path to match for executing this filter.
     * @param config the specified for <em>this particular filter only</em> for the given <code>path</code>
     * @return this configured filter.
     */
    public Handler processPathConfig(String path, String config) {
        String[] values = null;
        if (config != null) {
            values = split(config);
        }

        this.appliedPaths.put(path, values);
        return this;
    }

    /**
     * Returns the context path within the application based on the specified <code>request</code>.
     *
     * @param request the incoming <code>Request</code>
     * @return the context path within the application.
     */
    protected String getPathWithinApplication(Request request) {
        return "/" + request.getPath();
    }

    /**
     * Returns <code>true</code> if the incoming <code>request</code> matches the specified <code>path</code> pattern,
     * <code>false</code> otherwise.
     *
     * @param path    the configured url pattern to check the incoming request against.
     * @param request the incoming ServletRequest
     * @return <code>true</code> if the incoming <code>request</code> matches the specified <code>path</code> pattern,
     *         <code>false</code> otherwise.
     */
    protected boolean pathsMatch(String path, Request request) {
        String requestURI = getPathWithinApplication(request);
        log.trace("Attempting to match pattern '{}' with current requestURI '{}'...", path, requestURI);
        return pathsMatch(path, requestURI);
    }

    /**
     * Returns <code>true</code> if the <code>path</code> matches the specified <code>pattern</code> string,
     * <code>false</code> otherwise.
     * <p/>
     * Simply delegates to
     * <b><code>this.pathMatcher.{@link PatternMatcher#matches(String, String) matches(pattern,path)}</code></b>,
     * but can be overridden by subclasses for custom matching behavior.
     *
     * @param pattern the pattern to match against
     * @param path    the value to match with the specified <code>pattern</code>
     * @return <code>true</code> if the <code>path</code> matches the specified <code>pattern</code> string,
     *         <code>false</code> otherwise.
     */
    protected boolean pathsMatch(String pattern, String path) {
        return pathMatcher.matches(pattern, path);
    }

    /**
     * Implementation that handles path-matching behavior before a request is evaluated.  If the path matches,
     * the request will be allowed through via the result from
     * {@link #onPreHandle(Request, Response, Object) onPreHandle}.  If the
     * path does not match or the filter is not enabled for that path, this filter will allow passthrough immediately
     * to allow the {@code FilterChain} to continue executing.
     * <p/>
     * In order to retain path-matching functionality, subclasses should not override this method if at all
     * possible, and instead override
     * {@link #onPreHandle(Request, Response, Object) onPreHandle} instead.
     *
     * @param request  the incoming ServletRequest
     * @param response the outgoing ServletResponse
     * @return {@code true} if the filter chain is allowed to continue to execute, {@code false} if a subclass has
     *         handled the request explicitly.
     * @throws Exception if an error occurs
     */
    protected boolean preHandle(Request request, Response response) throws Exception {

        if (this.appliedPaths == null || this.appliedPaths.isEmpty()) {
            if (log.isTraceEnabled()) {
                log.trace("appliedPaths property is null or empty.  This Filter will passthrough immediately.");
            }
            return true;
        }

        for (String path : this.appliedPaths.keySet()) {
            // If the path does match, then pass on to the subclass implementation for specific checks
            //(first match 'wins'):
            if (pathsMatch(path, request)) {
                log.trace("Current requestURI matches pattern '{}'.  Determining filter chain execution...", path);
                Object config = this.appliedPaths.get(path);
                if (log.isTraceEnabled()) {
                    log.trace("Filter is enabled for the current request under path '{}' with config [{}].  " +
                            "Delegating to subclass implementation for 'onPreHandle' check.",
                            path, config);
                }
                //The filter is enabled for this specific request, so delegate to subclass implementations
                //so they can decide if the request should continue through the chain or not:
                return onPreHandle(request, response, config);
            }
        }

        //no path matched, allow the request to go through:
        return true;
    }

    /**
     * This default implementation always returns {@code true} and should be overridden by subclasses for custom
     * logic if necessary.
     *
     * @param request     the incoming ServletRequest
     * @param response    the outgoing ServletResponse
     * @param mappedValue the filter-specific config value mapped to this filter in the URL rules mappings.
     * @return {@code true} if the request should be able to continue, {@code false} if the filter will
     *         handle the response directly.
     * @throws Exception if an error occurs
     */
    protected boolean onPreHandle(Request request, Response response, Object mappedValue) throws Exception {
        return true;
    }

}
