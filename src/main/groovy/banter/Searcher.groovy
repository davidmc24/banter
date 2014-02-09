package banter

import com.google.inject.Inject
import groovy.util.logging.Slf4j
import org.apache.lucene.queryparser.xml.FilterBuilder
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.sort.SortBuilders
import org.joda.time.DateTime

import static banter.SearchConstants.INDEX
import static banter.SearchConstants.TYPE
import static org.elasticsearch.search.sort.SortOrder.ASC

@Slf4j
class Searcher {

    @Inject
    Client client

    // TODO: remove explains
    // TODO: support paging
    // TODO: load all fields?
    // TODO: better re-use

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

    SearchHits searchContext(String channel, String queryTerm, DateTime timestamp) {
        // TODO: better querying
        log.info("Running search")
        def start = timestamp.minusHours(1)
        def end = timestamp.plusHours(1)
        def query = QueryBuilders.constantScoreQuery(FilterBuilders.andFilter(
                FilterBuilders.inFilter("channel", channel),
                FilterBuilders.rangeFilter("timestamp").gte(start).lt(end)
        ))
        def searchRequest = client.prepareSearch(INDEX)
                        .setTypes(TYPE)
                        .addSort(SortBuilders.fieldSort("timestamp").order(ASC))
                        .setFrom(0).setSize(100).setExplain(true)
                        .setHighlighterQuery(QueryBuilders.termQuery("text", queryTerm))
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
