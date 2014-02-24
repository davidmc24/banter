package ratpack.openid.provider.yahoo;

import org.openid4java.message.MessageException;
import org.openid4java.message.ax.FetchRequest;
import ratpack.openid.Attribute;

public enum YahooAttribute implements Attribute {
    email("email", "http://axschema.org/contact/email"),
    fullname("fullname", "http://axschema.org/namePerson");

    private final String alias;
    private final String typeUri;

    YahooAttribute(String alias, String typeUri) {
        this.alias = alias;
        this.typeUri = typeUri;
    }

    @Override
    public void register(FetchRequest fetchRequest, boolean required) throws MessageException {
        fetchRequest.addAttribute(alias, typeUri, required, 1);
    }
}
