package com.alphonso.profile_service.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alphonso.profile_service.Entity.ProfileSkills;

@Repository
public interface ProfileSkillsRepository extends JpaRepository<ProfileSkills, Long> {
	Optional<ProfileSkills> findByProfile_Id(Long profileId);
	
}
