package banter

import org.elasticsearch.search.SearchHit

class MessageSearchHit {

    private final SearchHit hit

    MessageSearchHit(SearchHit hit) {
        this.hit = hit
    }

    String getChannel() {
        return hit.field('channel').value
    }

    String getRealname() {
        return hit.field('realname').value
    }

    String getDateTimeString() {
        return hit.field('timestamp').dateTimeString
    }

    String getPeriodString() {
        return hit.field('timestamp').periodString
    }

    String getTimestampString() {
        return hit.field('timestamp').dateTimeValue.toString()
    }

    String getNumericTimeString() {
        return hit.field('timestamp').numericTimeString
    }

    String getTimeString() {
        return hit.field('timestamp').timeString
    }

    String getMaybeHighlightedText() {
        return hit.maybeHighlightedFieldValue('text')
    }

    String getContextURL(String q) {
        return "/context?channel=${channel.encodeURIComponent()}&q=${q.encodeURIComponent()}&timestamp=${timestampString.encodeURIComponent()}#${numericTimeString.encodeURIComponent()}"
    }

}
