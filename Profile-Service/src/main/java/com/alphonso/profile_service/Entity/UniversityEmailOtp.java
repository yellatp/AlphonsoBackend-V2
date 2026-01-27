package com.alphonso.profile_service.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "university_email_otps", indexes = {@Index(columnList = "email")})
@NoArgsConstructor 
@AllArgsConstructor 
@Builder

public class UniversityEmailOtp {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private Integer otp;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private int attempts;
    private boolean verified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private ProfileDetails profile;
    
    public UniversityEmailOtp(String email, Integer otp, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.email = email;
        this.otp = otp;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.verified = false;
        this.attempts = 0;
    }
}
