package com.swivel.cc.auth.domain.response;

import com.swivel.cc.auth.enums.ApprovalStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessProfileViewsResponseDto implements ResponseDto {

    private MerchantBusinessDto merchant;
    private long viewCount;
    private String displayDate;

    public BusinessProfileViewsResponseDto(long viewCount, String displayDate) {
        this.viewCount = viewCount;
        this.displayDate = displayDate;
    }

    public void setMerchant(String merchantId, String businessName, ApprovalStatus approvalStatus) {
        this.merchant = new MerchantBusinessDto(merchantId, businessName, approvalStatus);
    }

    /**
     * This method converts object to json string for logging purpose.
     * PII data should be obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }
}
