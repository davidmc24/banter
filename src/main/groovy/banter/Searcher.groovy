package banter

import com.google.inject.Inject
import groovy.util.logging.Slf4j
import org.elasticsearch.client.Client
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.sort.SortBuilders

import static banter.SearchConstants.INDEX
import static banter.SearchConstants.TYPE
import static org.elasticsearch.search.sort.SortOrder.ASC

@Slf4j
class Searcher {

    @Inject
    Client client

    SearchHits search(String channel, String query) {
        // TODO: better querying
        log.info("Running search")
        def searchResponse = client.prepareSearch(INDEX)
                .setTypes(TYPE)
//                .setPostFilter(FilterBuilders.termFilter("channel", message.parameters[0]))
                .addSort(SortBuilders.fieldSort("timestamp").order(ASC))
                .setFrom(0).setSize(100).setExplain(true)
                .addFields("timestamp", "channel", "nickname", "username", "realname", "text")
                .execute().actionGet()
        log.info("Search result: {} total hits", searchResponse.hits.totalHits)
        for (hit in searchResponse.hits.hits) {
            log.info("Hit: {}, {}", hit.sourceAsMap(), hit.fields.collect {"${it.key}:${it.value.value}}"})
        }
        return searchResponse.hits
    }

}
