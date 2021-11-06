package com.swivel.cc.auth.domain.request;

import lombok.Getter;

@Getter
public class ForgotPasswordRequestDto extends RequestDto {

    private String email;

    @Override
    public boolean isRequiredAvailable() {
        if (email != null) {
            return isNonEmpty(email);
        } else {
            return false;
        }
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
