package com.swivel.cc.auth.service;

import com.swivel.cc.auth.domain.entity.BlockedMerchant;
import com.swivel.cc.auth.domain.request.MerchantStatusUpdateRequestDto;
import com.swivel.cc.auth.exception.AuthServiceException;
import com.swivel.cc.auth.repository.BlockedMerchantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Blocked merchant service
 */
@Service
@Slf4j
public class BlockedMerchantService {

    private final BlockedMerchantRepository blockedMerchantRepository;

    @Autowired
    public BlockedMerchantService(BlockedMerchantRepository blockedMerchantRepository) {
        this.blockedMerchantRepository = blockedMerchantRepository;
    }

    /**
     * This method will add comment for blocked merchant.
     *
     * @param merchantStatusUpdateRequestDto RequestDto
     */
    public void addComment(MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto) {
        try {
            BlockedMerchant blockedMerchant = new BlockedMerchant(merchantStatusUpdateRequestDto);
            blockedMerchantRepository.save(blockedMerchant);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Saving blocked-merchant comment to database was failed", e);
        }
    }

}
