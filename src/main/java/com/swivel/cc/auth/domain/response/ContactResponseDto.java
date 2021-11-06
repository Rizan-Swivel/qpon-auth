package com.swivel.cc.auth.domain.response;

import com.swivel.cc.auth.domain.entity.Contact;
import com.swivel.cc.auth.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Contact details response Dto
 */
@Getter
@Setter
@AllArgsConstructor
public class ContactResponseDto implements ResponseDto {

    private String contactId;
    private String merchantId;
    private String name;
    private String designation;
    private ApprovalStatus approvalStatus;
    private String email;
    private MobileNoResponseDto telephone;
    private DateResponseDto createdAt;
    private DateResponseDto updatedAt;


    public ContactResponseDto(Contact contact, String timeZone) {
        this.contactId = contact.getId();
        this.merchantId = contact.getMerchant().getId();
        this.name = contact.getName();
        this.designation = contact.getDesignation();
        this.telephone = new MobileNoResponseDto(contact.getTelephone());
        this.email = contact.getEmail();
        this.createdAt = new DateResponseDto(contact.getCreatedAt().getTime(), timeZone, contact.getCreatedAt());
        this.updatedAt = new DateResponseDto(contact.getUpdatedAt().getTime(), timeZone, contact.getUpdatedAt());
        this.approvalStatus = contact.getApprovalStatus();
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}