package banter

import groovy.util.logging.Slf4j
import jircer.IrcClient
import jircer.IrcMessage
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.node.Node as SearchNode
import org.elasticsearch.client.Client as SearchClient
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder

import static org.elasticsearch.node.NodeBuilder.*

@Slf4j
class Indexer extends IrcClient {

    private final SearchNode node
    private final SearchClient client

    Indexer() {
        // TODO: make this client-only
        node = nodeBuilder().local(true).node()
        client = node.client()
        // TODO: how is this shut down cleanly?
//        try {
//            client
//        } finally {
//            node.close()
//        }
    }

    static void main(String... args) {
        def ircHost = args[0]
        def channel = args[1]
        def nick = "indexbot"
        def indexer = new Indexer()
        indexer.connect(ircHost)
        // TODO: reconnect
        indexer.sendNick(nick)
        indexer.sendUser(nick, nick)
        indexer.sendJoin(channel)
        indexer.awaitClose()
        indexer.disconnect()
//        def builder = new Configuration.Builder().setName("IndexBot")
//        builder.setCapEnabled(true)
//        builder.addCapHandler(new TLSCapHandler(new UtilSSLSocketFactory().trustAllCertificates(), true))
//        builder.addListener(new MyListener())
//        builder.setServerHostname("localhost")
//        def config = builder.buildConfiguration()
//        def bot = new PircBotX(config)
//        bot.startBot()
    }

    @Override
    void onMessage(IrcMessage message) {
        // Elasticsearch automatically creates a _timestamp which is indexed but not stored
        log.info("Got message: {}", message)
        def doc = [
                timestamp: new Date(),
                user: message.prefix,
                channel: message.parameters[0],
                text: message.parameters[1]
        ]
        def indexResponse = client.prepareIndex("irc", "message").setSource(doc).execute().actionGet()
        log.info("Indexing result: created={}, index={}, type={}, id={}, version={}", indexResponse.created, indexResponse.index, indexResponse.type, indexResponse.id, indexResponse.version)
        log.info("Running search")
//        def searchResponse = client.prepareSearch()
        // TODO: sort
        def searchResponse = client.prepareSearch("irc")
                .setTypes("message")
//                .setPostFilter(FilterBuilders.termFilter("channel", message.parameters[0]))
                .addSort(SortBuilders.fieldSort("timestamp").order(SortOrder.ASC))
                .setFrom(0).setSize(100).setExplain(true)
                .addFields("user", "channel", "text", "timestamp")
                .execute().actionGet()
        log.info("Search result: {} total hits", searchResponse.hits.totalHits)
        for (hit in searchResponse.hits.hits) {
            log.info("Hit: {}, {}", hit.sourceAsMap(), hit.fields.collect {"${it.key}:${it.value.value}}"})
        }
    }

}
