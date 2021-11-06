package com.swivel.cc.auth.domain.response;

import com.swivel.cc.auth.domain.entity.Contact;
import com.swivel.cc.auth.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Pending contact response Dto
 */
@Getter
@AllArgsConstructor
public class PendingContactResponseDto implements ResponseDto {

    private String contactId;
    private BasicUserResponseDto merchant;
    private ApprovalStatus approvalStatus;
    private DateResponseDto updatedAt;

    public PendingContactResponseDto(Contact contact, String timeZone) {
        this.contactId = contact.getId();
        this.merchant = new BasicUserResponseDto(contact.getMerchant(), timeZone);
        this.approvalStatus = contact.getApprovalStatus();
        this.updatedAt = new DateResponseDto(contact.getUpdatedAt().getTime(), timeZone, contact.getUpdatedAt());
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
