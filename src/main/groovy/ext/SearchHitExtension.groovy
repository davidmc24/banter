package ext

import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.highlight.HighlightField

class SearchHitExtension {

    static HighlightField highlightField(SearchHit self, String fieldName) {
        return self.highlightFields()[fieldName]
    }

    static String maybeHighlightedFieldValue(SearchHit self, String fieldName) {
        def highlight = self.highlightField(fieldName)
        if (highlight?.fragments) {
            return highlight.fragments[0].string()
        }
        return self.field(fieldName)?.value
    }

}
