package com.swivel.cc.auth.util;

import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.swivel.cc.auth.enums.GraphDateOption;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * This class covert Graph Date Option into Date Range
 */
public class DateRangeConverter {

    private final String timeZone;
    private final DayOfWeek firstDayOfWeek;
    private final GraphDateOption graphDateOption;

    public DateRangeConverter(String timeZone, GraphDateOption graphDateOption) {
        this.timeZone = timeZone;
        this.graphDateOption = graphDateOption;
        this.firstDayOfWeek = WeekFields.of(Locale.UK).getFirstDayOfWeek();
    }

    private LocalDate getFirstDay() {
        return LocalDate.now(ZoneId.of(timeZone)).with(TemporalAdjusters.previousOrSame(this.firstDayOfWeek));
    }

    /**
     * This method create date range using GraphDateOption
     *
     * @return DateRange
     */
    public DateRange createDateRange() {
        var dateRange = new DateRange();
        switch (graphDateOption) {
            case YESTERDAY:
                dateRange.setStartDate(String.valueOf(LocalDate.now().minusDays(1))).
                        setEndDate(String.valueOf(LocalDate.now()));
                break;
            case THIS_WEEK:
                dateRange.setStartDate(String.valueOf(getFirstDay())).
                        setEndDate(String.valueOf(LocalDate.now()));
                break;
            case THIS_MONTH:
                dateRange.setStartDate(String.valueOf(LocalDate.now().withDayOfMonth(1))).
                        setEndDate(String.valueOf(LocalDate.now()));
                break;
            case THIS_YEAR:
                dateRange.setStartDate(String.valueOf(LocalDate.now().withDayOfYear(1))).
                        setEndDate(String.valueOf(LocalDate.now()));
                break;
            default:
                dateRange.setStartDate(String.valueOf(LocalDate.now())).
                        setEndDate(String.valueOf(LocalDate.now()));
                break;
        }

        return dateRange;
    }

}