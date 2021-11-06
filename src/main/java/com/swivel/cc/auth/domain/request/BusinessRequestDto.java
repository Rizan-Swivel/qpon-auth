package com.swivel.cc.auth.domain.request;

import com.swivel.cc.auth.domain.response.ResponseDto;
import com.swivel.cc.auth.util.Validator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Merchant business details request Dto
 */
@Slf4j
@Getter
@Setter
@AllArgsConstructor
public class BusinessRequestDto extends RequestDto {

    private static final String INVALID_IMAGE_URL = "Invalid image URL : {}";
    private String merchantId;
    private String businessName;
    private String ownerName;
    private MobileNoRequestDto telephone;
    private String email;
    private String businessRegNo;
    private String address;
    private String imageUrl;
    private String webSite;
    private String facebook;
    private String instagram;

    @Override
    public boolean isRequiredAvailable() {
        return isNonEmpty(merchantId) && isNonEmpty(businessName) && isNonEmpty(ownerName) &&
                telephone.isRequiredAvailable();
    }

    @Override
    public String toLogJson() {
        BasicBusinessLog basicBusinessLog = new BasicBusinessLog(this);
        return basicBusinessLog.toLogJson();
    }

    /**
     * Checks if valid telephone number.
     *
     * @param validator validator
     * @return true/false
     */
    public boolean isValidTelephoneNo(Validator validator) {
        return validator.isValidMobileNoWithCountryCode(telephone.getNo());
    }

    /**
     * Checks if valid email address.
     *
     * @param validator validator
     * @return true/false
     */
    public boolean isContainingValidEmail(Validator validator) {
        return !isNonEmpty(email) || validator.isValidEmail(email);
    }

    /**
     * Checks if valid urls.
     *
     * @param validator validator
     * @return true/false
     */
    public boolean isContainingValidUrls(Validator validator) {
        if (isNonEmpty(imageUrl) && !validator.isValidUrl(imageUrl)) {
            log.error(INVALID_IMAGE_URL, imageUrl);
        }
        return (!isNonEmpty(webSite) || validator.isValidUrl(webSite))
                && (!isNonEmpty(facebook) || validator.isValidUrl(facebook))
                && (!isNonEmpty(instagram) || validator.isValidUrl(instagram));
    }

    /**
     * This class is for logging purpose only.
     */
    @Getter
    private class BasicBusinessLog implements ResponseDto {
        private final String merchantId;
        private final String businessName;

        public BasicBusinessLog(BusinessRequestDto businessRequestDto) {
            this.merchantId = businessRequestDto.getMerchantId();
            this.businessName = businessRequestDto.businessName;
        }

        @Override
        public String toLogJson() {
            return toJson();
        }
    }
}
