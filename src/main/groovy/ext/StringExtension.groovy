package ext

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

class StringExtension {

    private static final parser = ISODateTimeFormat.dateOptionalTimeParser().withZone(DateTimeZone.UTC)

    static DateTime parseDateTime(String self) {
        return parser.parseDateTime(self)
    }

    static String encodeURIComponent(String self) {
        return URLEncoder.encode(self, "UTF-8")
    }

}
