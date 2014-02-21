package ext

import io.netty.handler.codec.http.HttpResponseStatus
import ratpack.http.Response

class ResponseExtension {

    static Response status(Response self, HttpResponseStatus status) {
        return self.status(status.code(), status.reasonPhrase())
    }

}
