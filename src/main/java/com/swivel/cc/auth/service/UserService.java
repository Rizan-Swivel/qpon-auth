package com.swivel.cc.auth.service;

import com.swivel.cc.auth.domain.entity.PasswordResetToken;
import com.swivel.cc.auth.domain.entity.Role;
import com.swivel.cc.auth.domain.entity.User;
import com.swivel.cc.auth.domain.entity.UserMobileNo;
import com.swivel.cc.auth.domain.request.*;
import com.swivel.cc.auth.domain.response.BasicUserResponseDto;
import com.swivel.cc.auth.domain.response.UpdatedMobileNoListResponseDto;
import com.swivel.cc.auth.domain.response.UpdatedMobileNoResponseDto;
import com.swivel.cc.auth.enums.ApprovalStatus;
import com.swivel.cc.auth.enums.ErrorResponseStatusType;
import com.swivel.cc.auth.enums.Language;
import com.swivel.cc.auth.enums.RoleType;
import com.swivel.cc.auth.exception.*;
import com.swivel.cc.auth.repository.PasswordResetTokenRepository;
import com.swivel.cc.auth.repository.RoleRepository;
import com.swivel.cc.auth.repository.UserMobileRepository;
import com.swivel.cc.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * User Service
 */
@Service
@Slf4j
public class UserService {

    public static final String EXISTING_EMAIL_ADDRESS = "Already existing email address";
    public static final String EXISTING_MOBILE_NUMBER = "Already existing mobile number";
    private static final String FAILED_GET_USER = "Getting user from database was failed";
    private static final String FAILED_UPDATE_USER = "Updating user to database was failed";
    private static final String FAILED_TO_UPDATE_EMAIL = "Updating email was failed";
    private static final String FAILED_TO_UPDATE_MOBILE = "Updating mobileNo was failed";
    private static final String FAILED_TO_UPDATE_PSW = "Updating password was failed";
    private static final String FAILED_TO_RESET_PSW = "Reset password was failed";
    private static final String FAILED_TO_GET_TOKEN = "Getting token from database was failed";
    private static final String FAILED_TO_ADD_USER_MOBILE_NO = "Adding mobile number to user mobile table failed";
    private static final String FAILED_TO_GET_USER_MOBILE_NO = "Getting updated mobile numbers failed";
    private static final String FAILED_TO_SEND_PASSWORD_RESET_LINK = "Failed to send password reset link";
    private static final String CREATED_AT = "createdAt";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String EXPIRED_PASSWORD_RESET_TOKEN = "Password reset token was expired";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTokenStore tokenStore;
    private final UserMobileRepository userMobileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JdbcTokenStore tokenStore, UserMobileRepository userMobileRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository, RoleRepository roleRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenStore = tokenStore;
        this.userMobileRepository = userMobileRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
    }

    /**
     * This method creates the user in the database.
     *
     * @param user user
     */
    public void createUser(User user) {
        try {
            userRepository.save(user);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Saving user to database was failed", e);
        }
    }

    /**
     * This method returns the default role
     *
     * @return role
     */
    public Role getDefaultRole() {
        try {
            return roleRepository.findByName(RoleType.USER.name());
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get role", e);
        }
    }

    /**
     * This method creates an unregistered user
     *
     * @param user user
     */
    public void createUnregisteredUser(User user) {
        try {
            if (!isMobileNoExist(user.getMobileNo())) {
                userRepository.save(user);
            } else {
                throw new MobileNoAlreadyExistsException(EXISTING_MOBILE_NUMBER);
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException("Saving user to database was failed", e);
        }
    }

    /**
     * This method checks the user is already registered with the given mobileNo.
     *
     * @param mobileNo mobileNo
     * @return true/false
     */
    public boolean isAlreadyRegisteredUser(String mobileNo) {
        if (isMobileNoExist(mobileNo)) {
            User user = userRepository.findByMobileNo(mobileNo);
            return user.isRegisteredUser();
        }
        return false;
    }

    /**
     * This method checks the existence of the given mobile no.
     *
     * @param mobileNo mobile no
     * @return true/false
     */
    public boolean isMobileNoExist(String mobileNo) {
        try {
            return userRepository.findByMobileNo(mobileNo) != null;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Checking mobile no existence was failed", e);
        }
    }

    /**
     * This method returns existing user by mobile no.
     *
     * @param mobileNo mobileNo
     * @return user
     */
    public User getUserByMobileNo(String mobileNo) {

        try {
            User user = userRepository.findByMobileNo(mobileNo);
            if (user != null) {
                return user;
            } else {
                throw new InvalidUserException(ErrorResponseStatusType.INVALID_MOBILE_NO.getMessage());
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_GET_USER, e);
        }
    }

    /**
     * This method returns existing registered user by mobile no
     *
     * @param userName userName
     * @return user
     */
    public User getRegisteredUser(String userName) {
        try {
            User user = userRepository.findByMobileNoOrEmailAndIsRegisteredUserTrue(userName, userName);
            if (user != null) {
                return user;
            } else {
                throw new InvalidUserException(USER_NOT_FOUND);
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_GET_USER, e);
        }
    }

    /**
     * This method returns existing user by user Id.
     *
     * @param userId userId
     * @return user
     */
    public User getUserByUserId(String userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                return user.get();
            } else {
                throw new InvalidUserException(ErrorResponseStatusType.INVALID_USER_ID.getMessage());
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_GET_USER, e);
        }
    }

    /**
     * This method checks the existence of the given email address.
     *
     * @param email email address
     * @return true/false
     */
    public boolean isEmailExist(String email) {
        try {
            return userRepository.findByEmail(email) != null;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Checking email existence was failed", e);
        }
    }

    /**
     * This method updates existing user
     *
     * @param userUpdateRequestDto userUpdateRequestDto
     * @return user
     */
    public User updateUser(UserUpdateRequestDto userUpdateRequestDto, String userId) {

        try {
            User userByUserId = getUserByUserId(userId);
            userByUserId.setFullName(userUpdateRequestDto.getFullName().trim());
            String imageUrl = userUpdateRequestDto.getImageUrl();
            if (userUpdateRequestDto.getLanguage() != null) {
                userByUserId.setLanguage(Language.getLanguage(userUpdateRequestDto.getLanguage()));
            }
            userByUserId.setImageUrl(imageUrl == null ? null : imageUrl.trim());
            userByUserId.setUpdatedAt(new Date());
            userRepository.save(userByUserId);

            return userByUserId;

        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_UPDATE_USER, e);
        }
    }

    /**
     * This method updates existing user's email
     *
     * @param userEmailUpdateRequestDto userEmailUpdateRequestDto
     * @return user
     */
    public User updateEmail(UserEmailUpdateRequestDto userEmailUpdateRequestDto, String userId) {
        try {
            User userByUserId = getUserByUserId(userId);

            if (updateOnlyForNullEmail(userEmailUpdateRequestDto, userByUserId))
                throw new EmailAlreadyExistsException(EXISTING_EMAIL_ADDRESS);

            if (updateOnlyExistingEmail(userEmailUpdateRequestDto, userByUserId))
                throw new EmailAlreadyExistsException(EXISTING_EMAIL_ADDRESS);

            userByUserId.setUpdatedAt(new Date());
            userRepository.save(userByUserId);
            return userByUserId;
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_UPDATE_EMAIL, e);
        }
    }

    /**
     * Check the user updating the email for the first time
     *
     * @param userId userId
     * @return true/ false
     */
    public boolean isFirstTimeUserEmail(String userId) {
        User userByUserId = getUserByUserId(userId);
        return userByUserId.getEmail() == null;
    }

    /**
     * Read access token
     *
     * @param accessToken accessToken
     * @return true/false
     */
    public boolean readAccessToken(String accessToken) {
        return tokenStore.readAccessToken(accessToken).isExpired();
    }

    /**
     * This method updates mobile number in a existing user
     *
     * @param mobileUpdateRequestDto mobileUpdateRequestDto
     * @return user
     */
    public User updateMobileNo(UserMobileUpdateRequestDto mobileUpdateRequestDto, String userId) {
        try {
            User userByUserId = getUserByUserId(userId);
            String mobileNo = mobileUpdateRequestDto.getMobileNo().getNo();

            if (!userByUserId.getMobileNo().equals(mobileNo)) {
                if (!isMobileNoExist(mobileNo)) {
                    userByUserId.setMobileNo(mobileNo);
                    userByUserId.setMobileNoAsUserName(mobileNo.replace("-", ""));
                } else {
                    throw new MobileNoAlreadyExistsException("Already existing mobile no");
                }
            }
            userByUserId.setUpdatedAt(new Date());
            userRepository.save(userByUserId);
            return userByUserId;
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_UPDATE_MOBILE, e);
        }
    }

    /**
     * This method updates password of a existing user
     *
     * @param userPasswordUpdateRequestDto userPasswordUpdateRequestDto
     */
    public void updatePassword(UserPasswordUpdateRequestDto userPasswordUpdateRequestDto, String userId) {
        try {
            User userByUserId = getUserByUserId(userId);
            if (passwordEncoder.matches(userPasswordUpdateRequestDto.getPassword(),
                    userByUserId.getPassword())) {
                userByUserId.setPassword(passwordEncoder.encode(userPasswordUpdateRequestDto.getNewPassword()));
                userByUserId.setUpdatedAt(new Date());
                userRepository.save(userByUserId);
            } else {
                throw new InvalidPasswordException(FAILED_TO_UPDATE_PSW);
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_UPDATE_PSW, e);
        }
    }

    /**
     * Sent email via email service
     *
     * @param email email address
     */
    public void sendPasswordResetLink(String email) {
        try {
            User userByUserId = getRegisteredUser(email);
            PasswordResetToken existingToken = passwordResetTokenRepository.findByUserId(userByUserId.getId());
            if (existingToken != null) {
                passwordResetTokenRepository.delete(existingToken);
            }
            PasswordResetToken passwordResetToken = new PasswordResetToken(userByUserId);
            passwordResetTokenRepository.save(passwordResetToken);
            //send the link as email
            boolean sent = emailService.sendEmail(email, passwordResetToken.getToken());
            if (!sent) {
                throw new SendEmailFailedException("Failed to sent password reset link");
            }
            // to be removed after verification
            log.info("Reset Token .................. " + passwordResetToken.getToken());
            log.info("Reset expiration .................. " + passwordResetToken.getExpiryDate());
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_SEND_PASSWORD_RESET_LINK, e);
        }

    }

    /**
     * This method reset the password of the user
     *
     * @param resetPasswordRequestDto resetPasswordRequestDto
     */
    public User resetPassword(ResetPasswordRequestDto resetPasswordRequestDto) {
        try {

            PasswordResetToken passwordResetToken =
                    passwordResetTokenRepository.findByToken(resetPasswordRequestDto.getToken());

            if (passwordResetToken == null) {
                throw new InvalidUserException(USER_NOT_FOUND);
            }

            if (isTokenExpired(passwordResetToken)) {
                throw new ExpiredPasswordResetTokenException(EXPIRED_PASSWORD_RESET_TOKEN); //request new token
            }

            User user = passwordResetToken.getUser();
            user.setPassword(passwordEncoder.encode(resetPasswordRequestDto.getPassword()));
            user.setUpdatedAt(new Date());
            userRepository.save(user);
            passwordResetTokenRepository.delete(passwordResetToken);
            return user;
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_RESET_PSW, e);
        }
    }

    /**
     * Check token expiration
     *
     * @param passToken passToken
     * @return true/false
     */
    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }

    /**
     * Save updated mobile numbers
     *
     * @param userMobileNo userMobileNo
     */
    public void addUpdatedNumber(UserMobileNo userMobileNo) {
        try {
            userMobileRepository.save(userMobileNo);
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_ADD_USER_MOBILE_NO, e);
        }
    }

    /**
     * Return the list of updated mobile numbers
     *
     * @param pageable pageable
     * @return updatedMobileNoListResponse
     */
    public UpdatedMobileNoListResponseDto getUpdateMobileNoList(Pageable pageable) {
        try {
            pageable = PageRequest.of(pageable.getPageNumber(),
                    pageable.getPageSize(), Sort.Direction.ASC, CREATED_AT);
            Page<UserMobileNo> updatedUserMobileNumbers = userMobileRepository.findAll(pageable);
            userMobileRepository.deleteAll(updatedUserMobileNumbers);
            List<UpdatedMobileNoResponseDto> updatedMobileNoList = new ArrayList<>();
            for (UserMobileNo userMobileNo : updatedUserMobileNumbers) {
                updatedMobileNoList.add(new UpdatedMobileNoResponseDto(userMobileNo));
            }
            return new UpdatedMobileNoListResponseDto(updatedMobileNoList, updatedUserMobileNumbers);
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_GET_USER_MOBILE_NO, e);
        }
    }

    /**
     * This method returns bulk user list
     *
     * @param userId userId
     * @return List<BasicUserResponseDto>
     */
    public List<BasicUserResponseDto> getUserList(List<String> userId, String timeZone) {
        try {
            List<BasicUserResponseDto> basicUserResponseDtoList = new ArrayList<>();
            List<User> users = userRepository.findAllById(userId);
            for (User user : users) {
                basicUserResponseDtoList.add(new BasicUserResponseDto(user, timeZone));
            }
            return basicUserResponseDtoList;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Error getting user list", e);
        }
    }


    /**
     * This method return the token of a user
     *
     * @param userId userId
     * @return OAuth2AccessToken
     */
    public Collection<OAuth2AccessToken> getTokenByUserId(String userId) {
        try {
            return tokenStore.findTokensByUserName(userId);
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_GET_TOKEN, e);
        }
    }

    private boolean updateOnlyForNullEmail(UserEmailUpdateRequestDto userEmailUpdateRequestDto, User userByUserId) {
        if (userByUserId.getEmail() == null && userEmailUpdateRequestDto.getEmail() != null) {
            if (!isEmailExist(userEmailUpdateRequestDto.getEmail().trim())) {
                userByUserId.setEmail(userEmailUpdateRequestDto.getEmail().trim());
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean updateOnlyExistingEmail(UserEmailUpdateRequestDto userEmailUpdateRequestDto, User userByUserId) {
        if (userEmailUpdateRequestDto.getEmail() != null
                && !userByUserId.getEmail().equals(userEmailUpdateRequestDto.getEmail().trim())) {
            if (!isEmailExist(userEmailUpdateRequestDto.getEmail().trim())) {
                userByUserId.setEmail(userEmailUpdateRequestDto.getEmail().trim());
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks if a merchant status is pending.
     *
     * @param userName mobileNo/ email
     * @return true/false
     */
    public boolean isPendingMerchant(String userName) {
        try {
            User user = userRepository.findByMobileNoAsUserNameOrEmail(userName, userName);
            return user != null && user.getApprovalStatus() == ApprovalStatus.PENDING;
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_GET_USER, e);
        }
    }
}
