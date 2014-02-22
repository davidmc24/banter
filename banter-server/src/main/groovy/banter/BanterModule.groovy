package banter

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import groovy.util.logging.Slf4j
import nz.net.ultraq.thymeleaf.LayoutDialect
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.Node
import org.thymeleaf.dialect.IDialect
import ratpack.openid.Attribute
import ratpack.openid.AuthenticationRequirement
import ratpack.openid.Required
import ratpack.openid.provider.google.GoogleAttribute

import javax.inject.Named
import javax.inject.Singleton

import static org.elasticsearch.node.NodeBuilder.nodeBuilder

@Slf4j
class BanterModule extends AbstractModule {

    @Override
    protected void configure() {
        def configDir = Main.locateConfigDirectory(Main.CONFIG_FILE_NAME)
        log.info("Using config from {}", configDir)
        bind(File).annotatedWith(Names.named("elasticsearch.yml")).toInstance(new File(configDir, "elasticsearch.yml"))
        def appConfig = new ConfigSlurper().parse(new File(configDir, Main.CONFIG_FILE_NAME).toURI().toURL())
        Names.bindProperties(binder(), appConfig.toProperties())
        bind(Indexer).asEagerSingleton()
        bind(Searcher).asEagerSingleton()
        bind(BanterBot).asEagerSingleton()
        Multibinder.newSetBinder(binder(), IDialect).addBinding().to(LayoutDialect)
        Multibinder.newSetBinder(binder(), AuthenticationRequirement).addBinding().toInstance(AuthenticationRequirement.of(~/\/auth\/.*/))
        def requiredOpenidAttributeBinder = Multibinder.newSetBinder(binder(), Attribute, Required)
        GoogleAttribute.values().each {requiredOpenidAttributeBinder.addBinding().toInstance(it)}
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
