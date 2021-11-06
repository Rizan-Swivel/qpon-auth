package com.swivel.cc.auth.service;

import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.*;
import com.swivel.cc.auth.domain.entity.Business;
import com.swivel.cc.auth.domain.entity.BusinessProfileViews;
import com.swivel.cc.auth.domain.response.BusinessProfileViewsResponseDto;
import com.swivel.cc.auth.domain.response.GroupedProfileViewsResponseDto;
import com.swivel.cc.auth.enums.AnalyticsDimensionsAndMetrics;
import com.swivel.cc.auth.enums.GraphDateOption;
import com.swivel.cc.auth.enums.ReportDateOption;
import com.swivel.cc.auth.exception.AuthServiceException;
import com.swivel.cc.auth.repository.BusinessRepository;
import com.swivel.cc.auth.util.DateRangeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Audience reach report service
 */
@Service
@Slf4j
public class MerchantAudienceReachReportService {

    private static final String ANALYTICS_DATE_FORMAT = "yyyy-MM-dd";
    private static final String ALL = "ALL";
    private static final String DOUBLE_EQUALS = "==";
    private final int pageSize;
    private final String viewId;
    private final AnalyticsReporting analyticsReporting;
    private final BusinessRepository businessRepository;
    private final MerchantService merchantService;

    @Autowired
    public MerchantAudienceReachReportService(@Value("${googleAnalytics.pageSize}") int pageSize,
                                              @Value("${googleAnalytics.viewId}") String viewId,
                                              AnalyticsReporting analyticsReporting,
                                              BusinessRepository businessRepository, MerchantService merchantService) {
        this.pageSize = pageSize;
        this.viewId = viewId;
        this.analyticsReporting = analyticsReporting;
        this.businessRepository = businessRepository;
        this.merchantService = merchantService;
    }

    /**
     * This method is used to create audience reach report using Google Analytics.
     *
     * @param dateOption daily/weekly/monthly/yearly
     * @param startDate  startDate
     * @param endDate    endDate
     * @param merchantId merchantId/ALL
     * @return list of business profile views
     */
    public List<BusinessProfileViewsResponseDto> generateAudienceReachReport(ReportDateOption dateOption,
                                                                             long startDate, long endDate,
                                                                             String merchantId, int page, int size) {
        try {
            if (!merchantId.equals("ALL")) {
                merchantService.validateBusinessProfileWithMerchantId(merchantId);
            }
            String option = dateOption.getAnalyticsOption();
            String formattedStartDate = dateFormatter(startDate);
            String formattedEndDate = dateFormatter(endDate);

            GetReportsResponse reportResponse =
                    setAudienceReachRequest(option, formattedStartDate, formattedEndDate, merchantId, page, size);
            List<BusinessProfileViews> businessProfileViewsList = getAudienceReachList(reportResponse, dateOption);
            List<BusinessProfileViewsResponseDto> businessProfileViewsDtoList = new ArrayList<>();

            for (BusinessProfileViews businessProfileViews : businessProfileViewsList) {
                var businessProfileViewsDto = new BusinessProfileViewsResponseDto(businessProfileViews.getViewCount(),
                        businessProfileViews.getDisplayDate());
                Optional<Business> business =
                        businessRepository.getLatestApprovedBusinessByMerchantId(businessProfileViews.getMerchantId());
                business.ifPresent(value -> businessProfileViewsDto.setMerchant(businessProfileViews.getMerchantId(),
                        value.getBusinessName(), value.getMerchant().getApprovalStatus()));
                businessProfileViewsDtoList.add(businessProfileViewsDto);
            }
            return businessProfileViewsDtoList;
        } catch (AuthServiceException | DataAccessException e) {
            throw new AuthServiceException("Generating audience reach report was failed.", e);
        }
    }

    /**
     * This method is used to format date to support Google Analytics.
     *
     * @param timeStamp timeStamp
     * @return date in string
     */
    private String dateFormatter(long timeStamp) {
        Date date = new Date(timeStamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ANALYTICS_DATE_FORMAT);
        return simpleDateFormat.format(date);
    }

    /**
     * Queries the Analytics Reporting API V4.
     *
     * @param dateOption dateOption
     * @param startDate  startDate
     * @param endDate    endDate
     * @param merchantId merchantId
     * @return GetReportResponse The Analytics Reporting API V4 response.
     */
    private GetReportsResponse setAudienceReachRequest(String dateOption, String startDate, String endDate,
                                                       String merchantId, int page, int size) {
        // Create the DateRange object.
        DateRange dateRange = new DateRange();
        dateRange.setStartDate(startDate);
        dateRange.setEndDate(endDate);

        // Create the Metrics object.
        Metric pageViewMetric = new Metric()
                .setExpression(AnalyticsDimensionsAndMetrics.PAGE_VIEWS_METRIC.getAnalyticsDimension());

        Dimension merchantIdDimension =
                new Dimension().setName(AnalyticsDimensionsAndMetrics.MERCHANT_ID_DIMENSION.getAnalyticsDimension());
        Dimension dateOptionDimension = new Dimension().setName(dateOption);

        // Create the ReportRequest object.
        ReportRequest request = new ReportRequest()
                .setViewId(viewId)
                .setDateRanges(List.of(dateRange))
                .setMetrics(List.of(pageViewMetric))
                .setDimensions(Arrays.asList(merchantIdDimension, dateOptionDimension))
                .setPageSize(size)
                .setPageToken(String.valueOf(page));
        if (!merchantId.equals(ALL)) {
            request.setFiltersExpression(AnalyticsDimensionsAndMetrics.MERCHANT_ID_DIMENSION.getAnalyticsDimension() +
                    DOUBLE_EQUALS + merchantId);
        }
        return sendAnalyticsRequest(request);
    }

    /**
     * This method will convert Google Analytics response into list.
     *
     * @param response   An Analytics Reporting API V4 response.
     * @param dateOption dateOption
     * @return list of business profile views.
     */
    private List<BusinessProfileViews> getAudienceReachList(GetReportsResponse response, ReportDateOption dateOption) {

        List<BusinessProfileViews> businessProfileViewsList = new ArrayList<>();
        Report report = response.getReports().get(0);
        List<ReportRow> rows = report.getData().getRows();
        if (rows != null) {
            for (ReportRow row : rows) {
                List<String> dimensions = row.getDimensions();
                List<DateRangeValues> metrics = row.getMetrics();
                businessProfileViewsList.add(new BusinessProfileViews(dimensions.get(0), dimensions.get(1),
                        Long.parseLong(metrics.get(0).getValues().get(0)), dateOption));
            }
        }
        return businessProfileViewsList;
    }

    /**
     * This method is used to create top 10 audience reach report using Google Analytics.
     *
     * @param graphDateOption graphDateOption
     * @param timeZone        timeZone
     * @return list of business profile views
     */
    public List<BusinessProfileViewsResponseDto> getTopTenViewCounts(GraphDateOption graphDateOption, String timeZone) {
        try {
            GetReportsResponse response = setTopTenAudienceReachRequest(graphDateOption, timeZone);
            List<BusinessProfileViews> businessProfileViewsList = getAudienceReachListForDateRange(response);
            List<BusinessProfileViewsResponseDto> businessProfileViewsDtoList = new ArrayList<>();

            for (BusinessProfileViews businessProfileViews : businessProfileViewsList) {
                var businessProfileViewsDto = new BusinessProfileViewsResponseDto(businessProfileViews.getViewCount(),
                        graphDateOption.getOption());
                Optional<Business> business =
                        businessRepository.getLatestApprovedBusinessByMerchantId(businessProfileViews.getMerchantId());
                business.ifPresent(value -> businessProfileViewsDto.setMerchant(businessProfileViews.getMerchantId(),
                        value.getBusinessName(), value.getMerchant().getApprovalStatus()));
                businessProfileViewsDtoList.add(businessProfileViewsDto);
            }
            return businessProfileViewsDtoList;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Generating top 10 audience reach report was failed.", e);
        }
    }

    /**
     * This method is used to create query for top 10 audience reach report.
     *
     * @param graphDateOption graphDateOption
     * @param timeZone        timeZone
     * @return GetReportResponse The Analytics Reporting API V4 response.
     */
    private GetReportsResponse setTopTenAudienceReachRequest(GraphDateOption graphDateOption, String timeZone) {

        DateRange dateRange = new DateRangeConverter(timeZone, graphDateOption).createDateRange();
        Metric pageViewMetric = new Metric()
                .setExpression(AnalyticsDimensionsAndMetrics.PAGE_VIEWS_METRIC.getAnalyticsDimension());
        Dimension merchantIdDimension =
                new Dimension().setName(AnalyticsDimensionsAndMetrics.MERCHANT_ID_DIMENSION.getAnalyticsDimension());
        OrderBy orderBy = new OrderBy()
                .setFieldName(AnalyticsDimensionsAndMetrics.PAGE_VIEWS_METRIC.getAnalyticsDimension())
                .setSortOrder("DESCENDING");

        ReportRequest request = new ReportRequest()
                .setViewId(viewId)
                .setDateRanges(List.of(dateRange))
                .setMetrics(List.of(pageViewMetric))
                .setDimensions(List.of(merchantIdDimension))
                .setOrderBys(List.of(orderBy))
                .setPageSize(pageSize);
        return sendAnalyticsRequest(request);
    }

    /**
     * This method will convert Google Analytics response into list.
     * Adds merchantId & view counts only.
     *
     * @param response An Analytics Reporting API V4 response.
     * @return list of business profile views.
     */
    private List<BusinessProfileViews> getAudienceReachListForDateRange(GetReportsResponse response) {

        List<BusinessProfileViews> businessProfileViewsList = new ArrayList<>();
        Report report = response.getReports().get(0);
        List<ReportRow> rows = report.getData().getRows();
        if (rows != null) {
            for (ReportRow row : rows) {
                List<String> dimensions = row.getDimensions();
                List<DateRangeValues> metrics = row.getMetrics();
                businessProfileViewsList.add(new BusinessProfileViews(dimensions.get(0),
                        Long.parseLong(metrics.get(0).getValues().get(0))));
            }
        }
        return businessProfileViewsList;
    }

    /**
     * Queries the Analytics Reporting API V4.
     *
     * @param request report request.
     * @return GetReportResponse The Analytics Reporting API V4 response.
     */
    private GetReportsResponse sendAnalyticsRequest(ReportRequest request) {
        try {
            ArrayList<ReportRequest> requests = new ArrayList<>();
            requests.add(request);

            // Create the GetReportsRequest object.
            GetReportsRequest getReport = new GetReportsRequest().setReportRequests(requests);

            // Call the batchGet method.
            return this.analyticsReporting.reports().batchGet(getReport).execute();
        } catch (IOException e) {
            throw new AuthServiceException("Creating google analytics API request failed", e);
        }
    }

    /**
     * This method is used to get audience reach report for date range.
     *
     * @param startDate start date
     * @param endDate   end date
     * @param page      page
     * @param size      size
     * @return merchant list with view counts.
     */
    public List<GroupedProfileViewsResponseDto> getReportForDateRange(long startDate, long endDate,
                                                                      int page, int size) {
        String formattedStartDate = dateFormatter(startDate);
        String formattedEndDate = dateFormatter(endDate);
        GetReportsResponse reportResponse = setRequestForDateRange(formattedStartDate, formattedEndDate, page, size);
        List<BusinessProfileViews> businessProfileViewsList = getAudienceReachListForDateRange(reportResponse);
        List<GroupedProfileViewsResponseDto> groupedProfileViewsResponseDtoList = new ArrayList<>();

        try {
            for (BusinessProfileViews businessProfileViews : businessProfileViewsList) {
                Optional<Business> business =
                        businessRepository.getLatestApprovedBusinessByMerchantId(businessProfileViews.getMerchantId());
                business.ifPresent(value -> groupedProfileViewsResponseDtoList.add(
                        new GroupedProfileViewsResponseDto(businessProfileViews.getMerchantId(),
                                value.getBusinessName(), value.getImageUrl(), value.getApprovalStatus(),
                                businessProfileViews.getViewCount())));
            }
            return groupedProfileViewsResponseDtoList;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Generating report for date range was failed.", e);
        }
    }

    /**
     * This method is used to create query for audience reach report in selected date range.
     *
     * @param startDate start date
     * @param endDate   end date
     * @param page      page
     * @param size      size
     * @return GetReportResponse The Analytics Reporting API V4 response.
     */
    private GetReportsResponse setRequestForDateRange(String startDate, String endDate, int page, int size) {

        DateRange dateRange = new DateRange();
        dateRange.setStartDate(startDate);
        dateRange.setEndDate(endDate);

        Metric pageViewMetric = new Metric()
                .setExpression(AnalyticsDimensionsAndMetrics.PAGE_VIEWS_METRIC.getAnalyticsDimension());
        Dimension merchantIdDimension =
                new Dimension().setName(AnalyticsDimensionsAndMetrics.MERCHANT_ID_DIMENSION.getAnalyticsDimension());
        ReportRequest request = new ReportRequest()
                .setViewId(viewId)
                .setDateRanges(List.of(dateRange))
                .setMetrics(List.of(pageViewMetric))
                .setDimensions(List.of(merchantIdDimension))
                .setPageSize(size)
                .setPageToken(String.valueOf(page));

        return sendAnalyticsRequest(request);
    }
}
