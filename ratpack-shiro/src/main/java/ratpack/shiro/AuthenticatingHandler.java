package ratpack.shiro;

import io.netty.handler.codec.http.HttpHeaders;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.subject.Subject;
import ratpack.handling.Context;
import ratpack.handling.internal.HandlerException;
import ratpack.http.Request;
import ratpack.http.Response;

import java.io.IOException;
import java.util.Arrays;

public abstract class AuthenticatingHandler extends AuthenticationHandler {
    //TODO: remove references to servlet
    public static final String PERMISSIVE = "permissive";

    //TODO - complete JavaDoc

    protected boolean executeLogin(Request request, Response response) throws Exception {
        AuthenticationToken token = createToken(request, response);
        if (token == null) {
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken " +
                    "must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        }
        try {
            Subject subject = getSubject(request, response);
            subject.login(token);
            return onLoginSuccess(token, subject, request, response);
        } catch (AuthenticationException e) {
            return onLoginFailure(token, e, request, response);
        }
    }

    protected abstract AuthenticationToken createToken(Request request, Response response) throws Exception;

    protected AuthenticationToken createToken(String username, String password,
                                              Request request, Response response) {
        boolean rememberMe = isRememberMe(request);
        String host = getHost(request);
        return createToken(username, password, rememberMe, host);
    }

    protected AuthenticationToken createToken(String username, String password,
                                              boolean rememberMe, String host) {
        return new UsernamePasswordToken(username, password, rememberMe, host);
    }

    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject,
                                     Request request, Response response) throws Exception {
        return true;
    }

    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e,
                                     Request request, Response response) {
        return false;
    }

    /**
     * Returns the host name or IP associated with the current subject.  This method is primarily provided for use
     * during construction of an <code>AuthenticationToken</code>.
     *
     * @param request the incoming ServletRequest
     * @return the <code>InetAddress</code> to associate with the login attempt.
     */
    protected String getHost(Request request) {
        return request.getHeaders().get(HttpHeaders.Names.HOST);
    }

    /**
     * Returns <code>true</code> if &quot;rememberMe&quot; should be enabled for the login attempt associated with the
     * current <code>request</code>, <code>false</code> otherwise.
     * <p/>
     * This implementation always returns <code>false</code> and is provided as a template hook to subclasses that
     * support <code>rememberMe</code> logins and wish to determine <code>rememberMe</code> in a custom mannner
     * based on the current <code>request</code>.
     *
     * @param request the incoming ServletRequest
     * @return <code>true</code> if &quot;rememberMe&quot; should be enabled for the login attempt associated with the
     *         current <code>request</code>, <code>false</code> otherwise.
     */
    protected boolean isRememberMe(Request request) {
        return false;
    }

    /**
     * Determines whether the current subject should be allowed to make the current request.
     * <p/>
     * The default implementation returns <code>true</code> if the user is authenticated.  Will also return
     * <code>true</code> if the {@link #isLoginRequest} returns false and the &quot;permissive&quot; flag is set.
     *
     * @return <code>true</code> if request should be allowed access
     */
    @Override
    protected boolean isAccessAllowed(Request request, Response response, Object mappedValue) {
        return super.isAccessAllowed(request, response, mappedValue) ||
                (!isLoginRequest(request, response) && isPermissive(mappedValue));
    }

    /**
     * Returns <code>true</code> if the mappedValue contains the {@link #PERMISSIVE} qualifier.
     *
     * @return <code>true</code> if this filter should be permissive
     */
    protected boolean isPermissive(Object mappedValue) {
        if(mappedValue != null) {
            String[] values = (String[]) mappedValue;
            return Arrays.binarySearch(values, PERMISSIVE) >= 0;
        }
        return false;
    }

    /**
     * Overrides the default behavior to call {@link #onAccessDenied} and swallow the exception if the exception is
     * {@link UnauthenticatedException}.
     */
    @Override
    protected void cleanup(Context context, Exception existing) throws HandlerException, IOException {
        if (existing instanceof UnauthenticatedException || (existing instanceof HandlerException && existing.getCause() instanceof UnauthenticatedException))
        {
            try {
                onAccessDenied(context.getRequest(), context.getResponse());
                existing = null;
            } catch (Exception e) {
                existing = e;
            }
        }
        super.cleanup(context, existing);

    }
}
