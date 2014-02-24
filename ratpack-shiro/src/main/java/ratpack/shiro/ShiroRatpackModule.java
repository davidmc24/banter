package ratpack.shiro;

import com.google.inject.Injector;
import com.google.inject.Scopes;
import org.apache.shiro.guice.ShiroModule;
import ratpack.guice.HandlerDecoratingModule;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;

public abstract class ShiroRatpackModule extends ShiroModule implements HandlerDecoratingModule {
    @Override
    protected void configureShiro() {
        bind(GuiceShiroHandler.class).in(Scopes.SINGLETON);
        expose(GuiceShiroHandler.class);
        // TODO: provide a session manager that delegates to ratpack-session?
        configureShiroRatpack();
        // TODO: configure handler?
    }

    protected abstract void configureShiroRatpack();

    @Override
    public Handler decorate(Injector injector, Handler handler) {
        return Handlers.chain(injector.getInstance(GuiceShiroHandler.class), handler);
    }
}
