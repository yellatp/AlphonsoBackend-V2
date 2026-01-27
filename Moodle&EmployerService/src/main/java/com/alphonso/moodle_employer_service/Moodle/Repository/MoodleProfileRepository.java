package com.alphonso.moodle_employer_service.Moodle.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.alphonso.moodle_employer_service.Moodle.Entity.MoodleProfile;
import com.alphonso.moodle_employer_service.Moodle.Entity.MoodleProfile.Status;

@Repository
public interface MoodleProfileRepository extends JpaRepository<MoodleProfile, Long> {
    Optional<MoodleProfile> findByProfileId(String profileId);
    Optional<MoodleProfile> findByMoodleUserId(Long moodleUserId);
    
    Optional<MoodleProfile> findByEmail(String email);
    
    @Query("select m.moodleUserId from MoodleProfile m where m.status = :status")
    List<Long> findMoodleUserIdsByStatus(@Param("status") Status status);

}
