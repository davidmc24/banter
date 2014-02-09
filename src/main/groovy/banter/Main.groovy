package banter

import org.slf4j.bridge.SLF4JBridgeHandler
import ratpack.groovy.launch.GroovyRatpackMain

class Main extends GroovyRatpackMain {

    static final String CONFIG_FILE_NAME = "banter.groovy"

    static File locateConfigDirectory(String fileName) {
        def paths = [
                "/etc/banter",
                "${System.getProperty("user.home")}/.banter",
                "config",
                "../../config"
        ]
        def files = paths.collect {new File(it).canonicalFile}
        def file = files.find {new File(it, fileName).file}
        if (!file) {
            throw new FileNotFoundException("Could not find ${fileName} in any of these directories: ${files}")
        }
        return file
    }

    static void main(String... args) {
        System.setProperty("app.config.dir", locateConfigDirectory(CONFIG_FILE_NAME).absolutePath)
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
        new Main().start()
    }

}
