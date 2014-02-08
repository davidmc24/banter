package banter

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.name.Names
import org.elasticsearch.node.Node
import org.elasticsearch.client.Client
import javax.inject.Singleton

import static org.elasticsearch.node.NodeBuilder.nodeBuilder

class BanterModule extends AbstractModule {

    @Override
    protected void configure() {
        // TODO: use a config file
        bindConstant().annotatedWith(Names.named("ircHostname")).to("localhost")
        bindConstant().annotatedWith(Names.named("ircPort")).to(6667)
        bindConstant().annotatedWith(Names.named("ircNickname")).to("BanterBot")
        bind(BanterBot).asEagerSingleton()
    }

    @Provides
    @Singleton
    Node node() {
        // TODO: adjust node settings
        return nodeBuilder().local(true).node()
    }

    @Provides
    Client client(Node node) {
        return node.client()
    }

}
