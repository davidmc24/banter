package ratpack.openid.provider.google;

import org.openid4java.message.MessageException;
import org.openid4java.message.ax.FetchRequest;
import ratpack.openid.Attribute;

public enum GoogleAttribute implements Attribute {

    country("country", "http://axschema.org/contact/country/home"),
    email("email", "http://axschema.org/contact/email"),
    firstname("firstname", "http://axschema.org/namePerson/first"),
    language("language", "http://axschema.org/pref/language"),
    lastname("lastname", "http://axschema.org/namePerson/last");

    private final String alias;
    private final String typeUri;

    GoogleAttribute(String alias, String typeUri) {
        this.alias = alias;
        this.typeUri = typeUri;
    }

    @Override
    public void register(FetchRequest fetchRequest, boolean required) throws MessageException {
        fetchRequest.addAttribute(alias, typeUri, required, 1);
    }

}
