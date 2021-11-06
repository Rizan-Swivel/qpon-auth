package com.swivel.cc.auth.domain.response;

import com.swivel.cc.auth.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Grouped profile views response dto
 */
@Getter
@Setter
@AllArgsConstructor
public class GroupedProfileViewsResponseDto implements ResponseDto {

    private String merchantId;
    private String businessName;
    private String imageUrl;
    private ApprovalStatus approvalStatus;
    private long totalViewCount;

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
