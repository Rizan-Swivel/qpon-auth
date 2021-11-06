package com.swivel.cc.auth.enums;

import lombok.Getter;

/**
 * Report date options
 */
@Getter
public enum ReportDateOption {

    DAILY("ga:date"),
    WEEKLY("ga:yearWeek"),
    MONTHLY("ga:yearMonth"),
    YEARLY("ga:year");

    private final String analyticsOption;

    ReportDateOption(String analyticsOption) {
        this.analyticsOption = analyticsOption;
    }
}