package com.swivel.cc.auth.domain.response;

import com.swivel.cc.auth.enums.ReportDateOption;
import com.swivel.cc.auth.exception.AuthServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link ReportDateResponseDto} class
 */
class ReportDateResponseDtoTest {

    /**
     * Daily
     * date: 2021 01 01, given: 20210101, expect: 1 Jan 2021
     */
    @Test
    void Should_ReturnReportDateResponseDto_When_DailyOptionIsGiven() {
        ReportDateResponseDto reportDateResponseDto =
                new ReportDateResponseDto(ReportDateOption.DAILY, "20210101");
        assertEquals("1 Jan 2021", reportDateResponseDto.getDisplayDate());
    }

    /**
     * Weekly
     * date: 2021 week 1, given: 202101, expect: 5th week - Dec 2020
     */
    @Test
    void Should_ReturnReportDateResponseDto_When_WeeklyOptionIsGiven() {
        ReportDateResponseDto reportDateResponseDto =
                new ReportDateResponseDto(ReportDateOption.WEEKLY, "202101");
        assertEquals("5th week - Dec 2020", reportDateResponseDto.getDisplayDate());
    }

    /**
     * Monthly
     * date: 2021 January, given: 202101, expect: Jan 2021
     */
    @Test
    void Should_ReturnReportDateResponseDto_When_MonthlyOptionIsGiven() {
        ReportDateResponseDto reportDateResponseDto =
                new ReportDateResponseDto(ReportDateOption.MONTHLY, "202101");
        assertEquals("Jan 2021", reportDateResponseDto.getDisplayDate());
    }

    /**
     * Yearly
     * year: 2021, given: 2021, expect: 2021
     */
    @Test
    void Should_ReturnReportDateResponseDto_When_YearlyOptionIsGiven() {
        ReportDateResponseDto reportDateResponseDto =
                new ReportDateResponseDto(ReportDateOption.YEARLY, "2021");
        assertEquals("2021", reportDateResponseDto.getDisplayDate());
    }

    /**
     * Any other string
     * given: qwerty, expect: exception message
     */
    @Test
    void Should_ReturnReportDateResponseDto_When_RandomStringIsGiven() {
        Exception exception = assertThrows(AuthServiceException.class,
                () -> new ReportDateResponseDto(ReportDateOption.DAILY, "qwerty"));
        assertTrue(exception.getMessage().contains("Converting to display date text failed"));
    }
}