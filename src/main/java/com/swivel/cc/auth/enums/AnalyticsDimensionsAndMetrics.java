package com.swivel.cc.auth.enums;

import lombok.Getter;

/**
 * Analytics dimensions
 */
@Getter
public enum AnalyticsDimensionsAndMetrics {

    MERCHANT_ID_DIMENSION("ga:dimension3"),
    PAGE_VIEWS_METRIC("ga:pageviews");

    private final String analyticsDimension;

    AnalyticsDimensionsAndMetrics(String analyticsDimension) {
        this.analyticsDimension = analyticsDimension;
    }
}
