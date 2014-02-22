package ratpack.openid;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.ax.FetchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.func.Action;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.handling.Redirector;
import ratpack.http.Request;
import ratpack.server.PublicAddress;
import ratpack.session.store.SessionStorage;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.Callable;

import static ratpack.openid.SessionConstants.*;

public class AuthHandler implements Handler {
    // TODO: remove?
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @VerificationPath
    String verificationPath;

    @Inject
    ProviderSelectionStrategy providerSelectionStrategy;

    @Inject
    Set<AuthenticationRequirement> authenticationRequirements;

    @Inject
    @Required
    Set<Attribute> requiredAttributes;

    @Inject
    @Optional
    Set<Attribute> optionalAttributes;

    @Inject
    ConsumerManager manager;

    @Override
    public void handle(final Context context) throws Exception {
        logger.info("in handle");
        if (requiresAuthentication(context)) {
            logger.info("requiring auth");
            if (isAuthenticated(context)) {
                logger.info("already authenticated");
                context.next();
            } else {
                logger.info("not authenticated");
                if (providerSelectionStrategy.handleProviderSelection(context)) {
                    context.getBackground()
                            .exec(new DiscoverProviderCallable())
                            .then(new ProcessDiscoveryInfoAction(context));
                } else {
                    logger.info("provider selection doing something");
                }
            }
        } else {
            logger.info("not requiring auth");
            context.next();
        }
    }

    private FetchRequest createFetchRequest() throws MessageException {
        FetchRequest fetchRequest = FetchRequest.createFetchRequest();
        for (Attribute attribute : requiredAttributes) {
            attribute.register(fetchRequest, true);
        }
        for (Attribute attribute : optionalAttributes) {
            attribute.register(fetchRequest, false);
        }
        return fetchRequest;
    }

    private boolean requiresAuthentication(Context context) {
        for (AuthenticationRequirement authenticationRequirement : authenticationRequirements) {
            if (authenticationRequirement.matches(context)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAuthenticated(Context context) {
        return context.get(SessionStorage.class).containsKey(USER);
    }

    private class DiscoverProviderCallable implements Callable<DiscoveryInformation> {
        @Override
        public DiscoveryInformation call() throws Exception {
            String discoveryUrl = providerSelectionStrategy.getProviderDiscoveryUrl();
            return manager.associate(manager.discover(discoveryUrl));
        }
    }

    private class ProcessDiscoveryInfoAction implements Action<DiscoveryInformation> {

        private final Context context;

        ProcessDiscoveryInfoAction(Context context) {
            this.context = context;
        }

        @Override
        public void execute(final DiscoveryInformation discoveryInfo) throws Exception {
            context.get(SessionStorage.class).put(DISCOVERY_INFO, discoveryInfo);
            context.getBackground()
                    .exec(new AuthenticateCallable(context, discoveryInfo))
                    .then(new ProcessAuthenticationAction(context));
        }
    }

    private class AuthenticateCallable implements Callable<AuthRequest> {
        private final Context context;
        private final DiscoveryInformation discoveryInfo;

        AuthenticateCallable(Context context, DiscoveryInformation discoveryInfo) {
            this.context = context;
            this.discoveryInfo = discoveryInfo;
        }

        @Override
        public AuthRequest call() throws Exception {
            String realm = context.get(PublicAddress.class).getAddress(context).toString();
            String returnToUrl = realm + "/" + verificationPath;
            logger.info("using returnToUrl {}, realm {}", returnToUrl, realm);
            return manager.authenticate(discoveryInfo, returnToUrl, realm);
        }
    }

    private class ProcessAuthenticationAction implements Action<AuthRequest> {
        private final Context context;

        ProcessAuthenticationAction(Context context) {
            this.context = context;
        }

        @Override
        public void execute(AuthRequest authReq) throws Exception {
            Redirector redirector = context.get(Redirector.class);
            Request request = context.getRequest();
            SessionStorage sessionStorage = context.get(SessionStorage.class);
            String requestedUri = request.getUri();
            logger.info("Saving URI {}", requestedUri);
            sessionStorage.put(SAVED_URI, requestedUri);
            authReq.addExtension(createFetchRequest());
            redirector.redirect(context, authReq.getDestinationUrl(true), HttpResponseStatus.FOUND.code());
        }
    }
}
