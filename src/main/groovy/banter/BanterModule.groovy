package banter

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.name.Names
import groovy.util.logging.Slf4j
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.Node

import javax.inject.Named
import javax.inject.Singleton

import static org.elasticsearch.node.NodeBuilder.nodeBuilder

@Slf4j
class BanterModule extends AbstractModule {

    private static final String CONFIG_FILE_NAME = "banter.groovy"

    private static File locateConfigDirectory(String fileName) {
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

    @Override
    protected void configure() {
        def configDir = locateConfigDirectory(CONFIG_FILE_NAME)
        log.info("Using config from {}", configDir)
        bind(File).annotatedWith(Names.named("elasticsearch.yml")).toInstance(new File(configDir, "elasticsearch.yml"))
        def appConfig = new ConfigSlurper().parse(new File(configDir, CONFIG_FILE_NAME).toURI().toURL())
        Names.bindProperties(binder(), appConfig.toProperties())
        bind(Indexer).asEagerSingleton()
        bind(Searcher).asEagerSingleton()
        bind(BanterBot).asEagerSingleton()
    }

    @Provides
    @Singleton
    Node node(@Named("elasticsearch.yml") File esConfigFile) {
        def settings = ImmutableSettings.builder().loadFromUrl(esConfigFile.toURI().toURL())
        return nodeBuilder().settings(settings).local(true).node()
    }

    @Provides
    @Singleton
    Client client(Node node) {
        return node.client()
    }

}
