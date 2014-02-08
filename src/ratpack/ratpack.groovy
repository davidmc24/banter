import banter.BanterBot
import banter.BanterModule
import org.slf4j.LoggerFactory
import ratpack.jackson.JacksonModule
import ratpack.jackson.Jackson
import ratpack.groovy.Groovy

import static io.netty.handler.codec.http.HttpResponseStatus.*

def log = LoggerFactory.getLogger("banter.ratpack.handler")

Groovy.ratpack {
    modules {
        register new JacksonModule()
        register new BanterModule()
    }
    handlers {
        get {
            render Groovy.groovyTemplate("index.html", title: "My Ratpack App")
        }
        get("some-json") {
            render Jackson.json(user: 1)
        }
        post("join/:room") { BanterBot banterBot ->
            def room = pathTokens["room"]
            log.info("Attempting to join room {}", room)
            banterBot.sendJoin(room)
            response.status(ACCEPTED)
            response.send()
        }
        
        assets "public"
    }
}
