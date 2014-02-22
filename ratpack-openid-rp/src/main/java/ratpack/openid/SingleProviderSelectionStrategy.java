package ratpack.openid;

import ratpack.handling.Context;
import ratpack.handling.Handler;

public class SingleProviderSelectionStrategy implements ProviderSelectionStrategy {
    private final String providerEndpointUrl;

    public SingleProviderSelectionStrategy(String providerEndpointUrl) {
        this.providerEndpointUrl = providerEndpointUrl;
    }

    @Override
    public Handler getHandler() {
        return null;
    }

    @Override
    public boolean handleProviderSelection(Context context) {
        return true;
    }

    @Override
    public String getProviderDiscoveryUrl() {
        return providerEndpointUrl;
    }
}
