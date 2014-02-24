package ratpack.shiro;

import com.google.inject.Inject;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.handling.internal.HandlerException;
import ratpack.http.Request;
import ratpack.http.Response;

import java.io.IOException;

public abstract class AdviceHandler implements Handler {

    //TODO: remove references to servlets and filters

    /**
     * The static logger available to this class only
     */
    private static final Logger log = LoggerFactory.getLogger(AdviceHandler.class);

    @Inject
    protected SecurityManager securityManager;

    /**
     * Returns {@code true} if the filter chain should be allowed to continue, {@code false} otherwise.
     * It is called before the chain is actually consulted/executed.
     * <p/>
     * The default implementation returns {@code true} always and exists as a template method for subclasses.
     *
     * @param request  the incoming ServletRequest
     * @param response the outgoing ServletResponse
     * @return {@code true} if the filter chain should be allowed to continue, {@code false} otherwise.
     * @throws Exception if there is any error.
     */
    protected boolean preHandle(Request request, Response response) throws Exception {
        return true;
    }

    /**
     * Allows 'post' advice logic to be called, but only if no exception occurs during filter chain execution.  That
     * is, if {@link #executeChain executeChain} throws an exception, this method will never be called.  Be aware of
     * this when implementing logic.  Most resource 'cleanup' behavior is often done in the
     * {@link #afterCompletion(Context, Exception)}
     * implementation, which is guaranteed to be called for every request, even when the chain processing throws
     * an Exception.
     * <p/>
     * The default implementation does nothing (no-op) and exists as a template method for subclasses.
     *
     * @param request  the incoming ServletRequest
     * @param response the outgoing ServletResponse
     * @throws Exception if an error occurs.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void postHandle(Request request, Response response) throws Exception {
    }

    /**
     * Called in all cases in a {@code finally} block even if {@link #preHandle preHandle} returns
     * {@code false} or if an exception is thrown during filter chain processing.  Can be used for resource
     * cleanup if so desired.
     * <p/>
     * The default implementation does nothing (no-op) and exists as a template method for subclasses.
     *
     * @param context The context to handle
     * @param exception any exception thrown during {@link #preHandle preHandle}, {@link #executeChain executeChain},
     *                  or {@link #postHandle postHandle} execution, or {@code null} if no exception was thrown
     *                  (i.e. the chain processed successfully).
     * @throws Exception if an error occurs.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void afterCompletion(Context context, Exception exception) throws Exception {
    }

    /**
     * Actually executes the specified filter chain by calling <code>chain.doFilter(request,response);</code>.
     * <p/>
     * Can be overridden by subclasses for custom logic.
     *
     * @param context The context to handle
     * @throws Exception if there is any error executing the chain.
     */
    protected void executeChain(Context context) throws Exception {
        context.next();
    }

    /**
     * Actually implements the chain execution logic, utilizing
     * {@link #preHandle(Request, Response) pre},
     * {@link #postHandle(Request, Response) post}, and
     * {@link #afterCompletion(Context, Exception) after}
     * advice hooks.
     *
     * @param context The context to handle
     */
    @Override
    public final void handle(Context context) throws Exception {
        ThreadContext.bind(securityManager);
        Request request = context.getRequest();
        Response response = context.getResponse();
        Exception exception = null;

        try {

            boolean continueChain = preHandle(request, response);
            if (log.isTraceEnabled()) {
                log.trace("Invoked preHandle method.  Continuing chain?: [" + continueChain + "]");
            }

            if (continueChain) {
                executeChain(context);
            }

            postHandle(request, response);
            if (log.isTraceEnabled()) {
                log.trace("Successfully invoked postHandle method");
            }

        } catch (Exception e) {
            exception = e;
        } finally {
            cleanup(context, exception);
        }
    }

    /**
     * Executes cleanup logic.
     * <p/>
     * This implementation specifically calls
     * {@link #afterCompletion(ratpack.handling.Context, Exception) afterCompletion}
     * as well as handles any exceptions properly.
     *
     * @param context The context to handle
     * @param existing any exception that might have occurred while executing the {@code FilterChain} or
     *                 pre or post advice, or {@code null} if the pre/chain/post execution did not throw an {@code Exception}.
     * @throws ratpack.handling.internal.HandlerException if any exception other than an {@code IOException} is thrown.
     * @throws java.io.IOException      if the pre/chain/post execution throw an {@code IOException}
     */
    protected void cleanup(Context context, Exception existing) throws HandlerException, IOException {
        Exception exception = existing;
        try {
            afterCompletion(context, exception);
            if (log.isTraceEnabled()) {
                log.trace("Successfully invoked afterCompletion method.");
            }
        } catch (Exception e) {
            if (exception == null) {
                exception = e;
            } else {
                log.debug("afterCompletion implementation threw an exception.  This will be ignored to " +
                        "allow the original source exception to be propagated.", e);
            }
        }
        if (exception != null) {
            if (exception instanceof HandlerException) {
                throw (HandlerException) exception;
            } else if (exception instanceof IOException) {
                throw (IOException) exception;
            } else {
                if (log.isDebugEnabled()) {
                    String msg = "Filter execution resulted in an unexpected Exception " +
                            "(not IOException or ServletException as the Filter API recommends).  " +
                            "Wrapping in ServletException and propagating.";
                    log.debug(msg);
                }
                throw new HandlerException(context, exception);
            }
        }
    }
}
