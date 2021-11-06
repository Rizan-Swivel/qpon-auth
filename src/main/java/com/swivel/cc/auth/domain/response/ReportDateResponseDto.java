package com.swivel.cc.auth.domain.response;

import com.swivel.cc.auth.enums.ReportDateOption;
import com.swivel.cc.auth.exception.AuthServiceException;
import lombok.Getter;
import lombok.Setter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Report date response dto
 * Used for Google Analytics date formats.
 * Formats are:
 * Daily: 20210101 (2021 January 1)
 * Weekly: 202110 (2021 week 10)
 * Monthly: 202101 (2021 January)
 * Yearly: 2021
 */
@Getter
@Setter
public class ReportDateResponseDto implements ResponseDto {

    private String displayDate;

    public ReportDateResponseDto(ReportDateOption dateOption, String dateInString) {
        try {
            switch (dateOption) {
                case DAILY:
                    Date date = new SimpleDateFormat("yyyyMMdd").parse(dateInString);
                    var sdf = new SimpleDateFormat("d MMM yyyy");
                    this.displayDate = sdf.format(date);
                    break;
                case WEEKLY:
                    Date date1 = new SimpleDateFormat("yyyyww").parse(dateInString);
                    var sdf1 = new SimpleDateFormat("WW MMM yyyy");
                    this.displayDate = addSuffixForWeek(sdf1.format(date1));
                    break;
                case MONTHLY:
                    Date date2 = new SimpleDateFormat("yyyyMM").parse(dateInString);
                    var sdf2 = new SimpleDateFormat("MMM yyyy");
                    this.displayDate = sdf2.format(date2);
                    break;
                case YEARLY:
                    Date date3 = new SimpleDateFormat("yyyy").parse(dateInString);
                    var sdf3 = new SimpleDateFormat("yyyy");
                    this.displayDate = sdf3.format(date3);
                    break;
                default:
                    this.displayDate = "";
            }
        } catch (ParseException e) {
            throw new AuthServiceException("Converting to display date text failed", e);
        }
    }

    /**
     * This method is used to add suffix for week display text.
     *
     * @param weekString weekString
     * @return week with suffix
     */
    private String addSuffixForWeek(String weekString) {
        String weekWithSuffix;
        String monthAndYear = weekString.substring(3);
        char weekIndex = weekString.charAt(1);

        switch (weekIndex) {
            case '1':
                weekWithSuffix = weekIndex + "st";
                break;
            case '2':
                weekWithSuffix = weekIndex + "nd";
                break;
            case '3':
                weekWithSuffix = weekIndex + "rd";
                break;
            case '4':
            case '5':
            case '6':
                weekWithSuffix = weekIndex + "th";
                break;
            default:
                weekWithSuffix = "";
        }
        return weekWithSuffix + " week - " + monthAndYear;
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
