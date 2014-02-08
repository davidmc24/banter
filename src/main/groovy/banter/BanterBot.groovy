package banter

import groovy.util.logging.Slf4j
import jircer.IrcClient
import jircer.IrcMessage
import org.elasticsearch.client.Client
//import org.elasticsearch.search.sort.SortBuilders
//import org.elasticsearch.search.sort.SortOrder

import javax.inject.Inject
import javax.inject.Named

@Slf4j
class BanterBot extends IrcClient {

    String host
    int port
    String nick
    Client client

    @Inject
    BanterBot(@Named("ircHostname") String host, @Named("ircPort") int port, @Named("ircNickname") String nick,
              Client client) {
        this.host = host
        this.port = port
        this.nick = nick
        this.client = client
        connect()
    }

    private void connect() {
        log.info("Starting banterbot: {}, {}, {}", host, port, nick)
        connect(host, port)
        sendNick(nick)
        sendUser(nick, nick)
        // TODO: reconnects
        // TODO: join from endpoint
//        indexer.sendJoin(channel)
    }

    @Override
    void onMessage(IrcMessage message) {
        // Elasticsearch automatically creates a _timestamp which is indexed but not stored
//        log.info("Got message: {}", message)
        def doc = [
                timestamp: new Date(),
                user: message.prefix,
                channel: message.parameters[0],
                text: message.parameters[1]
        ]
        def indexResponse = client.prepareIndex("irc", "message").setSource(doc).execute().actionGet()
        log.info("Indexing result: created={}, index={}, type={}, id={}, version={}", indexResponse.created, indexResponse.index, indexResponse.type, indexResponse.id, indexResponse.version)
        // TODO: search in UI
//        log.info("Running search")
//        def searchResponse = client.prepareSearch()
        // TODO: sort
//        def searchResponse = client.prepareSearch("irc")
//                .setTypes("message")
////                .setPostFilter(FilterBuilders.termFilter("channel", message.parameters[0]))
//                .addSort(SortBuilders.fieldSort("timestamp").order(SortOrder.ASC))
//                .setFrom(0).setSize(100).setExplain(true)
//                .addFields("user", "channel", "text", "timestamp")
//                .execute().actionGet()
//        log.info("Search result: {} total hits", searchResponse.hits.totalHits)
//        for (hit in searchResponse.hits.hits) {
//            log.info("Hit: {}, {}", hit.sourceAsMap(), hit.fields.collect {"${it.key}:${it.value.value}}"})
//        }
    }

}
