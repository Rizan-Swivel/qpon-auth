package com.swivel.cc.auth.domain.response;

import com.swivel.cc.auth.domain.entity.Business;
import com.swivel.cc.auth.enums.ApprovalStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Basic merchant response Dto
 */
@Getter
@Setter
public class BasicMerchantResponseDto {

    private String id;
    private String name;
    private String imageUrl;
    private ApprovalStatus approvalStatus;
    private DateResponseDto joinedOn;

    public BasicMerchantResponseDto(Business business, String timeZone) {
        this.id = business.getMerchant().getId();
        this.name = business.getBusinessName();
        this.imageUrl = business.getImageUrl();
        this.approvalStatus = business.getApprovalStatus();
        this.joinedOn = new DateResponseDto(business.getMerchant().getCreatedAt().getTime(), timeZone,
                business.getMerchant().getCreatedAt(), "Joined on ");
    }
}
