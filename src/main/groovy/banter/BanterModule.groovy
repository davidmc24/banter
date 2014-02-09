package banter

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.name.Names
import groovy.util.logging.Slf4j
import org.elasticsearch.client.Client
import org.elasticsearch.node.Node

import javax.inject.Singleton

import static com.google.inject.Scopes.SINGLETON
import static org.elasticsearch.node.NodeBuilder.nodeBuilder

@Slf4j
class BanterModule extends AbstractModule {

    @Override
    protected void configure() {
        // TODO: use a config file
        bindConstant().annotatedWith(Names.named("ircHostname")).to("localhost")
        bindConstant().annotatedWith(Names.named("ircPort")).to(6667)
        bindConstant().annotatedWith(Names.named("ircNickname")).to("BanterBot")
        bindConstant().annotatedWith(Names.named("ircUsername")).to("banterbot")
        bindConstant().annotatedWith(Names.named("ircRealname")).to("Banter Bot")
        bindConstant().annotatedWith(Names.named("ircPassword")).to("")
        bind(BanterBot).in(SINGLETON)
        bind(Indexer).in(SINGLETON)
        bind(Searcher).in(SINGLETON)
    }

    @Provides
    @Singleton
    Node node() {
        // TODO: adjust node settings
        return nodeBuilder().local(true).node()
    }

    @Provides
    @Singleton
    Client client(Node node) {
        return node.client()
    }

}
