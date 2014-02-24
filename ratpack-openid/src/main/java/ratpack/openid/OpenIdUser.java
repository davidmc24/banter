package ratpack.openid;

import ratpack.util.MultiValueMap;
import ratpack.util.internal.ImmutableDelegatingMultiValueMap;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class OpenIdUser implements Serializable {
    private final String identifier;
    private final Map<String, List<String>> attributes;

    OpenIdUser(String identifier, Map<String, List<String>> attributes) {
        this.identifier = identifier;
        this.attributes = attributes;
    }

    public String getIdentifier() {
        return identifier;
    }

    public MultiValueMap<String, String> getAttributes() {
        return new ImmutableDelegatingMultiValueMap<>(attributes);
    }
}
