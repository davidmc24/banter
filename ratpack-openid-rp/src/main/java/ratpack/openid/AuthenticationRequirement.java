package ratpack.openid;

import ratpack.handling.Context;

import java.util.regex.Pattern;

public class AuthenticationRequirement {

    private final Pattern pattern;

    private AuthenticationRequirement(Pattern pattern) {
        this.pattern = pattern;
    }

    boolean matches(Context context) {
        String uri = context.getRequest().getUri();
        System.out.println("uri: " + uri);
        System.out.println("pattern: " + pattern.toString());
        return pattern.matcher(uri).matches();
    }

    public static AuthenticationRequirement of(String path) {
        return new AuthenticationRequirement(Pattern.compile(Pattern.quote(path)));
    }

    public static AuthenticationRequirement of(Pattern pattern) {
        return new AuthenticationRequirement(pattern);
    }

}
