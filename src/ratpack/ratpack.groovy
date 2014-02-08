import banter.BanterModule
import ratpack.jackson.JacksonModule
import ratpack.jackson.Jackson
import ratpack.groovy.Groovy

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
        post("join/:room") {
            // TODO: implement real joining
            render Jackson.json(room: pathTokens["room"])
        }
        
        assets "public"
    }
}
