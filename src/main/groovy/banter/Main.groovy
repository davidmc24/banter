package banter

import org.slf4j.bridge.SLF4JBridgeHandler
import ratpack.groovy.launch.GroovyRatpackMain

class Main extends GroovyRatpackMain {

    static void main(String... args) {
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
        new Main().start()
    }

}
