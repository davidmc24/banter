package ratpack.shiro;

import com.google.inject.Inject;
import ratpack.handling.Context;
import ratpack.handling.Handler;

public class GuiceShiroHandler implements Handler {
    @Inject
    BasicHttpAuthenticationHandler basicHttpAuthenticationHandler;

    @Override
    public void handle(Context context) throws Exception {
        basicHttpAuthenticationHandler.processPathConfig("/auth/page1", "");
        // TODO: implement
        if (context.getRequest().getPath().startsWith("auth")) {
            context.insert(basicHttpAuthenticationHandler);
        } else {
            context.next();
        }
    }
}
