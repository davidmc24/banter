package ratpack.openid;

import ratpack.handling.Context;
import ratpack.handling.Handler;

public interface ProviderSelectionStrategy {
    /**
     * Returns the handler to use for provider selection, or {@code null} if no handler is needed.
     */
    Handler getHandler();

    /**
     * Executes whatever logic is needed to determine which provider to use.  This may include redirecting the user to
     * a selection screen, in which case this method should return {@code false}.
     *
     * @param context the context to handle
     * @return whether a provider has been fully selected and request handling should continue
     */
    boolean handleProviderSelection(Context context);

    /**
     * Returns the OpenID Provider Discovery URL to use for authentication.
     */
    String getProviderDiscoveryUrl();
}
