package com.alphonso.profile_service.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alphonso.profile_service.Entity.Education;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByProfile_Id(Long profileId);
}
