package ratpack.shiro;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.shiro.subject.Subject;
import ratpack.handling.Context;
import ratpack.handling.Redirector;
import ratpack.http.Request;
import ratpack.http.Response;

import javax.inject.Inject;

public abstract class AuthenticationHandler extends AccessControlHandler {
    // TODO: remove servlet references
    //TODO - complete JavaDoc

    public static final String DEFAULT_SUCCESS_URL = "/";

    private String successUrl = DEFAULT_SUCCESS_URL;

    /**
     * Returns the success url to use as the default location a user is sent after logging in.  Typically a redirect
     * after login will redirect to the originally request URL; this property is provided mainly as a fallback in case
     * the original request URL is not available or not specified.
     * <p/>
     * The default value is {@link #DEFAULT_SUCCESS_URL}.
     *
     * @return the success url to use as the default location a user is sent after logging in.
     */
    public String getSuccessUrl() {
        return successUrl;
    }

    /**
     * Sets the default/fallback success url to use as the default location a user is sent after logging in.  Typically
     * a redirect after login will redirect to the originally request URL; this property is provided mainly as a
     * fallback in case the original request URL is not available or not specified.
     * <p/>
     * The default value is {@link #DEFAULT_SUCCESS_URL}.
     *
     * @param successUrl the success URL to redirect the user to after a successful login.
     */
    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }


    /**
     * Determines whether the current subject is authenticated.
     * <p/>
     * The default implementation {@link #getSubject(Request, Response) acquires}
     * the currently executing Subject and then returns
     * {@link org.apache.shiro.subject.Subject#isAuthenticated() subject.isAuthenticated()};
     *
     * @return true if the subject is authenticated; false if the subject is unauthenticated
     */
    protected boolean isAccessAllowed(Request request, Response response, Object mappedValue) {
        Subject subject = getSubject(request, response);
        return subject.isAuthenticated();
    }

    /**
     * Redirects to user to the previously attempted URL after a successful login.
     *
     * @param context The context to handle
     * @throws Exception if there is a problem redirecting.
     */
    protected void issueSuccessRedirect(Context context) throws Exception {
        Redirector redirector = context.get(Redirector.class);
        redirector.redirect(context, getSuccessUrl(), HttpResponseStatus.FOUND.code());
    }

}
