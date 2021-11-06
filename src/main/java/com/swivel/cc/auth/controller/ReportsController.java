package com.swivel.cc.auth.controller;

import com.swivel.cc.auth.configuration.Translator;
import com.swivel.cc.auth.domain.request.GroupedReportRequestDto;
import com.swivel.cc.auth.domain.request.ReportRequestDto;
import com.swivel.cc.auth.domain.response.AllGroupedProfileViewsResponseDto;
import com.swivel.cc.auth.domain.response.AllProfileViewsResponseDto;
import com.swivel.cc.auth.domain.response.BusinessProfileViewsResponseDto;
import com.swivel.cc.auth.domain.response.TopTenBusinessProfileViewsDto;
import com.swivel.cc.auth.enums.ErrorResponseStatusType;
import com.swivel.cc.auth.enums.GraphDateOption;
import com.swivel.cc.auth.enums.SuccessResponseStatusType;
import com.swivel.cc.auth.exception.AuthServiceException;
import com.swivel.cc.auth.exception.InvalidDateOptionException;
import com.swivel.cc.auth.exception.InvalidUserException;
import com.swivel.cc.auth.service.MerchantAudienceReachReportService;
import com.swivel.cc.auth.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * Reports controller
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/api/v1/reports")
public class ReportsController extends Controller {

    private final MerchantAudienceReachReportService merchantAudienceReachReportService;

    @Autowired
    public ReportsController(Translator translator, MerchantAudienceReachReportService merchantAudienceReachReportService) {
        super(translator);
        this.merchantAudienceReachReportService = merchantAudienceReachReportService;
    }

    /**
     * This method is used to get audience reach report for merchant business profile.
     *
     * @param userId           userId
     * @param merchantId       merchantId
     * @param page             page
     * @param size             size
     * @param reportRequestDto reportRequestDto
     * @return audience reach report for merchant business profile
     */
    @PostMapping(value = "/MERCHANT/{merchantId}/audience-reach/{page}/{size}", consumes = APPLICATION_JSON_UTF_8,
            produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> audienceReachReportForBusinessProfile(
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @PathVariable("merchantId") String merchantId,
            @Min(DEFAULT_PAGE) @PathVariable("page") Integer page,
            @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE)
            @Positive @PathVariable("size") Integer size,
            @RequestBody ReportRequestDto reportRequestDto) {

        try {
            if (reportRequestDto.isRequiredAvailable()) {
                if (!reportRequestDto.isValidDateRange()) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_DATE_RANGE);
                }
                if (!reportRequestDto.isSupportedDateRange()) {
                    return getBadRequestError(ErrorResponseStatusType.UNSUPPORTED_OPTION_FOR_DATE_RANGE);
                }
                Pageable pageable = PageRequest.of(page, size);
                var profileViewsList = merchantAudienceReachReportService
                        .generateAudienceReachReport(reportRequestDto.getOption(), reportRequestDto.getStartDate(),
                                reportRequestDto.getEndDate(), merchantId, page, size);
                var profileViewsPage = new PageImpl<>(profileViewsList, pageable, profileViewsList.size());
                return getSuccessResponse(SuccessResponseStatusType.GET_BUSINESS_PROFILE_VIEWS,
                        new AllProfileViewsResponseDto(profileViewsPage, profileViewsList));
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (InvalidUserException e) {
            log.error("Invalid merchantId: {}. Failed to get business profile views for date option: {}.", merchantId,
                    reportRequestDto.getOption(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (AuthServiceException e) {
            log.error("Getting audience reach report was failed for merchantId: {} by userId: {} for option: {}",
                    merchantId, userId, reportRequestDto.getOption(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get top 10 business profile views.
     *
     * @param userId   userId
     * @param timeZone timeZone
     * @param option   graph date option
     * @return top 10 business profile views.
     */
    @GetMapping(value = "/MERCHANT/audience-reach/top-ten/{option}",
            consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> audienceReachReportForTopTenBusinessProfiles(
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @PathVariable("option") String option) {

        try {
            if (!isValidTimeZone(timeZone)) {
                log.debug(LOG_INVALID_TIMEZONE, userId, timeZone);
                return getBadRequestError(ErrorResponseStatusType.INVALID_TIMEZONE);
            }
            List<BusinessProfileViewsResponseDto> profileViewsDtoList =
                    merchantAudienceReachReportService.getTopTenViewCounts(GraphDateOption.getOption(option), timeZone);
            return getSuccessResponse(SuccessResponseStatusType.GET_BUSINESS_PROFILE_VIEWS,
                    new TopTenBusinessProfileViewsDto(profileViewsDtoList));
        } catch (InvalidDateOptionException e) {
            log.error("Invalid dateOption. dateOption: {}, userId: {}", option, userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_DATE_OPTION);
        } catch (AuthServiceException e) {
            log.error("Getting top 10 audience reach report was failed for userId: {}, option: {}", userId, option, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get grouped audience reach report.
     *
     * @param userId                  userId
     * @param page                    page
     * @param size                    size
     * @param groupedReportRequestDto groupedReportRequestDto
     * @return grouped business profile views.
     */
    @PostMapping(value = "/MERCHANT/grouped-audience-reach/{page}/{size}", consumes = APPLICATION_JSON_UTF_8,
            produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> groupedAudienceReachReportForBusinessProfile(
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @Min(DEFAULT_PAGE) @PathVariable("page") Integer page,
            @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE) @Positive @PathVariable("size") Integer size,
            @RequestBody GroupedReportRequestDto groupedReportRequestDto) {

        try {
            if (groupedReportRequestDto.isRequiredAvailable()) {
                if (!groupedReportRequestDto.isValidDateRange()) {
                    return getBadRequestError(ErrorResponseStatusType.INVALID_DATE_RANGE);
                }
                Pageable pageable = PageRequest.of(page, size);
                var profileViewsList = merchantAudienceReachReportService
                        .getReportForDateRange(groupedReportRequestDto.getStartDate(),
                                groupedReportRequestDto.getEndDate(), page, size);
                var profileViewsPage = new PageImpl<>(profileViewsList, pageable, profileViewsList.size());
                return getSuccessResponse(SuccessResponseStatusType.GET_BUSINESS_PROFILE_VIEWS,
                        new AllGroupedProfileViewsResponseDto(profileViewsPage, profileViewsList));
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (AuthServiceException e) {
            log.error("Getting grouped audience reach report was failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }
}
