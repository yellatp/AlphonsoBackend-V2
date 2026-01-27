package com.alphonso.profile_service.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.alphonso.profile_service.Entity.Experience;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByProfile_Id(Long profileId);
}
