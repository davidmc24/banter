package banter

import com.google.inject.Inject
import groovy.util.logging.Slf4j
import org.apache.lucene.queryparser.xml.FilterBuilder
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.FilterBuilders
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.facet.FacetBuilders
import org.elasticsearch.search.facet.terms.TermsFacet
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
                        .setFrom(0).setSize(500)
                        .addHighlightedField("text")
                        .setHighlighterPreTags('<span class="highlight">')
                        .setHighlighterPostTags('</span>')
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
        def start = timestamp.withTimeAtStartOfDay()
        def end = start.plusDays(1)
        def query = QueryBuilders.constantScoreQuery(FilterBuilders.andFilter(
                FilterBuilders.inFilter("channel", channel),
                FilterBuilders.rangeFilter("timestamp").gte(start).lt(end)
        ))
        def searchRequest = client.prepareSearch(INDEX)
                        .setTypes(TYPE)
                        .addSort(SortBuilders.fieldSort("timestamp").order(ASC))
                        .setFrom(0).setSize(500)
                        .setHighlighterQuery(QueryBuilders.termQuery("text", queryTerm))
                        .addHighlightedField("text")
                        .setHighlighterPreTags('<span class="highlight">')
                        .setHighlighterPostTags('</span>')
                        .addFields("timestamp", "channel", "nickname", "username", "realname", "text")
                        .setQuery(query)
        def searchResponse = searchRequest.get()
        log.info("Search result: {} total hits", searchResponse.hits.totalHits)
        for (hit in searchResponse.hits.hits) {
            log.info("Hit: {}, {}, {}", hit.sourceAsMap(), hit.fields.collect {"${it.key}:${it.value.value}}"}, hit.highlightFields())
        }
        return searchResponse.hits
    }

    Set<String> getKnownChannels() {
        def searchRequest = client.prepareSearch(INDEX)
                .setTypes(TYPE)
                .addFacet(FacetBuilders.termsFacet("channel").field("channel"))
                .setSearchType(SearchType.COUNT)
        def searchResponse = searchRequest.get()
        return searchResponse.facets.facet(TermsFacet, "channel").entries.collect {it.term.string()} as Set
    }

}
