package com.swivel.cc.auth.domain.response;

import com.swivel.cc.auth.domain.entity.User;
import com.swivel.cc.auth.enums.ApprovalStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicUserResponseDto {

    private String id;
    private String name;
    private String imageUrl;
    private ApprovalStatus approvalStatus;
    private DateResponseDto joinedOn;

    public BasicUserResponseDto(User user, String timeZone) {
        this.id = user.getId();
        this.name = user.getFullName();
        this.imageUrl = user.getImageUrl();
        this.approvalStatus = user.getApprovalStatus();
        this.joinedOn = new DateResponseDto(user.getCreatedAt().getTime(), timeZone,
                user.getCreatedAt(), "Joined on ");
    }
}
