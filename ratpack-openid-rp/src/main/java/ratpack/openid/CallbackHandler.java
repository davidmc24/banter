package ratpack.openid;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchResponse;
import ratpack.func.Action;
import ratpack.handling.Background;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.handling.Redirector;
import ratpack.server.PublicAddress;
import ratpack.session.store.SessionStorage;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static ratpack.openid.SessionConstants.*;

public class CallbackHandler implements Handler {

    @Inject
    ConsumerManager manager;

    @Inject
    @VerificationPath
    String verificationPath;

    @Override
    public void handle(final Context context) throws Exception {
        if (isVerificationRequest(context)) {
            Background background = context.getBackground();
            background.exec(new VerifyAuthenticationCallable(context)).then(new ProcessVerificationAction(context));
        } else {
            context.next();
        }
    }

    private boolean isVerificationRequest(Context context) {
        return verificationPath.equals(context.getRequest().getPath());
    }

    private String getReceivingUrl(Context context) {
        String baseUri = context.get(PublicAddress.class).getAddress(context).toString();
        return baseUri + context.getRequest().getUri();
    }

    private ParameterList getAuthParamList(Context context) {
        return new ParameterList(context.getRequest().getQueryParams());
    }

    private DiscoveryInformation getDiscoveryInfo(Context context) {
        return (DiscoveryInformation) context.get(SessionStorage.class).remove(DISCOVERY_INFO);
    }

    private boolean isVerified(VerificationResult verificationResult) {
        return verificationResult.getVerifiedId() != null;
    }

    private void saveUserInSession(Context context, VerificationResult verificationResult) throws MessageException {
        String identifier = verificationResult.getVerifiedId().getIdentifier();
        OpenIdUser user = new OpenIdUser(identifier, getAttributes(verificationResult));
        SessionStorage sessionStorage = context.get(SessionStorage.class);
        sessionStorage.put(USER, user);
    }

    private void redirectToSavedUri(Context context) {
        SessionStorage sessionStorage = context.get(SessionStorage.class);
        String originalUri = (String) sessionStorage.remove(SAVED_URI);
        if (originalUri == null) {
            originalUri = "/";
        }
        Redirector redirector = context.get(Redirector.class);
        redirector.redirect(context, originalUri, HttpResponseStatus.FOUND.code());
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> getAttributes(VerificationResult verificationResult) throws MessageException {
        Message authResponse = verificationResult.getAuthResponse();
        FetchResponse fetchResponse = (FetchResponse) authResponse.getExtension(AxMessage.OPENID_NS_AX);
        return (Map<String, List<String>>) fetchResponse.getAttributes();
    }

    class VerifyAuthenticationCallable implements Callable<VerificationResult> {
        private final Context context;

        VerifyAuthenticationCallable(Context context) {
            this.context = context;
        }

        @Override
        public VerificationResult call() throws Exception {
            String receivingUrl = getReceivingUrl(context);
            ParameterList authParamList = getAuthParamList(context);
            DiscoveryInformation discovered = getDiscoveryInfo(context);
            return manager.verify(receivingUrl, authParamList, discovered);
        }
    }

    class ProcessVerificationAction implements Action<VerificationResult> {
        private final Context context;

        ProcessVerificationAction(Context context) {
            this.context = context;
        }

        @Override
        public void execute(VerificationResult verificationResult) throws Exception {
            if (isVerified(verificationResult)) {
                saveUserInSession(context, verificationResult);
                redirectToSavedUri(context);
            } else {
                throw new ConsumerException("Failed to authenticate: " + verificationResult.getStatusMsg());
            }
        }
    }

}
