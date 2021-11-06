package com.swivel.cc.auth.repository;

import com.swivel.cc.auth.domain.entity.Contact;
import com.swivel.cc.auth.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, String> {

    Optional<Contact> findByMerchantId(String merchantId);

    Page<Contact> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);

    Page<Contact> findByApprovalStatusAndNameContaining(ApprovalStatus approvalStatus,
                                                        String searchTerm, Pageable pageable);
}
