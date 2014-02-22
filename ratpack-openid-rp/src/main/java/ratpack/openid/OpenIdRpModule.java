package ratpack.openid;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import org.openid4java.consumer.ConsumerManager;
import ratpack.guice.HandlerDecoratingModule;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;

import java.util.LinkedList;
import java.util.List;

public class OpenIdRpModule extends AbstractModule implements HandlerDecoratingModule {
    private static final String DEFAULT_VERIFICATION_PATH = "openid/verification";

    private final ProviderSelectionStrategy providerSelectionStrategy;
    private final String verificationPath;

    public OpenIdRpModule() {
        this((ProviderSelectionStrategy) null);
    }

    public OpenIdRpModule(String providerUrl) {
        this(new SingleProviderSelectionStrategy(providerUrl));
    }

    public OpenIdRpModule(ProviderSelectionStrategy providerSelectionStrategy) {
        this(providerSelectionStrategy, DEFAULT_VERIFICATION_PATH);
    }

    OpenIdRpModule(ProviderSelectionStrategy providerSelectionStrategy, String verificationPath) {
        this.providerSelectionStrategy = providerSelectionStrategy;
        this.verificationPath = verificationPath;
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(VerificationPath.class).to(verificationPath);
        Multibinder.newSetBinder(binder(), AuthenticationRequirement.class);
        Multibinder.newSetBinder(binder(), Attribute.class, Required.class);
        Multibinder.newSetBinder(binder(), Attribute.class, Optional.class);
        bind(ConsumerManager.class).in(Scopes.SINGLETON);
        bind(AuthHandler.class);
        bind(CallbackHandler.class);
        if (providerSelectionStrategy != null) {
            bind(ProviderSelectionStrategy.class).toInstance(providerSelectionStrategy);
        }
    }

    @Override
    public Handler decorate(Injector injector, Handler handler) {
        List<Handler> handlers = new LinkedList<>();
        ProviderSelectionStrategy providerSelectionStrategy = injector.getInstance(ProviderSelectionStrategy.class);
        Handler providerSelectionHandler = providerSelectionStrategy.getHandler();
        if (providerSelectionHandler != null) {
            handlers.add(providerSelectionHandler);
        }
        handlers.add(injector.getInstance(CallbackHandler.class));
        handlers.add(injector.getInstance(AuthHandler.class));
        handlers.add(handler);
        return Handlers.chain(handlers.toArray(new Handler[handlers.size()]));
    }
}
