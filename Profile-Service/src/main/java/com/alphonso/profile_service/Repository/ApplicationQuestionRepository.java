package com.alphonso.profile_service.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alphonso.profile_service.Entity.ApplicationQuestions;

@Repository
public interface ApplicationQuestionRepository extends JpaRepository<ApplicationQuestions, Long> {
    Optional<ApplicationQuestions> findById(Long profile);
    Optional<ApplicationQuestions> findByProfile_Id(Long profileId);
}