package com.swivel.cc.auth.service;

import com.swivel.cc.auth.configuration.Translator;
import com.swivel.cc.auth.domain.BusinessProfile;
import com.swivel.cc.auth.domain.entity.*;
import com.swivel.cc.auth.domain.request.MerchantStatusUpdateRequestDto;
import com.swivel.cc.auth.domain.request.MobileNoRequestDto;
import com.swivel.cc.auth.enums.ApprovalAction;
import com.swivel.cc.auth.enums.ApprovalStatus;
import com.swivel.cc.auth.enums.ApprovalStatusAction;
import com.swivel.cc.auth.enums.MerchantInfo;
import com.swivel.cc.auth.exception.*;
import com.swivel.cc.auth.repository.BusinessRepository;
import com.swivel.cc.auth.repository.ContactRepository;
import com.swivel.cc.auth.repository.RejectedProfileUpdatesRepository;
import com.swivel.cc.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.swivel.cc.auth.enums.ApprovalStatus.*;

/**
 * Merchant Service
 */
@Service
@Slf4j
public class MerchantService {

    private static final String ALL = "ALL";
    private static final String CREATED_AT = "createdAt";
    private static final String UPDATED_AT = "updatedAt";
    private static final String INVALID_ACTION = "Invalid action";
    private static final String EMAIL_GREETING_PREFIX = "<MERCHANT-NAME>";
    private static final String INVALID_MERCHANT_ID = "Invalid merchant id";
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final RejectedProfileUpdatesRepository rejectedProfileUpdatesRepository;
    private final BlockedMerchantService blockedMerchantService;
    private final RejectedMerchantService rejectedMerchantService;
    private final NotifcationService notifcationService;
    NotificationMetaData notificationMetaData;
    @Autowired
    Translator translator;

    @Autowired
    public MerchantService(ContactRepository contactRepository, UserRepository userRepository,
                           RejectedProfileUpdatesRepository rejectedProfileUpdatesRepository,
                           BlockedMerchantService blockedMerchantService, BusinessRepository businessRepository,
                           RejectedMerchantService rejectedMerchantService, NotifcationService notifcationService) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.rejectedProfileUpdatesRepository = rejectedProfileUpdatesRepository;
        this.businessRepository = businessRepository;
        this.blockedMerchantService = blockedMerchantService;
        this.rejectedMerchantService = rejectedMerchantService;
        this.notifcationService = notifcationService;
    }

    /**
     * Method to check ApprovalStatus & ApprovalAction combination.
     *
     * @param existingStatus existingApprovalStatus
     * @param action         approvalAction
     * @return Approval status action
     */
    private ApprovalStatusAction checkApprovalStatusAndAction(String existingStatus, String action) {
        String checkCombinationString = existingStatus + "_" + action;
        try {
            return ApprovalStatusAction.valueOf(checkCombinationString);
        } catch (IllegalArgumentException e) {
            throw new InvalidActionException(INVALID_ACTION);
        }
    }

    /**
     * This method will update approval status for merchant.
     *
     * @param merchantStatusUpdateRequestDto merchantStatusUpdateRequest
     */
    public void updateMerchantApprovalStatus(MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto,
                                             String timeZone) {
        try {

            Optional<User> optionalMerchant = userRepository.findById(merchantStatusUpdateRequestDto.getMerchantId());
            String action = merchantStatusUpdateRequestDto.getAction().toString();
            if (optionalMerchant.isPresent()) {
                User merchant = optionalMerchant.get();
                String existingStatus = merchant.getApprovalStatus().toString();
                ApprovalStatusAction approvalStatusAction = checkApprovalStatusAndAction(existingStatus, action);

                switch (approvalStatusAction) {
                    case BLOCKED_UNBLOCK:
                        merchant.setApprovalStatus(UNBLOCKED);
                        this.notificationMetaData = new NotificationMetaData(UNBLOCKED, merchant, timeZone);
                        break;
                    case UNBLOCKED_BLOCK:
                    case APPROVED_BLOCK:
                        merchant.setApprovalStatus(BLOCKED);
                        blockedMerchantService.addComment(merchantStatusUpdateRequestDto);
                        this.notificationMetaData = new NotificationMetaData(BLOCKED, merchant, timeZone);
                        break;

                    case PENDING_APPROVE:
                        merchant.setApprovalStatus(APPROVED);
                        this.notificationMetaData = new NotificationMetaData(APPROVED, merchant, timeZone);

                        break;

                    case PENDING_REJECT:
                        merchant.setApprovalStatus(REJECTED);
                        rejectedMerchantService.addComment(merchantStatusUpdateRequestDto);
                        this.notificationMetaData = new NotificationMetaData(REJECTED, merchant, timeZone);

                        break;
                }
                merchant.setUpdatedAt(new Date());
                userRepository.save(merchant);
                sendNotification(notificationMetaData);
            } else {
                throw new InvalidUserException("Invalid user id");
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException("Retrieving data failed for approval status update", e);
        }
    }

    /**
     * This method will return pending merchants.
     *
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return pending merchant page
     */
    public Page<User> getPendingMerchant(int page, int size, String searchTerm) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, CREATED_AT);
            Page<User> pendingMerchantPage;
            if (searchTerm.equals(ALL)) {
                pendingMerchantPage = userRepository.findByRoleIdAndApprovalStatus(3,
                        PENDING, pageable);
            } else {
                pendingMerchantPage = userRepository.findByApprovalStatusAndFullNameContaining(ApprovalStatus.PENDING,
                        searchTerm, pageable);
            }
            return pendingMerchantPage;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get merchant", e);
        }
    }

    /**
     * This method is used to get pending business information with searchTerm.
     *
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return page of business
     */
    public Page<Business> getPendingBusinessInfoList(int page, int size, String searchTerm) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, UPDATED_AT);
            Page<Business> businessInfoPage;
            if (searchTerm.equals(ALL)) {
                businessInfoPage = businessRepository.findByApprovalStatus(PENDING, pageable);
            } else {
                businessInfoPage = businessRepository.
                        findByApprovalStatusAndBusinessNameContaining(PENDING, searchTerm, pageable);
            }
            return businessInfoPage;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get pending business info from database.", e);
        }
    }

    /**
     * This method is used to get pending contact information with searchTerm.
     *
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return page of contact
     */
    public Page<Contact> getPendingContactInfoList(int page, int size, String searchTerm) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, UPDATED_AT);
            Page<Contact> contactInfoPage;
            if (searchTerm.equals(ALL)) {
                contactInfoPage = contactRepository.findByApprovalStatus(PENDING, pageable);
            } else {
                contactInfoPage = contactRepository.findByApprovalStatusAndNameContaining(PENDING,
                        searchTerm, pageable);
            }
            return contactInfoPage;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get pending contact info from database.", e);
        }
    }

    /**
     * This method saves business info in database.
     *
     * @param business business
     */
    public void saveBusiness(Business business) {
        try {
            businessRepository.save(business);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Saving business data to database was failed", e);
        }
    }

    /**
     * This method is used to get the latest business profile with merchantId.
     *
     * @param merchantId merchantId
     * @return latest business profile.
     */
    public Optional<Business> getLatestBusinessInfoByMerchantId(String merchantId) {
        try {
            return businessRepository.getLatestBusinessByMerchantId(merchantId);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get latest business profile from database.", e);
        }
    }

    /**
     * This method is used to get latest approved business profile with merchantId.
     *
     * @param merchantId merchantId
     * @return existing business info
     */
    public Optional<Business> getApprovedBusinessInfoByMerchantId(String merchantId) {
        try {
            return businessRepository.getLatestApprovedBusinessByMerchantId(merchantId);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get latest approved business profile from database.", e);
        }
    }

    /**
     * This method is used to get merchant business profile with businessId.
     *
     * @param businessId businessId
     * @return existing business info
     */
    public Business getBusinessInfoByBusinessId(String businessId) {
        try {
            Optional<Business> business = businessRepository.findById(businessId);
            if (business.isPresent()) {
                return business.get();
            } else {
                throw new BusinessProfileException("Invalid business id");
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get business profile from database.", e);
        }
    }

    /**
     * This method saves contact info in database.
     *
     * @param contact contact
     */
    public void saveContact(Contact contact) {
        try {
            contactRepository.save(contact);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Saving contact data to database was failed", e);
        }
    }

    /**
     * This method will check whether merchant exist in user table.
     *
     * @param merchantId merchantId
     */
    public void validateMerchantId(String merchantId) {
        Optional<User> user = userRepository.findById(merchantId);
        if (user.isEmpty()) {
            throw new InvalidUserException(INVALID_MERCHANT_ID);
        }
    }

    /**
     * This method will get merchant's contact data with merchantId.
     *
     * @param merchantId merchantId
     * @return existing contact info
     */
    public Optional<Contact> getContactInfoByMerchantId(String merchantId) {
        try {
            return contactRepository.findByMerchantId(merchantId);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get merchant contact info from database.", e);
        }
    }

    /**
     * This method will return merchant from user table.
     *
     * @param merchantId merchantId
     * @return existing user
     */
    public User getMerchantById(String merchantId) {
        Optional<User> user = userRepository.findById(merchantId);
        if (user.isEmpty()) {
            throw new InvalidUserException(INVALID_MERCHANT_ID);
        } else {
            return user.get();
        }
    }

    /**
     * This method is used update merchant's business profile approval status.
     *
     * @param rejectedProfileUpdates rejectedProfileUpdates
     * @param approvalAction         approvalAction
     */
    public void updateMerchantBusinessApprovalStatus(RejectedProfileUpdates rejectedProfileUpdates,
                                                     ApprovalAction approvalAction, String timeZone) {

        try {
            Optional<Business> optionalBusiness = businessRepository.findById(rejectedProfileUpdates.getReferenceId());
            if (optionalBusiness.isPresent()) {
                Business business = optionalBusiness.get();
                User businessMerchant = business.getMerchant();
                ApprovalStatus existingStatus = business.getApprovalStatus();
                if (existingStatus == PENDING && approvalAction == ApprovalAction.APPROVE) {
                    business.setApprovalStatus(APPROVED);
                    this.notificationMetaData = new NotificationMetaData(APPROVED, businessMerchant, timeZone);

                } else if (existingStatus == PENDING && approvalAction == ApprovalAction.REJECT) {
                    business.setApprovalStatus(REJECTED);
                    rejectedProfileUpdates.setMerchantInfoType(MerchantInfo.BUSINESS);
                    rejectedProfileUpdatesRepository.save(rejectedProfileUpdates);
                    this.notificationMetaData = new NotificationMetaData(REJECTED, businessMerchant, timeZone);

                } else {
                    throw new InvalidActionException(INVALID_ACTION);
                }
                business.setUpdatedAt(new Date());
                businessRepository.save(business);
                sendNotification(notificationMetaData);
            } else {
                throw new InvalidUserException("Invalid business id");
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to update business approval status", e);
        }
    }

    /**
     * This method is used update merchant's contact profile approval status.
     *
     * @param rejectedProfileUpdates rejectedProfileUpdates
     * @param approvalAction         approvalAction
     */
    public void updateMerchantContactApprovalStatus(RejectedProfileUpdates rejectedProfileUpdates,
                                                    ApprovalAction approvalAction, String timeZone) {

        try {
            Optional<Contact> optionalContact = contactRepository.findById(rejectedProfileUpdates.getReferenceId());
            if (optionalContact.isPresent()) {
                Contact contact = optionalContact.get();
                User contactMerchant = contact.getMerchant();
                ApprovalStatus existingStatus = contact.getApprovalStatus();
                if (existingStatus == PENDING && approvalAction == ApprovalAction.APPROVE) {
                    contact.setApprovalStatus(APPROVED);
                    this.notificationMetaData = new NotificationMetaData(APPROVED, contactMerchant, timeZone);
                } else if (existingStatus == PENDING && approvalAction == ApprovalAction.REJECT) {
                    contact.setApprovalStatus(REJECTED);
                    rejectedProfileUpdates.setMerchantInfoType(MerchantInfo.CONTACT);
                    rejectedProfileUpdatesRepository.save(rejectedProfileUpdates);
                    this.notificationMetaData = new NotificationMetaData(REJECTED, contactMerchant, timeZone);
                } else {
                    throw new InvalidActionException(INVALID_ACTION);
                }
                contact.setUpdatedAt(new Date());
                contactRepository.save(contact);
                sendNotification(notificationMetaData);
            } else {
                throw new InvalidUserException("Invalid contact id");
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to update contact approval status", e);
        }
    }

    /**
     * This method will get merchant's contact data with contactId.
     *
     * @param contactId contactId
     * @return existing contact info
     */
    public Contact getContactInfoByContactId(String contactId) {
        try {
            Optional<Contact> contact = contactRepository.findById(contactId);
            if (contact.isPresent()) {
                return contact.get();
            } else {
                throw new ContactProfileException("Invalid contact id");
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get contact profile from database.", e);
        }
    }

    /**
     * This method is used to get bulk merchant information.
     *
     * @param merchantIds merchantIds
     * @return merchant list.
     */
    public List<Business> getBulkMerchantList(List<String> merchantIds) {
        try {
            List<Business> businessList = new ArrayList<>();
            for (String merchantId : merchantIds) {
                Optional<Business> business = businessRepository.getLatestApprovedBusinessByMerchantId(merchantId);
                business.ifPresent(businessList::add);
            }
            return businessList;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Error getting bulk merchant list", e);
        }
    }


    /**
     * This method generate sms, email
     *
     * @param notificationMetaData notificationMetaData
     */
    private void sendNotification(NotificationMetaData notificationMetaData) {

        String sms = translator.toLocale(notificationMetaData.getApprovalStatus().getSms());
        var mobileNoRequestDto = new MobileNoRequestDto(notificationMetaData.getMerchant().getMobileNo());
        var timeZone = notificationMetaData.getTimeZone();
        notifcationService.sendSms(mobileNoRequestDto, sms, timeZone);
        if (!notificationMetaData.getMerchant().getEmail().isEmpty()
                && notificationMetaData.getMerchant().getEmail() != null) {
            String subject = translator.toLocale(notificationMetaData.getApprovalStatus().getEmailSubject());
            String bodyTemplate = translator.toLocale(notificationMetaData.getApprovalStatus().getEmailBody());
            String body = bodyTemplate.replace(EMAIL_GREETING_PREFIX, notificationMetaData.getMerchant().getFullName());
            String merchantEmail = notificationMetaData.getMerchant().getEmail();
            notifcationService.sendMail(merchantEmail, subject, body, timeZone);
        }
    }

    /**
     * This method will check whether merchant exist in business table.
     *
     * @param merchantId merchantId
     */
    public void validateBusinessProfileWithMerchantId(String merchantId) {
        Optional<Business> business = businessRepository.findByMerchantId(merchantId);
        if (business.isEmpty()) {
            throw new InvalidUserException(INVALID_MERCHANT_ID);
        }
    }

    /**
     * This method is used to get merchant business profile details at login.
     *
     * @param merchantId merchantId
     * @return merchant business profile details.
     */
    public BusinessProfile getMerchantLoginResponse(String merchantId) {
        BusinessProfile businessProfile = new BusinessProfile();
        Optional<Business> business = getApprovedBusinessInfoByMerchantId(merchantId);
        if (business.isEmpty()) {
            businessProfile.setUpdated(false);
        } else {
            businessProfile.setUpdated(true);
            businessProfile.setApprovalStatus(business.get().getApprovalStatus());
        }
        return businessProfile;
    }
}
