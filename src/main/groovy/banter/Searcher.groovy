package banter

import com.google.inject.Inject
import groovy.util.logging.Slf4j
import org.apache.lucene.queryparser.xml.FilterBuilder
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.sort.SortBuilders

import static banter.SearchConstants.INDEX
import static banter.SearchConstants.TYPE
import static org.elasticsearch.search.sort.SortOrder.ASC

@Slf4j
class Searcher {

    @Inject
    Client client

    SearchHits search(String channel, String queryTerm) {
        // TODO: better querying
        log.info("Running search")
        def query = queryTerm ? QueryBuilders.termQuery("text", queryTerm) : QueryBuilders.matchAllQuery()
        if (channel) {
            query = QueryBuilders.filteredQuery(query, FilterBuilders.inFilter("channel", channel))
        }
        def searchRequest = client.prepareSearch(INDEX)
                        .setTypes(TYPE)
                        .addSort(SortBuilders.fieldSort("timestamp").order(ASC))
                        .setFrom(0).setSize(100).setExplain(true)
                        .addHighlightedField("text")
                        .setHighlighterPreTags("<b>")
                        .setHighlighterPostTags("</b>")
                        .addFields("timestamp", "channel", "nickname", "username", "realname", "text")
                        .setQuery(query)
        def searchResponse = searchRequest.get()
        log.info("Search result: {} total hits", searchResponse.hits.totalHits)
        for (hit in searchResponse.hits.hits) {
            log.info("Hit: {}, {}, {}", hit.sourceAsMap(), hit.fields.collect {"${it.key}:${it.value.value}}"}, hit.highlightFields())
        }
        return searchResponse.hits
    }

}
