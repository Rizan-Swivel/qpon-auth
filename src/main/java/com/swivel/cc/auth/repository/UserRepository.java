package com.swivel.cc.auth.repository;

import com.swivel.cc.auth.domain.entity.Role;
import com.swivel.cc.auth.domain.entity.User;
import com.swivel.cc.auth.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * This method finds and returns relevant user if given mobile no is existed.
     *
     * @param mobileNo mobile no
     * @return user / null
     */
    User findByMobileNo(String mobileNo);

    /**
     * This method finds and returns relevant user if given email address is existed.
     *
     * @param email email address
     * @return user/ null
     */
    User findByEmail(String email);

    /**
     * This method finds and returns relevant user if given userId is existed
     *
     * @param userId userId
     * @return user
     */
    Optional<User> findById(String userId);

    /**
     * This method find registered user by mobileNo
     *
     * @param mobileNo mobile no
     * @return user
     */
    User findByMobileNoOrEmailAndIsRegisteredUserTrue(String mobileNo, String email);

    /**
     * This method finds registered user by mobileNoAsUserName or with email.
     *
     * @param mobileNo mobileNoAsUserName
     * @param email    email
     * @return user
     */
    User findByMobileNoAsUserNameOrEmail(String mobileNo, String email);

    /**
     * This method finds the users by role
     *
     * @param role role
     * @return list of users
     */
    List<User> findByRole(Role role);

    Page<User> findByRoleId(int roleId, Pageable pageable);

    Page<User> findByFullNameContaining(String searchTerm, Pageable pageable);

    Page<User> findByRoleIdAndFullNameContaining(int roleId, String searchTerm, Pageable pageable);

    Page<User> findByRoleIdAndFullName(int roleId, String searchTerm, Pageable pageable);

    /**
     * This method will return all pending merchants.
     *
     * @param roleId         roleId
     * @param approvalStatus approvalStatus
     * @param pageable       pageable
     * @return pending merchants
     */
    Page<User> findByRoleIdAndApprovalStatus(int roleId, ApprovalStatus approvalStatus, Pageable pageable);

    /**
     * This method will find pending merchant with fullName.
     *
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @return pending merchant
     */
    @Query(value = "SELECT * FROM User WHERE role_id = 3 AND approvalStatus = 'PENDING' AND fullName = ?1", nativeQuery = true)
    Page<User> getPendingMerchant(String searchTerm, Pageable pageable);

    /**
     * This method will find users with approval status & fullName.
     *
     * @param approvalStatus approvalStatus
     * @param searchTerm     searchTerm
     * @param pageable       pageable
     * @return users
     */
    Page<User> findByApprovalStatusAndFullNameContaining(ApprovalStatus approvalStatus, String searchTerm,
                                                         Pageable pageable);

}
