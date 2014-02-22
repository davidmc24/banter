package ratpack.openid;

import org.openid4java.message.MessageException;
import org.openid4java.message.ax.FetchRequest;

public interface Attribute {
    void register(FetchRequest fetchRequest, boolean required) throws MessageException;
}
