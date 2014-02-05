import ratpack.jackson.JacksonModule
import static ratpack.jackson.Jackson.json
import static ratpack.groovy.Groovy.ratpack
import static ratpack.groovy.Groovy.groovyTemplate

ratpack {
    modules {
        register new JacksonModule()
    }
    handlers {
        get {
            render groovyTemplate("index.html", title: "My Ratpack App")
        }
        get("some-json") {
            render json(user: 1)
        }
        
        assets "public"
    }
}
