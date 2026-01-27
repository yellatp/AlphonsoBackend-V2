package com.alphonso.moodle_employer_service.Moodle.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.alphonso.moodle_employer_service.Moodle.Entity.MoodleProfile;
import com.alphonso.moodle_employer_service.Moodle.Entity.ProfileSkillEntity;
import com.alphonso.moodle_employer_service.Moodle.Entity.SkillEntity;

@Repository
public interface ProfileSkillRepository extends JpaRepository<ProfileSkillEntity, Long> {
	
	boolean existsByUserAndSkill(MoodleProfile user, SkillEntity skill);

	List<SkillEntity> findByUserId(Long id);

	@Query("SELECT ps.skill FROM ProfileSkillEntity ps WHERE ps.user.id = :userId")
	List<SkillEntity> findSkillsByUserId(Long userId);

}