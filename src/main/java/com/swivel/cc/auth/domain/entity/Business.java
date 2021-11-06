package com.swivel.cc.auth.domain.entity;

import com.swivel.cc.auth.domain.request.BusinessRequestDto;
import com.swivel.cc.auth.enums.ApprovalStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class Business {

    @Transient
    private static final String BUSINESS_ID_PREFIX = "bisid-";
    @Id
    private String id;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "merchantId")
    private User merchant;
    private String businessName;
    private String ownerName;
    private String telephone;
    private String email;
    private String businessRegNo;
    private String address;
    private String imageUrl;
    private String webSite;
    private String facebook;
    private String instagram;
    private Date createdAt;
    private Date updatedAt;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    public Business(BusinessRequestDto businessRequestDto, User user) {
        this.id = BUSINESS_ID_PREFIX + UUID.randomUUID();
        this.merchant = user;
        this.businessName = businessRequestDto.getBusinessName();
        this.ownerName = businessRequestDto.getOwnerName();
        this.telephone = businessRequestDto.getTelephone().getNo();
        this.email = businessRequestDto.getEmail();
        this.businessRegNo = businessRequestDto.getBusinessRegNo();
        this.address = businessRequestDto.getAddress();
        this.imageUrl = businessRequestDto.getImageUrl();
        this.webSite = businessRequestDto.getWebSite();
        this.facebook = businessRequestDto.getFacebook();
        this.instagram = businessRequestDto.getInstagram();
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
        this.approvalStatus = ApprovalStatus.PENDING;
    }

    public Business(BusinessRequestDto businessRequestDto, User user, Date createdAt) {
        this.id = BUSINESS_ID_PREFIX + UUID.randomUUID();
        this.merchant = user;
        this.businessName = businessRequestDto.getBusinessName();
        this.ownerName = businessRequestDto.getOwnerName();
        this.telephone = businessRequestDto.getTelephone().getNo();
        this.email = businessRequestDto.getEmail();
        this.businessRegNo = businessRequestDto.getBusinessRegNo();
        this.address = businessRequestDto.getAddress();
        this.imageUrl = businessRequestDto.getImageUrl();
        this.webSite = businessRequestDto.getWebSite();
        this.facebook = businessRequestDto.getFacebook();
        this.instagram = businessRequestDto.getInstagram();
        this.createdAt = createdAt;
        this.updatedAt = new Date();
        this.approvalStatus = ApprovalStatus.PENDING;
    }
}
