package com.swivel.cc.auth.domain.response;

import com.swivel.cc.auth.domain.entity.Contact;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Pending contact response list dto
 */
@Getter
public class PendingContactResponseListDto extends PageResponseDto {

    private final List<PendingContactResponseDto> allPendingContactInfo;

    public PendingContactResponseListDto(Page<Contact> page, String timeZone) {
        super(page);
        this.allPendingContactInfo = addToPendingContactResponseListDto(page, timeZone);
    }

    /**
     * Add contact information from page to dto list.
     *
     * @param page     page
     * @param timeZone timeZone
     * @return contactResponseList
     */
    private List<PendingContactResponseDto> addToPendingContactResponseListDto(Page<Contact> page, String timeZone) {
        List<PendingContactResponseDto> responseList = new ArrayList<>();
        page.getContent().forEach(contact -> responseList.add(new PendingContactResponseDto(contact, timeZone)));
        return responseList;
    }
}
