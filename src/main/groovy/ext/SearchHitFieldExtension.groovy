package ext

import org.elasticsearch.search.SearchHitField
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.DurationFieldType
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.chrono.ISOChronology
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.format.PeriodFormat

class SearchHitFieldExtension {

    private static final chronology = ISOChronology.instanceUTC
    private static final parser = ISODateTimeFormat.dateOptionalTimeParser().withZone(DateTimeZone.UTC)
    private static final formatter = PeriodFormat.default

    static DateTime getDateValue(SearchHitField self) {
        return parser.parseDateTime(self.value)
    }

    static Duration getDuration(SearchHitField self) {
        return new Duration(getDateValue(self), DateTime.now())
    }

    static Period getPeriod(SearchHitField self) {
        return getDuration(self).toPeriod(PeriodType.forFields([DurationFieldType.years(), DurationFieldType.months(), DurationFieldType.days(), DurationFieldType.hours(), DurationFieldType.minutes(), DurationFieldType.seconds()] as DurationFieldType[]), chronology)
    }

    static String getPeriodString(SearchHitField self) {
        return formatter.print(getPeriod(self))
    }

}
