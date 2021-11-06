package com.swivel.cc.auth.repository;

import com.swivel.cc.auth.domain.entity.Business;
import com.swivel.cc.auth.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, String> {

    Optional<Business> findByMerchantId(String merchantId);

    Page<Business> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);

    Page<Business> findByApprovalStatusAndBusinessNameContaining(ApprovalStatus approvalStatus,
                                                                 String searchTerm, Pageable pageable);

    @Query(value = "SELECT * FROM business b WHERE b.merchantId = ?1 AND b.approvalStatus = 'APPROVED' " +
            "ORDER BY b.updatedAt DESC LIMIT 1", nativeQuery = true)
    Optional<Business> getLatestApprovedBusinessByMerchantId(String merchantId);

    @Query(value = "SELECT * FROM business b WHERE b.merchantId = ?1 ORDER BY b.updatedAt DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<Business> getLatestBusinessByMerchantId(String merchantId);
}
