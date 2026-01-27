package com.alphonso.profile_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alphonso.profile_service.Entity.UniversityEmailOtp;
import java.util.Optional;

@Repository
public interface UniversityEmailOtpRepository extends JpaRepository<UniversityEmailOtp, Long> {
    Optional<UniversityEmailOtp> findByEmail(String email);
    void deleteByEmail(String email);
    boolean existsByEmail(String email);
}
