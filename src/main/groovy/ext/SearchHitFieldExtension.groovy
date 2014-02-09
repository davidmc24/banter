package ext

import org.elasticsearch.search.SearchHitField
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.DurationFieldType
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.chrono.ISOChronology
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.PeriodFormat

class SearchHitFieldExtension {

    private static final chronology = ISOChronology.instanceUTC
    private static final periodFormatter = PeriodFormat.default
    private static final dateTimeFormatter = DateTimeFormat.mediumDateTime()
    private static final timeFormatter = DateTimeFormat.shortTime()

    static DateTime getDateTimeValue(SearchHitField self) {
        return (self.value as String).parseDateTime()
    }

    static String getDateTimeString(SearchHitField self) {
        return dateTimeFormatter.print(getDateTimeValue(self))
    }

    static Date getDateValue(SearchHitField self) {
        return getDateTimeValue(self).toDate()
    }

    static Duration getDuration(SearchHitField self) {
        return new Duration(getDateTimeValue(self), DateTime.now())
    }

    static Period getPeriod(SearchHitField self) {
        // TODO: intelligent selection of fields
        return getDuration(self).toPeriod(PeriodType.forFields([DurationFieldType.years(), DurationFieldType.months(), DurationFieldType.days(), DurationFieldType.hours(), DurationFieldType.minutes(), DurationFieldType.seconds()] as DurationFieldType[]), chronology)
    }

    static String getPeriodString(SearchHitField self) {
        return periodFormatter.print(getPeriod(self))
    }

    static String getTimeString(SearchHitField self) {
        return timeFormatter.print(getDateTimeValue(self))
    }

}
