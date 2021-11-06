package com.swivel.cc.auth.service;

import com.swivel.cc.auth.domain.entity.RejectedMerchant;
import com.swivel.cc.auth.domain.request.MerchantStatusUpdateRequestDto;
import com.swivel.cc.auth.exception.AuthServiceException;
import com.swivel.cc.auth.repository.RejectedMerchantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Rejected merchant service
 */
@Service
@Slf4j
public class RejectedMerchantService {

    private final RejectedMerchantRepository rejectedMerchantRepository;

    @Autowired
    public RejectedMerchantService(RejectedMerchantRepository rejectedMerchantRepository) {
        this.rejectedMerchantRepository = rejectedMerchantRepository;
    }

    /**
     * This method will add comment for rejected merchant.
     *
     * @param merchantStatusUpdateRequestDto requestDto
     */
    public void addComment(MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto) {
        try {
            RejectedMerchant rejectedMerchant = new RejectedMerchant(merchantStatusUpdateRequestDto);
            rejectedMerchantRepository.save(rejectedMerchant);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Saving rejected-merchant comment to database was failed", e);
        }
    }
}
