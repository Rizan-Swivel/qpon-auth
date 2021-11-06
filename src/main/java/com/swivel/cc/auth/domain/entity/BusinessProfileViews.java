package com.swivel.cc.auth.domain.entity;

import com.swivel.cc.auth.domain.response.ReportDateResponseDto;
import com.swivel.cc.auth.enums.ReportDateOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Business profile views
 */
@Getter
@Setter
@AllArgsConstructor
public class BusinessProfileViews {

    private String merchantId;
    private long viewCount;
    private String displayDate;

    public BusinessProfileViews(String merchantId, String displayDate, Long viewCount, ReportDateOption dateOption) {
        this.merchantId = merchantId;
        this.displayDate = new ReportDateResponseDto(dateOption, displayDate).getDisplayDate();
        this.viewCount = viewCount;
    }
    public BusinessProfileViews(String merchantId, Long viewCount){
        this.merchantId = merchantId;
        this.viewCount = viewCount;
    }

}
