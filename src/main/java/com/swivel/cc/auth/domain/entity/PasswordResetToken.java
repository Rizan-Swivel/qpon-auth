package com.swivel.cc.auth.domain.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "password_reset_token")
@NoArgsConstructor
public class PasswordResetToken {

    @Transient
    private static final String PSW_RESET_TOKEN_PREFIX = "prt-";

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", unique = true)
    private User user;

    private Date expiryDate;

    public void setExpiration(int minutes) {
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, minutes);
        this.expiryDate = now.getTime();
    }

    public PasswordResetToken(User user) {
        this.id = PSW_RESET_TOKEN_PREFIX + UUID.randomUUID() ;
        this.token = String.valueOf(UUID.randomUUID());
        this.user = user;
        setExpiration(10);
    }

}

