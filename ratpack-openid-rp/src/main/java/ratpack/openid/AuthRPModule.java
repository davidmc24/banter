package ratpack.openid;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.openid4java.consumer.ConsumerManager;
import ratpack.guice.HandlerDecoratingModule;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;

public class AuthRPModule extends AbstractModule implements HandlerDecoratingModule {

    private static final String DEFAULT_VERIFICATION_PATH = "openid/verification";

    private final String providerUrl;
    private final String verificationPath;

    public AuthRPModule(String providerUrl) {
        this(providerUrl, DEFAULT_VERIFICATION_PATH);
    }

    // TODO: support multiple providers somehow
    AuthRPModule(String providerUrl, String verificationPath) {
        this.providerUrl = providerUrl;
        this.verificationPath = verificationPath;
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(ProviderUrl.class).to(providerUrl);
        bindConstant().annotatedWith(VerificationPath.class).to(verificationPath);
        Multibinder.newSetBinder(binder(), AuthenticationRequirement.class);
        Multibinder.newSetBinder(binder(), Attribute.class, Required.class);
        Multibinder.newSetBinder(binder(), Attribute.class, Optional.class);
        bind(ConsumerManager.class).in(Scopes.SINGLETON);
        bind(AuthHandler.class);
        bind(CallbackHandler.class);
    }

    @Override
    public Handler decorate(Injector injector, Handler handler) {
        AuthHandler authHandler = injector.getInstance(AuthHandler.class);
        CallbackHandler callbackHandler = injector.getInstance(CallbackHandler.class);
        return Handlers.chain(callbackHandler, authHandler, handler);
    }

}
